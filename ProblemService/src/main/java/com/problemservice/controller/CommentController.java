package com.problemservice.controller;

import com.problemservice.dto.CommentDTO;
import com.problemservice.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/problems/{problemId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<Page<CommentDTO>> getComments(
            @PathVariable Long problemId,
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getCommentsByProblemId(problemId, pageable));
    }

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long problemId,
            @RequestBody CommentDTO commentDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Name", required = false) String username) {
        
        System.out.println("AddComment called. ProblemId: " + problemId);
        System.out.println("X-User-Id: " + userId);
        System.out.println("X-User-Name: " + username);

        if (userId == null) {
            // Fallback or throw error if auth is required
             throw new RuntimeException("User not authenticated - Missing X-User-Id header");
        }

        commentDTO.setProblemId(problemId);
        commentDTO.setUserId(userId);
        commentDTO.setUsername(username != null ? username : "Anonymous");
        
        return ResponseEntity.ok(commentService.addComment(commentDTO));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long problemId,
            @PathVariable Long commentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        
        boolean isAdmin = roles != null && roles.contains("Admin"); // Simplified role check
        commentService.deleteComment(commentId, userId, isAdmin);
        return ResponseEntity.ok().build();
    }
}
