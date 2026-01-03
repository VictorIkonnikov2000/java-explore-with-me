package compilation.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;

    private String description; // Description is optional

    private Boolean pinned = false; // Default value

    private List<Long> events; // List of event IDs
}
