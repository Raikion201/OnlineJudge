package com.problemservice.controller;

import com.problemservice.dto.request.ProblemRequest;
import com.problemservice.dto.response.ProblemResponse;
import com.problemservice.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/problems")
@RequiredArgsConstructor
public class ProblemController {
    private final ProblemService problemService;

    @PostMapping
    public ResponseEntity<ProblemResponse> createProblem(@Valid @RequestBody ProblemRequest request) {
        ProblemResponse response = problemService.createProblem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemResponse> getProblemById(@PathVariable Long id) {
        ProblemResponse response = problemService.getProblemById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProblemResponse>> getAllProblems(Pageable pageable) {
        Page<ProblemResponse> responses = problemService.getAllProblems(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<ProblemResponse>> getProblemsByTags(
            @RequestParam("slugs") List<String> tagSlugs) {
        List<ProblemResponse> responses = problemService.getProblemsByTags(tagSlugs);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProblemResponse> updateProblem(
            @PathVariable Long id,
            @Valid @RequestBody ProblemRequest request) {
        ProblemResponse response = problemService.updateProblem(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{problemId}/tags/{tagId}")
    public ResponseEntity<ProblemResponse> addTagToProblem(
            @PathVariable Long problemId,
            @PathVariable Long tagId) {
        ProblemResponse response = problemService.addTagToProblem(problemId, tagId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{problemId}/tags/{tagId}")
    public ResponseEntity<ProblemResponse> removeTagFromProblem(
            @PathVariable Long problemId,
            @PathVariable Long tagId) {
        ProblemResponse response = problemService.removeTagFromProblem(problemId, tagId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProblemResponse>> searchProblems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String difficulty,
            Pageable pageable) {
        Page<ProblemResponse> responses = problemService.searchProblems(keyword, tags, difficulty, pageable);
        return ResponseEntity.ok(responses);
    }
}