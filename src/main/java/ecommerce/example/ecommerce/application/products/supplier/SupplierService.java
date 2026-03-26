// package ecommerce.example.ecommerce.application.products.supplier;

// @Service
// @RequiredArgsConstructor
// public class SupplierService {
//     private final SupplierRepository supplierRepository;

//     // Create
//     public Supplier createSupplier(SupplierRequest request) {
//         if(supplierRepository.existsByCode(request.code())) {
//             throw new RuntimeException("Mã nhà cung cấp đã tồn tại!");
//         }
//         Supplier supplier = Supplier.builder()
//             .code(request.code())
//             .name(request.name())
//             .address(request.address())
//             .phoneNumber(request.phoneNumber())
//             .email(request.email())
//             .providedProduct(request.providedProduct())
//             .quantity(request.quantity())
//             .unitPrice(request.unitPrice())
//             .build();
//         return supplierRepository.save(supplier);
//     }

//     // Read All
//     public List<Supplier> getAllSuppliers() {
//         return supplierRepository.findAll();
//     }

//     // Update
//     public Supplier updateSupplier(Long id, SupplierRequest request) {
//         Supplier existing = supplierRepository.findById(id)
//             .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
        
//         existing.setName(request.name());
//         existing.setAddress(request.address());
//         existing.setPhoneNumber(request.phoneNumber());
//         existing.setEmail(request.email());
//         existing.setProvidedProduct(request.providedProduct());
//         existing.setQuantity(request.quantity());
//         existing.setUnitPrice(request.unitPrice());

//         return supplierRepository.save(existing);
//     }

//     // Delete
//     public void deleteSupplier(Long id) {
//         supplierRepository.deleteById(id);
//     }
// }