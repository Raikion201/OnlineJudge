package com.problemservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditorialResponse {
    private Long id;
    private Long problemId;
    private String problemTitle;
    private String content;
    private String approach;
    private String timeComplexity;
    private String spaceComplexity;
    private String solutionCode;
    private String solutionLanguage;
    private String videoUrl;
    private String authorId;
    private String authorName;
    private Boolean isPremium;
    private Integer viewCount;
    private Integer helpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
