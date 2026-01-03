package compilation.dto;


import event.dto.EventShortDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {

    private Long id;
    private String title;
    private String description;
    private Boolean pinned;
    private List<EventShortDto> events; // Use EventShortDto
}
