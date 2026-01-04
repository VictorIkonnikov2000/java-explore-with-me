package compilation;


import compilation.dto.CompilationDto;
import compilation.dto.NewCompilationDto;
import compilation.dto.UpdateCompilationRequest;
import event.dto.EventShortDto;
import event.Event;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CompilationMapper {

    public CompilationDto toCompilationDto(Compilation compilation) {
        return new CompilationDto(
                compilation.getId(),
                compilation.getTitle(),
                compilation.getDescription(),
                compilation.getPinned(),
                (compilation.getEvents() != null) ? compilation.getEvents().stream().map(this::toEventShortDto).collect(Collectors.toList()) : null // Replace with toEventShortDto!
        );
    }

    public Compilation toCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setDescription(newCompilationDto.getDescription());
        compilation.setPinned(newCompilationDto.getPinned());
        return compilation;
    }

    public EventShortDto toEventShortDto(Event event) { // Заглушка. нужно прописать логику как в eventMapper.
        return null;
    }

    ;

    public void updateCompilation(UpdateCompilationRequest updateCompilationRequest, Compilation compilation) {
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getDescription() != null) {
            compilation.setDescription(updateCompilationRequest.getDescription());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
    }
}
