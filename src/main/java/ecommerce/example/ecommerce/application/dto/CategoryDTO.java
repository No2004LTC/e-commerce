package ecommerce.example.ecommerce.application.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor  
@AllArgsConstructor
public class CategoryDTO {
    private String id;
    private String name;
    private String slug;
    private String parentId;
    private List<CategoryDTO> children; 
}