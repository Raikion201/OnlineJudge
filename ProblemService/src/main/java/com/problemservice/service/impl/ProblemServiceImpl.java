package com.problemservice.service.impl;

import com.problemservice.dto.request.ProblemExampleRequest;
import com.problemservice.dto.request.ProblemRequest;
import com.problemservice.dto.request.TestCaseRequest;
import com.problemservice.dto.response.ProblemResponse;
import com.problemservice.entity.Problem;
import com.problemservice.entity.ProblemExample;
import com.problemservice.entity.Tag;
import com.problemservice.entity.TestCase;
import com.problemservice.exception.BadRequestException;
import com.problemservice.exception.ResourceNotFoundException;
import com.problemservice.repository.ProblemRepository;
import com.problemservice.repository.TagRepository;
import com.problemservice.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public ProblemResponse createProblem(ProblemRequest request) {
        // Validate request
        if (request == null) {
            throw new BadRequestException("Problem request cannot be null");
        }

        // Create and save the Problem entity
        Problem problem = convertToProblem(request);

        // Process tags
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Long> tagIds = new HashSet<>(request.getTagIds());
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));

            // Check if all requested tags were found
            if (tags.size() != request.getTagIds().size()) {
                throw new ResourceNotFoundException("One or more tags not found");
            }

            problem.setTags(tags);
        }

        // Save the problem first to get an ID
        problem = problemRepository.save(problem);

        // Handle test cases
        if (request.getTestCases() != null) {
            Set<TestCase> testCases = new HashSet<>();
            for (TestCaseRequest testCaseRequest : request.getTestCases()) {
                TestCase testCase = TestCase.builder()
                        .problem(problem)
                        .input(testCaseRequest.getInput())
                        .expectedOutput(testCaseRequest.getExpectedOutput())
                        .isSample(testCaseRequest.isSample())
                        .points(testCaseRequest.getPoints())
                        .timeLimit(testCaseRequest.getTimeLimit())
                        .memoryLimit(testCaseRequest.getMemoryLimit())
                        .ordering(testCaseRequest.getOrdering())
                        .build();
                testCases.add(testCase);
            }
            problem.setTestCases(testCases);
        }

        // Handle examples
        if (request.getExamples() != null) {
            Set<ProblemExample> examples = new HashSet<>();
            for (ProblemExampleRequest exampleRequest : request.getExamples()) {
                ProblemExample example = ProblemExample.builder()
                        .problem(problem)
                        .input(exampleRequest.getInput())
                        .output(exampleRequest.getOutput())
                        .explanation(exampleRequest.getExplanation())
                        .ordering(exampleRequest.getOrdering())
                        .build();
                examples.add(example);
            }
            problem.setExamples(examples);
        }

        // Save again with all relationships
        problem = problemRepository.save(problem);
        return convertToProblemResponse(problem);
    }

    @Override
    @Transactional(readOnly = true)
    public ProblemResponse getProblemById(Long id) {
        if (id == null) {
            throw new BadRequestException("Problem ID cannot be null");
        }

        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        return convertToProblemResponse(problem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProblemResponse> getAllProblems(Pageable pageable) {
        if (pageable == null) {
            throw new BadRequestException("Pageable cannot be null");
        }

        return problemRepository.findAll(pageable)
                .map(this::convertToProblemResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProblemResponse> getProblemsByTags(List<String> tagSlugs) {
        if (tagSlugs == null || tagSlugs.isEmpty()) {
            throw new BadRequestException("Tag slugs cannot be null or empty");
        }

        // Since findAllBySlugsIn doesn't exist, we need to implement a workaround
        // First, update the TagRepository interface to include this method
        List<Tag> tags = new ArrayList<>(); // This will need proper implementation

        if (tags.isEmpty()) {
            throw new ResourceNotFoundException("No tags found with the provided slugs");
        }

        // This method also needs to be added to ProblemRepository
        List<Problem> problems = new ArrayList<>(); // This will need proper implementation

        return problems.stream()
                .map(this::convertToProblemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProblemResponse updateProblem(Long id, ProblemRequest request) {
        if (id == null) {
            throw new BadRequestException("Problem ID cannot be null");
        }

        if (request == null) {
            throw new BadRequestException("Problem request cannot be null");
        }

        Problem existingProblem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        // Update basic fields
        existingProblem.setTitle(request.getTitle());
        existingProblem.setDescription(request.getDescription());
        existingProblem.setInput_format(request.getInputFormat());
        existingProblem.setOutput_format(request.getOutputFormat());
        existingProblem.setConstraints(request.getConstraints());
        existingProblem.setDifficulty(request.getDifficulty());
        existingProblem.setTime_limit(request.getTimeLimit());
        existingProblem.setMemory_limit(request.getMemoryLimit());
        existingProblem.setPublic(request.isPublic());

        // Update tags
        if (request.getTagIds() != null) {
            // Cast to Integer since TagRepository uses Integer as ID type
            Set<Long> tagIds = new HashSet<>(request.getTagIds());

            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));

            // Check if all requested tags were found
            if (!request.getTagIds().isEmpty() && tags.size() != request.getTagIds().size()) {
                throw new ResourceNotFoundException("One or more tags not found");
            }

            existingProblem.setTags(tags);
        }

        // Update test cases - remove existing and add new ones
        existingProblem.getTestCases().clear();
        if (request.getTestCases() != null) {
            for (TestCaseRequest testCaseRequest : request.getTestCases()) {
                TestCase testCase = TestCase.builder()
                        .problem(existingProblem)
                        .input(testCaseRequest.getInput())
                        .expectedOutput(testCaseRequest.getExpectedOutput())
                        .isSample(testCaseRequest.isSample()) // Changed from sample to isSample
                        .points(testCaseRequest.getPoints())
                        .timeLimit(testCaseRequest.getTimeLimit())
                        .memoryLimit(testCaseRequest.getMemoryLimit())
                        .ordering(testCaseRequest.getOrdering())
                        .build();
                existingProblem.getTestCases().add(testCase);
            }
        }

        // Update examples - remove existing and add new ones
        existingProblem.getExamples().clear();
        if (request.getExamples() != null) {
            for (ProblemExampleRequest exampleRequest : request.getExamples()) {
                ProblemExample example = ProblemExample.builder()
                        .problem(existingProblem)
                        .input(exampleRequest.getInput())
                        .output(exampleRequest.getOutput())
                        .explanation(exampleRequest.getExplanation())
                        .ordering(exampleRequest.getOrdering())
                        .build();
                existingProblem.getExamples().add(example);
            }
        }

        existingProblem = problemRepository.save(existingProblem);
        return convertToProblemResponse(existingProblem);
    }

    @Override
    @Transactional
    public void deleteProblem(Long id) {
        if (id == null) {
            throw new BadRequestException("Problem ID cannot be null");
        }

        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        problemRepository.delete(problem);
    }

    @Override
    @Transactional
    public ProblemResponse addTagToProblem(Long problemId, Long tagId) {
        if (problemId == null) {
            throw new BadRequestException("Problem ID cannot be null");
        }

        if (tagId == null) {
            throw new BadRequestException("Tag ID cannot be null");
        }

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + problemId));

        // Convert Long to Integer for TagRepository
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        problem.getTags().add(tag);
        problem = problemRepository.save(problem);

        return convertToProblemResponse(problem);
    }

    @Override
    @Transactional
    public ProblemResponse removeTagFromProblem(Long problemId, Long tagId) {
        if (problemId == null) {
            throw new BadRequestException("Problem ID cannot be null");
        }

        if (tagId == null) {
            throw new BadRequestException("Tag ID cannot be null");
        }

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + problemId));

        // Check if tag exists before removal
        boolean tagExists = problem.getTags().removeIf(tag -> tag.getId().equals(tagId));

        if (!tagExists) {
            throw new ResourceNotFoundException("Problem does not have tag with id: " + tagId);
        }

        problem = problemRepository.save(problem);
        return convertToProblemResponse(problem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProblemResponse> searchProblems(String keyword, List<String> tags,
                                                String difficulty, Pageable pageable) {
        if (pageable == null) {
            throw new BadRequestException("Pageable cannot be null");
        }

        // For Specification to work, ProblemRepository needs to extend JpaSpecificationExecutor
        // For now, use a simpler approach with available methods
        Page<Problem> problems = problemRepository.findAll(pageable);

        // Filter results manually (temporary solution)
        // In production, implement a proper specification-based search

        return problems.map(this::convertToProblemResponse);
    }

    // Conversion methods
    private Problem convertToProblem(ProblemRequest request) {
        return Problem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .input_format(request.getInputFormat())  // Match entity field name
                .output_format(request.getOutputFormat()) // Match entity field name
                .constraints(request.getConstraints())
                .difficulty(request.getDifficulty())
                .time_limit(request.getTimeLimit())  // Match entity field name
                .memory_limit(request.getMemoryLimit()) // Match entity field name
                .isPublic(request.isPublic())
                .build();
    }

    private ProblemResponse convertToProblemResponse(Problem problem) {
        // Convert test cases
        Set<com.problemservice.dto.response.TestCaseResponse> testCaseResponses = problem.getTestCases() != null ?
                problem.getTestCases().stream()
                        .map(tc -> com.problemservice.dto.response.TestCaseResponse.builder()
                                .id(tc.getId())
                                .input(tc.getInput())
                                .expectedOutput(tc.getExpectedOutput())
                                .isSample(tc.isSample())
                                .points(tc.getPoints())
                                .timeLimit(tc.getTimeLimit())
                                .memoryLimit(tc.getMemoryLimit())
                                .ordering(tc.getOrdering())
                                .createdAt(tc.getCreatedAt())
                                .build())
                        .collect(Collectors.toSet()) : new HashSet<>();

        // Convert examples
        Set<com.problemservice.dto.response.ProblemExampleResponse> exampleResponses = problem.getExamples() != null ?
                problem.getExamples().stream()
                        .map(ex -> com.problemservice.dto.response.ProblemExampleResponse.builder()
                                .id(ex.getId())
                                .input(ex.getInput())
                                .output(ex.getOutput())
                                .explanation(ex.getExplanation())
                                .ordering(ex.getOrdering())
                                .createdAt(ex.getCreatedAt())
                                .build())
                        .collect(Collectors.toSet()) : new HashSet<>();

        // Convert tags
        Set<com.problemservice.dto.response.TagResponse> tagResponses = problem.getTags() != null ?
                problem.getTags().stream()
                        .map(tag -> com.problemservice.dto.response.TagResponse.builder()
                                .id(tag.getId())
                                .name(tag.getName())
                                .slug(tag.getSlug())
                                .description(tag.getDescription())
                                .createdAt(tag.getCreatedAt())
                                .build())
                        .collect(Collectors.toSet()) : new HashSet<>();

        return ProblemResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .inputFormat(problem.getInput_format())  // Match entity field name
                .outputFormat(problem.getOutput_format()) // Match entity field name
                .constraints(problem.getConstraints())
                .difficulty(problem.getDifficulty())
                .timeLimit(problem.getTime_limit())  // Match entity field name
                .memoryLimit(problem.getMemory_limit()) // Match entity field name
                .isPublic(problem.isPublic())
                .acceptanceRate(calculateAcceptanceRate(problem))
                .totalSubmissions(problem.getTotalSubmissions())
                .totalAccepted(problem.getTotalAccepted())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .testCases(testCaseResponses)
                .examples(exampleResponses)
                .tags(tagResponses)
                .build();
    }

    private double calculateAcceptanceRate(Problem problem) {
        if (problem.getTotalSubmissions() == 0) {
            return 0.0;
        }
        return (double) problem.getTotalAccepted() / problem.getTotalSubmissions() * 100;
    }
}