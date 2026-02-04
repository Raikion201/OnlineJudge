package com.problemservice.service.impl;

import com.problemservice.dto.request.EditorialRequest;
import com.problemservice.dto.response.EditorialResponse;
import com.problemservice.entity.Editorial;
import com.problemservice.entity.Problem;
import com.problemservice.exception.BadRequestException;
import com.problemservice.exception.ResourceNotFoundException;
import com.problemservice.repository.EditorialRepository;
import com.problemservice.repository.ProblemRepository;
import com.problemservice.service.EditorialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EditorialServiceImpl implements EditorialService {

    private final EditorialRepository editorialRepository;
    private final ProblemRepository problemRepository;

    @Override
    @Transactional
    public EditorialResponse getEditorial(Long problemId) {
        log.info("Getting editorial for problem {}", problemId);

        Editorial editorial = editorialRepository.findByProblemId(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Editorial not found for problem: " + problemId));

        // Increment view count
        editorialRepository.incrementViewCount(problemId);

        return mapToResponse(editorial);
    }

    @Override
    @Transactional
    public EditorialResponse createEditorial(Long problemId, EditorialRequest request,
                                             String authorId, String authorName) {
        log.info("Creating editorial for problem {}", problemId);

        // Verify problem exists
        if (!problemRepository.existsById(problemId)) {
            throw new ResourceNotFoundException("Problem not found: " + problemId);
        }

        // Check if editorial already exists
        if (editorialRepository.existsByProblemId(problemId)) {
            throw new BadRequestException("Editorial already exists for problem: " + problemId);
        }

        Editorial editorial = Editorial.builder()
                .problemId(problemId)
                .content(request.getContent())
                .approach(request.getApproach())
                .timeComplexity(request.getTimeComplexity())
                .spaceComplexity(request.getSpaceComplexity())
                .solutionCode(request.getSolutionCode())
                .solutionLanguage(request.getSolutionLanguage())
                .videoUrl(request.getVideoUrl())
                .authorId(authorId)
                .authorName(authorName)
                .isPremium(request.getIsPremium() != null ? request.getIsPremium() : false)
                .build();

        Editorial saved = editorialRepository.save(editorial);
        log.info("Editorial created with id {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EditorialResponse updateEditorial(Long problemId, EditorialRequest request) {
        log.info("Updating editorial for problem {}", problemId);

        Editorial editorial = editorialRepository.findByProblemId(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Editorial not found for problem: " + problemId));

        if (request.getContent() != null) {
            editorial.setContent(request.getContent());
        }
        if (request.getApproach() != null) {
            editorial.setApproach(request.getApproach());
        }
        if (request.getTimeComplexity() != null) {
            editorial.setTimeComplexity(request.getTimeComplexity());
        }
        if (request.getSpaceComplexity() != null) {
            editorial.setSpaceComplexity(request.getSpaceComplexity());
        }
        if (request.getSolutionCode() != null) {
            editorial.setSolutionCode(request.getSolutionCode());
        }
        if (request.getSolutionLanguage() != null) {
            editorial.setSolutionLanguage(request.getSolutionLanguage());
        }
        if (request.getVideoUrl() != null) {
            editorial.setVideoUrl(request.getVideoUrl());
        }
        if (request.getIsPremium() != null) {
            editorial.setIsPremium(request.getIsPremium());
        }

        Editorial updated = editorialRepository.save(editorial);
        log.info("Editorial updated for problem {}", problemId);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteEditorial(Long problemId) {
        log.info("Deleting editorial for problem {}", problemId);

        if (!editorialRepository.existsByProblemId(problemId)) {
            throw new ResourceNotFoundException("Editorial not found for problem: " + problemId);
        }

        editorialRepository.deleteByProblemId(problemId);
        log.info("Editorial deleted for problem {}", problemId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEditorial(Long problemId) {
        return editorialRepository.existsByProblemId(problemId);
    }

    @Override
    @Transactional
    public void markHelpful(Long problemId) {
        if (!editorialRepository.existsByProblemId(problemId)) {
            throw new ResourceNotFoundException("Editorial not found for problem: " + problemId);
        }
        editorialRepository.incrementHelpfulCount(problemId);
    }

    private EditorialResponse mapToResponse(Editorial editorial) {
        String problemTitle = null;
        try {
            Problem problem = problemRepository.findById(editorial.getProblemId()).orElse(null);
            if (problem != null) {
                problemTitle = problem.getTitle();
            }
        } catch (Exception e) {
            log.warn("Could not fetch problem title for editorial", e);
        }

        return EditorialResponse.builder()
                .id(editorial.getId())
                .problemId(editorial.getProblemId())
                .problemTitle(problemTitle)
                .content(editorial.getContent())
                .approach(editorial.getApproach())
                .timeComplexity(editorial.getTimeComplexity())
                .spaceComplexity(editorial.getSpaceComplexity())
                .solutionCode(editorial.getSolutionCode())
                .solutionLanguage(editorial.getSolutionLanguage())
                .videoUrl(editorial.getVideoUrl())
                .authorId(editorial.getAuthorId())
                .authorName(editorial.getAuthorName())
                .isPremium(editorial.getIsPremium())
                .viewCount(editorial.getViewCount())
                .helpfulCount(editorial.getHelpfulCount())
                .createdAt(editorial.getCreatedAt())
                .updatedAt(editorial.getUpdatedAt())
                .build();
    }
}
