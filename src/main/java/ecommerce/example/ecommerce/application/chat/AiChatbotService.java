package ecommerce.example.ecommerce.application.chat;

import ecommerce.example.ecommerce.domain.products.Product;
import ecommerce.example.ecommerce.domain.products.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatbotService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    // ── Fallback model chain ─────────────────────────────────────────────────
    private static final List<String> MODEL_CHAIN = List.of(
            "gemini-2.0-flash-lite",
            "gemini-1.5-flash-8b",
            "gemini-1.5-flash",
            "gemini-1.0-pro"
    );

    // ── Cache: 30-minute TTL, max 200 entries ────────────────────────────────
    private static final long CACHE_TTL_MS  = 30 * 60 * 1000L;
    private static final int  CACHE_MAX     = 200;

    private static final class CacheEntry {
        final String value;
        final long   expiresAt;
        CacheEntry(String v) {
            this.value     = v;
            this.expiresAt = Instant.now().toEpochMilli() + CACHE_TTL_MS;
        }
        boolean valid() { return Instant.now().toEpochMilli() < expiresAt; }
    }

    private final Map<String, CacheEntry> responseCache = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> e) {
                    return size() > CACHE_MAX;
                }
            }
    );

    // ── Per-model rate-limit cool-down tracker ───────────────────────────────
    // Default cool-down: 90 seconds per model after a 429
    private static final long MODEL_COOLDOWN_MS = 90_000L;
    private final Map<String, AtomicLong> modelCooldownUntil = new ConcurrentHashMap<>();

    // ── Global rate-limiter: allow only 1 live Gemini call at a time ─────────
    // Prevents "burst 429" when multiple users send messages simultaneously
    private final Semaphore apiCallSlot = new Semaphore(1, true);

    // ── Retry config: if all models 429, wait and retry up to 2 times ────────
    private static final int  MAX_GLOBAL_RETRIES = 2;
    private static final long RETRY_WAIT_MS      = 8_000L; // 8 seconds between retries

    // ─────────────────────────────────────────────────────────────────────────

    public String getBotResponse(String userMessage) {
        String cacheKey = userMessage.trim().toLowerCase(Locale.ROOT);

        // 1. Serve from cache if available
        CacheEntry cached = responseCache.get(cacheKey);
        if (cached != null && cached.valid()) {
            log.info("[AI CHATBOT] Cache HIT: \"{}\"", abbrev(userMessage));
            return cached.value;
        }

        // 2. Build prompt once
        String fullPrompt = buildFullPrompt(userMessage);

        // 3. Try with global retries
        for (int attempt = 0; attempt <= MAX_GLOBAL_RETRIES; attempt++) {
            if (attempt > 0) {
                log.warn("[AI CHATBOT] Global retry #{} — waiting {}s before re-trying all models…",
                        attempt, RETRY_WAIT_MS / 1000);
                sleep(RETRY_WAIT_MS);
            }

            String result = tryAllModels(fullPrompt);
            if (result != null) {
                responseCache.put(cacheKey, new CacheEntry(result));
                return result;
            }
        }

        // 4. All retries exhausted
        log.error("[AI CHATBOT] All models and retries exhausted for: \"{}\"", abbrev(userMessage));
        return buildOfflineAnswer(userMessage);
    }

    // ── Iterate through model chain, return first successful response ─────────
    private String tryAllModels(String fullPrompt) {
        for (String model : MODEL_CHAIN) {
            AtomicLong coolUntil = modelCooldownUntil.computeIfAbsent(model, k -> new AtomicLong(0));
            long now = Instant.now().toEpochMilli();
            if (now < coolUntil.get()) {
                long remaining = (coolUntil.get() - now) / 1000;
                log.warn("[AI CHATBOT] Model {} still rate-limited ({}s left). Skipping.", model, remaining);
                continue;
            }

            try {
                // Serialize API calls so we don't burst the quota
                apiCallSlot.acquire();
                try {
                    String result = callGemini(model, fullPrompt);
                    if (result != null) {
                        log.info("[AI CHATBOT] Model {} → OK", model);
                        return result;
                    }
                } finally {
                    apiCallSlot.release();
                }
            } catch (HttpClientErrorException e) {
                apiCallSlot.release();
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    coolUntil.set(Instant.now().toEpochMilli() + MODEL_COOLDOWN_MS);
                    log.warn("[AI CHATBOT] Model {} → 429. Cool-down {}s. Trying next…",
                            model, MODEL_COOLDOWN_MS / 1000);
                } else {
                    log.error("[AI CHATBOT] Model {} → HTTP {} — {}",
                            model, e.getStatusCode(), e.getResponseBodyAsString());
                }
            } catch (HttpServerErrorException e) {
                apiCallSlot.release();
                log.error("[AI CHATBOT] Model {} → Server error {} — {}",
                        model, e.getStatusCode(), e.getResponseBodyAsString());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[AI CHATBOT] Interrupted waiting for API slot.");
                return null;
            } catch (Exception e) {
                apiCallSlot.release();
                log.error("[AI CHATBOT] Model {} → Unexpected error: {}", model, e.getMessage());
            }
        }
        return null; // All models failed this round
    }

    // ── Build a smart offline answer when API is unavailable ─────────────────
    private String buildOfflineAnswer(String userMessage) {
        String q = userMessage.toLowerCase(Locale.ROOT);

        // Product / price keywords
        if (q.contains("giá") || q.contains("hàng") || q.contains("sản phẩm") || q.contains("tồn kho")) {
            try {
                List<Product> products = productRepository.findAll();
                if (!products.isEmpty()) {
                    StringBuilder sb = new StringBuilder("📦 Dữ liệu kho hàng hiện tại (phản hồi ngoại tuyến):\n\n");
                    products.stream().limit(10).forEach(p ->
                        sb.append(String.format("• %s — %s VNĐ (tồn: %d)\n",
                                p.getName(),
                                p.getPrice() != null ? p.getPrice().toPlainString() : "N/A",
                                p.getStock()  != null ? p.getStock() : 0))
                    );
                    if (products.size() > 10) sb.append("…và ").append(products.size() - 10).append(" mặt hàng khác.\n");
                    sb.append("\n⚠️ Trợ lý AI đang quá tải. Vui lòng thử lại sau vài phút.");
                    return sb.toString();
                }
            } catch (Exception ex) {
                log.warn("[AI CHATBOT] Offline fallback: failed to load products: {}", ex.getMessage());
            }
        }

        // POS / system keywords
        if (q.contains("pos") || q.contains("vietqr") || q.contains("thanh toán")) {
            return "💳 Hướng dẫn POS & VietQR:\n\n"
                    + "1. Mở màn hình Bán Hàng (POS).\n"
                    + "2. Quét mã hàng hoặc chọn sản phẩm từ danh sách.\n"
                    + "3. Nhấn \"Thanh toán\" → chọn VietQR / Tiền mặt / MoMo / VNPay.\n"
                    + "4. Quét mã QR bằng app ngân hàng của khách.\n\n"
                    + "⚠️ Trợ lý AI đang quá tải. Vui lòng thử lại sau vài phút.";
        }

        if (q.contains("excel") || q.contains("hóa đơn") || q.contains("xuất")) {
            return "📊 Xuất hóa đơn Excel:\n\n"
                    + "1. Vào mục Quản lý Đơn Hàng.\n"
                    + "2. Lọc theo ngày hoặc chi nhánh.\n"
                    + "3. Nhấn nút \"Xuất Excel\" ở góc trên bên phải.\n"
                    + "4. File .xlsx sẽ tải về tự động.\n\n"
                    + "⚠️ Trợ lý AI đang quá tải. Vui lòng thử lại sau vài phút.";
        }

        // Generic fallback
        return "⚠️ Trợ lý AI đang tạm thời quá tải do giới hạn API miễn phí. "
                + "Vui lòng thử lại sau 1–2 phút, hoặc chuyển sang tab \"Trực Quầy Admin\" "
                + "để được hỗ trợ trực tiếp.";
    }

    // ── Call one specific Gemini model ────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private String callGemini(String modelName, String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + modelName + ":generateContent?key=" + apiKey;

        log.info("[AI CHATBOT] → {}", modelName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> contentMap = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        parts.add(Map.of("text", prompt));
        contentMap.put("parts", parts);
        contents.add(contentMap);
        requestBody.put("contents", contents);

        // Safety: trim prompt so giant product lists don't exceed token limit
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
        if (response == null || !response.containsKey("candidates")) return null;

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) return null;

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) return null;

        List<Map<String, Object>> resParts = (List<Map<String, Object>>) content.get("parts");
        if (resParts == null || resParts.isEmpty()) return null;

        Object text = resParts.get(0).get("text");
        return text != null ? text.toString() : null;
    }

    // ── Build system prompt with live product context ─────────────────────────
    private String buildFullPrompt(String userMessage) {
        List<Product> products;
        try {
            products = productRepository.findAll();
        } catch (Exception e) {
            products = Collections.emptyList();
        }

        StringBuilder ctx = new StringBuilder("Danh sách hàng hóa trong kho hiện tại:\n");
        // Limit to 50 products to avoid token overflow
        products.stream().limit(50).forEach(p ->
                ctx.append(String.format("- Tên: %s, Giá: %s VNĐ, Tồn kho: %d, Trạng thái: %s\n",
                        p.getName(),
                        p.getPrice() != null ? p.getPrice().toPlainString() : "0",
                        p.getStock()  != null ? p.getStock()  : 0,
                        p.getStatus() != null ? p.getStatus() : "AVAILABLE"))
        );

        return "Bạn là một trợ lý thông minh giải đáp kỹ thuật và bán hàng tại quầy POS MarketHub.\n"
                + "Dưới đây là ngữ cảnh kho hàng hiện hành:\n"
                + ctx
                + "\nLuật tương tác:\n"
                + "1. Nếu khách hỏi về sản phẩm, đối chiếu dữ liệu kho để trả lời giá và tồn kho.\n"
                + "2. Nếu hỏi câu hỏi chung, giải đáp về POS, Excel, hàng hóa, hệ thống.\n"
                + "Hãy trả lời ngắn gọn, chuyên nghiệp bằng tiếng Việt.\n\n"
                + "Yêu cầu khách hàng: " + userMessage;
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static String abbrev(String s) {
        return s.length() > 60 ? s.substring(0, 60) + "…" : s;
    }
}
