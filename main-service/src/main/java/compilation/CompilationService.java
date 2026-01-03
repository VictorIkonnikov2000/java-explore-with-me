package compilation;



import compilation.dto.CompilationDto;
import compilation.dto.NewCompilationDto;
import compilation.dto.UpdateCompilationRequest;
import java.util.List;

public interface CompilationService {

    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);

    List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size); //For public API

    CompilationDto getCompilationById(Long compId);
}
