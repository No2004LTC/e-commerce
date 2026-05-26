package ecommerce.example.ecommerce.application.order;

import ecommerce.example.ecommerce.domain.order.Order;
import ecommerce.example.ecommerce.domain.order.OrderItem;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * UseCase: Xuất danh sách đơn hàng ra file Excel (.xlsx).
 *
 * Cấu trúc file Excel:
 *  - Sheet "Danh sách đơn hàng": Header + từng dòng Order
 *  - Sheet "Chi tiết sản phẩm":  Từng OrderItem của mỗi đơn hàng
 *
 * Ghi trực tiếp ra OutputStream để tránh tạo file tạm trên disk.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExportOrdersUseCase {

    private final OrderRepository orderRepository;

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Ghi Workbook Excel chứa danh sách đơn hàng ra outputStream.
     *
     * @param currentUserName Tên người dùng hiện tại (dùng để lọc nếu scope là cá nhân).
     * @param scope          "all" để xuất tất cả, ngược lại xuất theo user.
     * @param ids            Danh sách ID đơn hàng cần xuất (phân cách bởi dấu phẩy).
     * @param outputStream   OutputStream từ HttpServletResponse (ghi stream thẳng).
     */
    public void execute(String currentUserName, String scope, String ids, OutputStream outputStream) throws Exception {
        
        List<Order> orders;
        if (ids != null && !ids.isBlank()) {
            List<String> idList = List.of(ids.split(","));
            orders = new ArrayList<>();
            for (String id : idList) {
                orderRepository.findById(id.trim()).ifPresent(orders::add);
            }
        } else {
            // Hỗ trợ trường hợp tải về thông thường (không truyền ids)
            orders = ("all".equalsIgnoreCase(scope))
                ? orderRepository.findAll()
                : orderRepository.findByBuyerId(currentUserName);
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);
            CellStyle borderedStyle = createBorderedStyle(workbook);

            // ── Sheet 1: Tổng hợp đơn hàng ────────────────────────────────────
            Sheet orderSheet = workbook.createSheet("Danh sách đơn hàng");

            // Header Row
            Row headerRow = orderSheet.createRow(0);
            String[] headers = {"Mã đơn hàng", "Email người mua", "Tổng tiền (VNĐ)", "Trạng thái", "Ngày đặt"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (Order order : orders) {
                Row row = orderSheet.createRow(rowIdx++);
                
                Cell c0 = row.createCell(0);
                c0.setCellValue(order.getId());
                c0.setCellStyle(centerStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(order.getBuyerId());
                c1.setCellStyle(borderedStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue(order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0);
                c2.setCellStyle(moneyStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue(order.getStatus());
                c3.setCellStyle(centerStyle);

                Cell c4 = row.createCell(4);
                c4.setCellValue(order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FMT) : "");
                c4.setCellStyle(centerStyle);
            }

            // Auto-size columns for orderSheet
            for (int i = 0; i < headers.length; i++) {
                orderSheet.autoSizeColumn(i);
            }

            // ── Sheet 2: Chi tiết sản phẩm trong đơn ─────────────────────────
            Sheet itemSheet = workbook.createSheet("Chi tiết sản phẩm");

            Row itemHeader = itemSheet.createRow(0);
            String[] itemHeaders = {"Mã đơn hàng", "Tên sản phẩm", "Mã sản phẩm", "Giá tại thời điểm mua", "Số lượng"};
            for (int i = 0; i < itemHeaders.length; i++) {
                Cell cell = itemHeader.createCell(i);
                cell.setCellValue(itemHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int itemRowIdx = 1;
            for (Order order : orders) {
                if (order.getItems() == null) continue;
                for (OrderItem item : order.getItems()) {
                    Row row = itemSheet.createRow(itemRowIdx++);
                    
                    Cell ic0 = row.createCell(0);
                    ic0.setCellValue(order.getId());
                    ic0.setCellStyle(centerStyle);

                    Cell ic1 = row.createCell(1);
                    ic1.setCellValue(item.getProductName());
                    ic1.setCellStyle(borderedStyle);

                    Cell ic2 = row.createCell(2);
                    ic2.setCellValue(item.getProductId());
                    ic2.setCellStyle(centerStyle);

                    Cell ic3 = row.createCell(3);
                    ic3.setCellValue(item.getPriceAtPurchase() != null ? item.getPriceAtPurchase().doubleValue() : 0.0);
                    ic3.setCellStyle(moneyStyle);

                    Cell ic4 = row.createCell(4);
                    ic4.setCellValue(item.getQuantity());
                    ic4.setCellStyle(centerStyle);
                }
            }

            // Auto-size columns for itemSheet
            for (int i = 0; i < itemHeaders.length; i++) {
                itemSheet.autoSizeColumn(i);
            }

            // Ghi workbook trực tiếp ra OutputStream của HttpServletResponse
            workbook.write(outputStream);
            outputStream.flush();

            log.info("[EXPORT] Đã xuất {} đơn hàng ra file Excel thành công.", orders.size());
        }
    }

    // ── Helpers: CellStyle ─────────────────────────────────────────────────────

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(79, 93, 108), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0\" đ\""));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBorderedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
