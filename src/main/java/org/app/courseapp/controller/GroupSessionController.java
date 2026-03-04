package org.app.courseapp.controller;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.request.CreateGroupSessionRequest;
import org.app.courseapp.dto.response.GroupSessionDto;
import org.app.courseapp.service.GroupSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group-sessions")
@RequiredArgsConstructor
public class GroupSessionController {

    private final GroupSessionService groupSessionService;

    @GetMapping
    public ResponseEntity<List<GroupSessionDto>> getAll() {
        return ResponseEntity.ok(groupSessionService.getAll());
    }

    @GetMapping("/specialist/{specialistId}")
    public ResponseEntity<List<GroupSessionDto>> getBySpecialist(@PathVariable Long specialistId) {
        return ResponseEntity.ok(groupSessionService.getBySpecialist(specialistId));
    }

    @PostMapping
    public ResponseEntity<GroupSessionDto> create(@RequestBody CreateGroupSessionRequest request) {
        return ResponseEntity.ok(groupSessionService.create(request));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> delete(@PathVariable Long sessionId) {
        groupSessionService.delete(sessionId);
        return ResponseEntity.noContent().build();
    }
}
