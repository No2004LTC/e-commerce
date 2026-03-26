// package ecommerce.example.ecommerce.adapter.web.products;

// @RestController
// @RequestMapping("/api/suppliers")
// @RequiredArgsConstructor
// public class SupplierController {
//     private final SupplierService supplierService;

//     @PostMapping
//     public ResponseEntity<Supplier> create(@RequestBody SupplierRequest request) {
//         return ResponseEntity.ok(supplierService.createSupplier(request));
//     }

//     @GetMapping
//     public ResponseEntity<List<Supplier>> getAll() {
//         return ResponseEntity.ok(supplierService.getAllSuppliers());
//     }

//     @PutMapping("/{id}")
//     public ResponseEntity<Supplier> update(@PathVariable Long id, @RequestBody SupplierRequest request) {
//         return ResponseEntity.ok(supplierService.updateSupplier(id, request));
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> delete(@PathVariable Long id) {
//         supplierService.deleteSupplier(id);
//         return ResponseEntity.noContent().build();
//     }
// }