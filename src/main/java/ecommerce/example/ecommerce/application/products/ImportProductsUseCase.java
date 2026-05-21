package ecommerce.example.ecommerce.application.products;

import ecommerce.example.ecommerce.domain.products.Product;
import ecommerce.example.ecommerce.domain.products.ProductId;
import ecommerce.example.ecommerce.domain.products.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImportProductsUseCase {

    private final ProductRepository productRepository;

    @Transactional(rollbackFor = Exception.class) // Lỗi bất kỳ dòng nào sẽ hủy toàn bộ giao dịch (Atomic)
    public int execute(MultipartFile file, String ownerId) throws Exception {
        List<Product> listToSave = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Bỏ qua dòng tiêu đề (Header Row) của bảng Excel
            if (rows.hasNext()) {
                rows.next();
            }

            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Kiểm tra nếu ô mã sản phẩm rỗng thì bỏ qua dòng đó tránh lỗi Null
                if (currentRow.getCell(0) == null || currentRow.getCell(0).getCellType() == CellType.BLANK) {
                    continue;
                }

                // Đọc dữ liệu thô từ các cột Excel tương ứng cấu trúc thực thể của bạn
                String productCode = currentRow.getCell(0).getStringCellValue();
                String name = currentRow.getCell(1).getStringCellValue();
                String description = currentRow.getCell(2) != null ? currentRow.getCell(2).getStringCellValue() : "";
                double priceValue = currentRow.getCell(3).getNumericCellValue();
                int stockQuantity = (int) currentRow.getCell(4).getNumericCellValue();
                String warehouse = currentRow.getCell(5) != null ? currentRow.getCell(5).getStringCellValue() : "";
                String supplier = currentRow.getCell(6) != null ? currentRow.getCell(6).getStringCellValue() : "";

                // Ánh xạ dữ liệu vào cấu trúc Domain Entity gốc
                Product entity = new Product();
                entity.setId(ProductId.fromString(UUID.randomUUID().toString()));
                entity.setOwnerId(ownerId); // Gán quyền sở hữu cố định cho shop/nhánh hiện tại
                entity.setProductCode(productCode);
                entity.setName(name);
                entity.setDescription(description);
                entity.setPrice(BigDecimal.valueOf(priceValue));
                entity.setStockQuantity(stockQuantity);
                entity.setSoldQuantity(0); // Hàng mới import mặc định số lượng bán bằng 0
                entity.setWarehouse(warehouse);
                entity.setSupplier(supplier);
                entity.setStatus("AVAILABLE"); // Mặc định sẵn sàng mở bán tại quầy chi nhánh

                listToSave.add(entity);
            }

            // 🌟 ĐÃ SỬA: Đổi từ .save(p) thành .persist(p) để khớp 100% với Domain Interface của bạn
            for (Product p : listToSave) {
                productRepository.persist(p);
            }
        }

        return listToSave.size();
    }
}