package compilation;

import compilation.dto.CompilationDto;
import compilation.dto.NewCompilationDto;
import compilation.dto.UpdateCompilationRequest;

import java.util.Collection;

public interface CompilationService {
    CompilationDto saveCompilation(NewCompilationDto request);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request);

    void deleteCompilation(Long compId);

    CompilationDto getCompilationById(Long compId);

    Collection<CompilationDto> getCompilations(Boolean pinned, int from, int size);
}