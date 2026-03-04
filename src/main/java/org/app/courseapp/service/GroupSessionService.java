package org.app.courseapp.service;

import org.app.courseapp.dto.request.CreateGroupSessionRequest;
import org.app.courseapp.dto.response.GroupSessionDto;

import java.util.List;

public interface GroupSessionService {
    GroupSessionDto create(CreateGroupSessionRequest request);
    List<GroupSessionDto> getAll();
    List<GroupSessionDto> getBySpecialist(Long specialistId);
    GroupSessionDto getById(Long id);
    void delete(Long id);
}
