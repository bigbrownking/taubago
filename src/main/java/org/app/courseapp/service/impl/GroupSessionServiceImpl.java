package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.request.CreateGroupSessionRequest;
import org.app.courseapp.dto.response.GroupSessionDto;
import org.app.courseapp.model.GroupSession;
import org.app.courseapp.model.users.Specialist;
import org.app.courseapp.model.users.User;
import org.app.courseapp.repository.GroupSessionRepository;
import org.app.courseapp.service.GroupSessionService;
import org.app.courseapp.service.UserService;
import org.app.courseapp.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupSessionServiceImpl implements GroupSessionService {

    private final GroupSessionRepository groupSessionRepository;
    private final UserService userService;
    private final Mapper mapper;

    @Transactional
    public GroupSessionDto create(CreateGroupSessionRequest request) {
        User user = userService.getCurrentUser();
        if (!(user instanceof Specialist specialist)) {
            throw new RuntimeException("Access denied");
        }

        GroupSession session = GroupSession.builder()
                .specialist(specialist)
                .title(request.getTitle())
                .description(request.getDescription())
                .telegramLink(request.getTelegramLink())
                .scheduledAt(request.getScheduledAt())
                .maxParticipants(request.getMaxParticipants())
                .build();

        return mapper.convertToGroupSessionDto(groupSessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public List<GroupSessionDto> getAll() {
        return groupSessionRepository.findAllByActiveTrue()
                .stream()
                .map(mapper::convertToGroupSessionDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GroupSessionDto> getBySpecialist(Long specialistId) {
        return groupSessionRepository.findBySpecialistIdAndActiveTrue(specialistId)
                .stream()
                .map(mapper::convertToGroupSessionDto)
                .toList();
    }

    @Override
    public GroupSessionDto getById(Long id) {
        return groupSessionRepository.findById(id)
                .filter(GroupSession::isActive)
                .map(mapper::convertToGroupSessionDto)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    @Transactional
    public void delete(Long sessionId) {
        User user = userService.getCurrentUser();
        if (!(user instanceof Specialist specialist)) {
            throw new RuntimeException("Access denied");
        }
        GroupSession session = groupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (!session.getSpecialist().getId().equals(specialist.getId())) {
            throw new RuntimeException("Access denied");
        }
        session.setActive(false);
        groupSessionRepository.save(session);
    }
}
