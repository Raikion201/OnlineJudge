package com.problemservice.service.impl;

import com.problemservice.dto.request.ProblemExampleRequest;
import com.problemservice.dto.request.TestCaseRequest;
import com.problemservice.dto.response.ProblemExampleResponse;
import com.problemservice.dto.response.TestCaseResponse;
import com.problemservice.entity.Problem;
import com.problemservice.entity.ProblemExample;
import com.problemservice.entity.TestCase;
import com.problemservice.exception.ResourceNotFoundException;
import com.problemservice.mapper.ProblemMapper;
import com.problemservice.repository.ProblemExampleRepository;
import com.problemservice.repository.ProblemRepository;
import com.problemservice.repository.TestCaseRepository;
import com.problemservice.service.TestCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {
    private final ProblemRepository problemRepository;
    private final ProblemExampleRepository exampleRepository;
    private final TestCaseRepository testCaseRepository;
    private final ProblemMapper mapper;

    @Override
    @Transactional
    public ProblemExampleResponse addExample(Long problemId, ProblemExampleRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + problemId));

        ProblemExample example = mapper.toExampleEntity(request);
        example.setProblem(problem);
        
        ProblemExample savedExample = exampleRepository.save(example);
        return mapper.toExampleResponse(savedExample);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProblemExampleResponse> getExamples(Long problemId) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResourceNotFoundException("Problem not found with id: " + problemId);
        }
        
        return exampleRepository.findByProblemIdOrderByOrderingAsc(problemId)
                .stream()
                .map(mapper::toExampleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProblemExampleResponse updateExample(Long problemId, Long exampleId, ProblemExampleRequest request) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResourceNotFoundException("Problem not found with id: " + problemId);
        }

        ProblemExample example = exampleRepository.findById(exampleId)
                .orElseThrow(() -> new ResourceNotFoundException("Example not found with id: " + exampleId));

        if (!example.getProblem().getId().equals(problemId)) {
            throw new IllegalArgumentException("Example does not belong to this problem");
        }

        example.setInput(request.getInput());
        example.setOutput(request.getOutput());
        example.setExplanation(request.getExplanation());
        example.setOrdering(request.getOrdering());

        ProblemExample updatedExample = exampleRepository.save(example);
        return mapper.toExampleResponse(updatedExample);
    }

    @Override
    @Transactional
    public void deleteExample(Long problemId, Long exampleId) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResourceNotFoundException("Problem not found with id: " + problemId);
        }

        ProblemExample example = exampleRepository.findById(exampleId)
                .orElseThrow(() -> new ResourceNotFoundException("Example not found with id: " + exampleId));

        if (!example.getProblem().getId().equals(problemId)) {
            throw new IllegalArgumentException("Example does not belong to this problem");
        }

        exampleRepository.delete(example);
    }

    @Override
    @Transactional
    public TestCaseResponse addTestCase(Long problemId, TestCaseRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + problemId));

        TestCase testCase = mapper.toTestCaseEntity(request);
        testCase.setProblem(problem);
        
        TestCase savedTestCase = testCaseRepository.save(testCase);
        return mapper.toTestCaseResponse(savedTestCase);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestCaseResponse> getTestCases(Long problemId) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResourceNotFoundException("Problem not found with id: " + problemId);
        }
        
        return testCaseRepository.findByProblemIdOrderByOrderingAsc(problemId)
                .stream()
                .map(mapper::toTestCaseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TestCaseResponse updateTestCase(Long problemId, Long testCaseId, TestCaseRequest request) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResourceNotFoundException("Problem not found with id: " + problemId);
        }

        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("TestCase not found with id: " + testCaseId));

        if (!testCase.getProblem().getId().equals(problemId)) {
            throw new IllegalArgumentException("TestCase does not belong to this problem");
        }

        testCase.setInput(request.getInput());
        testCase.setExpectedOutput(request.getExpectedOutput());
        testCase.setSample(request.isSample());
        testCase.setPoints(request.getPoints());
        testCase.setTimeLimit(request.getTimeLimit());
        testCase.setMemoryLimit(request.getMemoryLimit());
        testCase.setOrdering(request.getOrdering());

        TestCase updatedTestCase = testCaseRepository.save(testCase);
        return mapper.toTestCaseResponse(updatedTestCase);
    }

    @Override
    @Transactional
    public void deleteTestCase(Long problemId, Long testCaseId) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResourceNotFoundException("Problem not found with id: " + problemId);
        }

        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("TestCase not found with id: " + testCaseId));

        if (!testCase.getProblem().getId().equals(problemId)) {
            throw new IllegalArgumentException("TestCase does not belong to this problem");
        }

        testCaseRepository.delete(testCase);
    }
}
