package ecommerce.example.ecommerce.application.products;

import ecommerce.example.ecommerce.application.common.UseCaseException;
import ecommerce.example.ecommerce.domain.products.Product;
import ecommerce.example.ecommerce.domain.products.ProductId;
import ecommerce.example.ecommerce.domain.products.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportProductsUseCase {

    private final ProductRepository productRepository;

    @Transactional(rollbackFor = Exception.class)
    public int execute(MultipartFile file, String branchId) {
        if (file == null || file.isEmpty()) {
            throw new UseCaseException("Định dạng file Excel không hợp lệ hoặc dữ liệu bị trống");
        }

        List<Product> listToSave = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new UseCaseException("Định dạng file Excel không hợp lệ hoặc dữ liệu bị trống");
            }

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            
            // Đọc bắt đầu từ dòng thứ 2 (Row index 1 - bỏ qua hàng tiêu đề)
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                Cell cellName = row.getCell(0);
                Cell cellSku = row.getCell(1);
                Cell cellPrice = row.getCell(2);
                Cell cellStock = row.getCell(3);
                Cell cellDesc = row.getCell(4);

                if (cellName == null || cellSku == null || cellPrice == null || cellStock == null) {
                    throw new UseCaseException("Định dạng file Excel không hợp lệ hoặc dữ liệu bị trống");
                }

                String name = getCellStringValue(cellName);
                String sku = getCellStringValue(cellSku);

                if (name.isEmpty() || sku.isEmpty()) {
                    throw new UseCaseException("Định dạng file Excel không hợp lệ hoặc dữ liệu bị trống");
                }

                double priceVal;
                int stockVal;

                try {
                    priceVal = getCellNumericValue(cellPrice);
                    stockVal = (int) getCellNumericValue(cellStock);
                } catch (Exception e) {
                    throw new UseCaseException("Định dạng file Excel không hợp lệ hoặc dữ liệu bị trống", e);
                }

                String description = cellDesc != null ? getCellStringValue(cellDesc) : "";

                Product product = new Product();
                product.setId(ProductId.fromString(UUID.randomUUID().toString()));
                product.setBranchId(branchId); // găm cứng sản phẩm ăn theo mã ID chi nhánh
                product.setProductCode(sku);
                product.setName(name);
                product.setDescription(description);
                product.setPrice(BigDecimal.valueOf(priceVal));
                product.setStockQuantity(stockVal);
                product.setSoldQuantity(0);
                product.setStatus("AVAILABLE");

                listToSave.add(product);
            }

            if (listToSave.isEmpty()) {
                throw new UseCaseException("Định dạng file Excel không hợp lệ hoặc dữ liệu bị trống");
            }

            for (Product p : listToSave) {
                productRepository.save(p);
            }

        } catch (UseCaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi import excel", e);
            throw new UseCaseException("Định dạng file Excel không hợp lệ hoặc dữ liệu bị trống", e);
        }

        return listToSave.size();
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            double val = cell.getNumericCellValue();
            if (val == (long) val) {
                return String.valueOf((long) val);
            }
            return String.valueOf(val);
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return "";
    }

    private double getCellNumericValue(Cell cell) {
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            return Double.parseDouble(cell.getStringCellValue().trim());
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue() ? 1.0 : 0.0;
        }
        return 0.0;
    }
}