package com.problemservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;
    private Long problemId;
    private String userId;
    private String username;
    private String content;
    private Long parentId;
    private List<CommentDTO> replies = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
