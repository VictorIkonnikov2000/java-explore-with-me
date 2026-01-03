package compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {

    @Size(min = 1, max = 50)
    private String title;

    private String description;

    private Boolean pinned;

    private List<Long> events; // List of event IDs to replace existing ones
}
