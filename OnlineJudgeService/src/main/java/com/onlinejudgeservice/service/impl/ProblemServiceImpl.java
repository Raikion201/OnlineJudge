package com.onlinejudgeservice.service.impl;

import com.onlinejudgeservice.dto.request.ProblemRequest;
import com.onlinejudgeservice.dto.response.ProblemResponse;
import com.onlinejudgeservice.entity.Problem;
import com.onlinejudgeservice.entity.ProblemExample;
import com.onlinejudgeservice.entity.Tag;
import com.onlinejudgeservice.entity.TestCase;
import com.onlinejudgeservice.enums.Difficulty;
import com.onlinejudgeservice.exception.ResourceNotFoundException;
import com.onlinejudgeservice.mapper.ProblemMapper;
import com.onlinejudgeservice.repository.ProblemRepository;
import com.onlinejudgeservice.repository.TagRepository;
import com.onlinejudgeservice.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final TagRepository tagRepository;
    private final ProblemMapper problemMapper;

    @Override
    public ProblemResponse createProblem(ProblemRequest request) {
        // Map request to entity
        Problem problem = problemMapper.toEntity(request);

        // Handle TestCases
        if (request.getTestCases() != null && !request.getTestCases().isEmpty()) {
            Set<TestCase> testCases = request.getTestCases().stream()
                    .map(testCaseRequest -> {
                        TestCase testCase = problemMapper.toTestCaseEntity(testCaseRequest);
                        testCase.setProblem(problem); // Set bidirectional relationship
                        return testCase;
                    })
                    .collect(Collectors.toSet());
            problem.setTestCases(testCases);
        }

        // Handle Examples
        if (request.getExamples() != null && !request.getExamples().isEmpty()) {
            Set<ProblemExample> examples = request.getExamples().stream()
                    .map(exampleRequest -> {
                        ProblemExample example = problemMapper.toExampleEntity(exampleRequest);
                        example.setProblem(problem); // Set bidirectional relationship
                        return example;
                    })
                    .collect(Collectors.toSet());
            problem.setExamples(examples);
        }

        // Save problem
        Problem savedProblem = problemRepository.save(problem);

        return problemMapper.toResponse(savedProblem);
    }

    @Override
    @Transactional(readOnly = true)
    public ProblemResponse getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));
        return problemMapper.toResponse(problem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProblemResponse> getAllProblems(Pageable pageable) {
        return problemRepository.findAll(pageable)
                .map(problemMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProblemResponse> getProblemsByTags(List<String> tagSlugs) {
        List<Problem> problems = problemRepository.findByTags_SlugIn(tagSlugs);
        return problems.stream()
                .map(problemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProblemResponse updateProblem(Long id, ProblemRequest request) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));
        problemMapper.updateEntity(request, problem);
        Problem updatedProblem = problemRepository.save(problem);
        return problemMapper.toResponse(updatedProblem);
    }

    @Override
    public void deleteProblem(Long id) {
        if (!problemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Problem not found with id: " + id);
        }
        problemRepository.deleteById(id);
    }

    @Override
    public ProblemResponse addTagToProblem(Long problemId, Long tagId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + problemId));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        problem.getTags().add(tag);
        Problem savedProblem = problemRepository.save(problem);
        return problemMapper.toResponse(savedProblem);
    }

    @Override
    public ProblemResponse removeTagFromProblem(Long problemId, Long tagId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + problemId));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        problem.getTags().remove(tag);
        Problem savedProblem = problemRepository.save(problem);
        return problemMapper.toResponse(savedProblem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProblemResponse> searchProblems(String keyword, List<String> tags,
                                                String difficulty, Pageable pageable) {
        Difficulty difficultyEnum = difficulty != null ? Difficulty.valueOf(difficulty.toUpperCase()) : null;

        Page<Problem> problems = problemRepository.searchProblems(keyword, tags, difficultyEnum, pageable);
        return problems.map(problemMapper::toResponse);
    }
}