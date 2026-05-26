package ecommerce.example.ecommerce.infrastructure.security;

import ecommerce.example.ecommerce.adapter.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JwtAuthenticationFilter — Kiểm tra JWT trên mỗi request.
 *
 * Chiến lược 2 tầng (Fast-path / Fallback):
 *  1. Fast-path : Nếu JWT có embed "role" claim → tạo authentication ngay,
 *                 không cần truy vấn database (giảm latency).
 *  2. Fallback  : Nếu JWT cũ chưa có "role" → load UserDetails từ DB như cũ.
 *
 * Đảm bảo role luôn có prefix "ROLE_" để khớp với
 * hasRole("ADMIN") / hasAnyRole("ADMIN","SHOP_OWNER") hoặc hasAnyAuthority trong SecurityConfig.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.isTokenValid(jwt)) {
                String username = tokenProvider.extractUsername(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // ── Fast-path: role đã được nhúng trong JWT claims ──────
                    String roleFromJwt = tokenProvider.extractRole(jwt);

                    UsernamePasswordAuthenticationToken authentication;

                    if (StringUtils.hasText(roleFromJwt)) {
                        // Đảm bảo prefix ROLE_
                        String normalizedRole = roleFromJwt.startsWith("ROLE_")
                                ? roleFromJwt
                                : "ROLE_" + roleFromJwt;

                        List<SimpleGrantedAuthority> authorities =
                                Collections.singletonList(new SimpleGrantedAuthority(normalizedRole));

                        // Tạo UserDetails tối giản — chỉ cần username & authorities
                        UserDetails lightUser = User.withUsername(username)
                                .password("") // password không dùng ở đây
                                .authorities(authorities)
                                .build();

                        authentication = new UsernamePasswordAuthenticationToken(
                                lightUser, null, authorities);

                        logger.debug("JWT fast-path auth: user=" + username + ", role=" + normalizedRole);

                    } else {
                        // ── Fallback: token cũ chưa có role → load từ DB ────
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        logger.debug("JWT fallback (DB) auth: user=" + username);
                    }

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context: " + ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}