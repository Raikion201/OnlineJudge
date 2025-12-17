package com.problemservice.controller;

import com.problemservice.dto.request.EditorialRequest;
import com.problemservice.dto.response.EditorialResponse;
import com.problemservice.service.EditorialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/problems/{problemId}/editorial")
@RequiredArgsConstructor
public class EditorialController {

    private static final String USER_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-Username";

    private final EditorialService editorialService;

    @GetMapping
    public ResponseEntity<EditorialResponse> getEditorial(@PathVariable Long problemId) {
        return ResponseEntity.ok(editorialService.getEditorial(problemId));
    }

    @PostMapping
    public ResponseEntity<EditorialResponse> createEditorial(
            @PathVariable Long problemId,
            @Valid @RequestBody EditorialRequest request,
            @RequestHeader(value = USER_HEADER, required = false) String authorId,
            @RequestHeader(value = USERNAME_HEADER, required = false) String authorName) {
        EditorialResponse response = editorialService.createEditorial(problemId, request, authorId, authorName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<EditorialResponse> updateEditorial(
            @PathVariable Long problemId,
            @Valid @RequestBody EditorialRequest request) {
        return ResponseEntity.ok(editorialService.updateEditorial(problemId, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteEditorial(@PathVariable Long problemId) {
        editorialService.deleteEditorial(problemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> hasEditorial(@PathVariable Long problemId) {
        boolean exists = editorialService.hasEditorial(problemId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/helpful")
    public ResponseEntity<Void> markHelpful(@PathVariable Long problemId) {
        editorialService.markHelpful(problemId);
        return ResponseEntity.ok().build();
    }
}
