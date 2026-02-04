package com.problemservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditorialRequest {

    @NotBlank(message = "Content is required")
    private String content;

    private String approach;

    private String timeComplexity;

    private String spaceComplexity;

    private String solutionCode;

    private String solutionLanguage;

    private String videoUrl;

    private Boolean isPremium;
}
