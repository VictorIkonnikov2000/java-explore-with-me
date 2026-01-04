package category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCategoryDto {
    @NotBlank(message = "Названия категории не может быть пустым")
    @Size(min = 1, max = 50, message = "Длинна названия категории должна быть от 1 до 50 символов")
    private String name;
}