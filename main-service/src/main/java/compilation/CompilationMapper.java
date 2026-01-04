package compilation;

import org.mapstruct.*;
import compilation.dto.CompilationDto;
import compilation.dto.NewCompilationDto;
import compilation.dto.UpdateCompilationRequest;
import event.EventMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    CompilationDto mapToCompilationDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation mapToCompilation(NewCompilationDto request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    void updateFromRequest(UpdateCompilationRequest request, @MappingTarget Compilation compilation);

    List<CompilationDto> toCompilationDtoList(List<Compilation> compilationList);
}
