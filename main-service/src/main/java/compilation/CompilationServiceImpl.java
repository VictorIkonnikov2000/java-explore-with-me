package compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import compilation.dto.CompilationDto;
import compilation.dto.NewCompilationDto;
import compilation.dto.UpdateCompilationRequest;
import exception.BadRequestException;
import exception.NotFoundException;
import event.EventRepository;
import event.Event;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository repository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto saveCompilation(NewCompilationDto request) {

        if (request == null) {
            throw new BadRequestException("Запрос на добавление новой подборки не может быть null");
        }

        Compilation compilation = compilationMapper.mapToCompilation(request);

        if (request.getPinned() == null) {
            compilation.setPinned(false);
        }

        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            List<Event> eventList = eventRepository.findByIdIn(request.getEvents());
            compilation.setEvents(new HashSet<>(eventList));
        }

        Compilation saveCompilation = repository.save(compilation);
        return compilationMapper.mapToCompilationDto(saveCompilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        isContainsCompilation(compId);
        repository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {

        if (request == null) {
            throw new BadRequestException("Запрос на обновление подборки не может быть null");
        }

        Compilation compilation = isContainsCompilation(compId);

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        if (request.getEvents() != null) {

            if (request.getEvents().isEmpty()) {
                compilation.getEvents().clear();
            } else {
                List<Event> listEvenDto = eventRepository.findByIdIn(request.getEvents());
                compilation.setEvents(new HashSet<>(listEvenDto));
            }
        }

        return compilationMapper.mapToCompilationDto(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = isContainsCompilation(compId);
        return compilationMapper.mapToCompilationDto(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Compilation> compilationsPage;

        if (pinned == null) {
            compilationsPage = repository.findAll(pageable);
        } else {
            compilationsPage = repository.findByPinned(pinned, pageable);
        }

        List<Compilation> compilationList = compilationsPage.getContent();
        return compilationMapper.toCompilationDtoList(compilationList);
    }

    private Compilation isContainsCompilation(Long id) {
        Optional<Compilation> optCompilation = repository.findById(id);

        if (optCompilation.isEmpty()) {
            throw new NotFoundException("Подборка с id: " + id + " в базе отсутствует");
        }

        return optCompilation.get();
    }
}
