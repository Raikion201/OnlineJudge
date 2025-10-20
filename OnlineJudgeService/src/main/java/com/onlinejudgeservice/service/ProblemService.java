package com.onlinejudgeservice.service;

import com.onlinejudgeservice.dto.request.ProblemRequest;
import com.onlinejudgeservice.dto.response.ProblemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProblemService {
    ProblemResponse createProblem(ProblemRequest request);

    ProblemResponse getProblemById(Long id);

    Page<ProblemResponse> getAllProblems(Pageable pageable);

    List<ProblemResponse> getProblemsByTags(List<String> tagSlugs);

    ProblemResponse updateProblem(Long id, ProblemRequest request);

    void deleteProblem(Long id);

    ProblemResponse addTagToProblem(Long problemId, Long tagId);

    ProblemResponse removeTagFromProblem(Long problemId, Long tagId);

    Page<ProblemResponse> searchProblems(String keyword, List<String> tags,
                                         String difficulty, Pageable pageable);
}