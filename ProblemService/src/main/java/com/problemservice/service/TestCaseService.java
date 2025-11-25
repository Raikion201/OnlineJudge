package com.problemservice.service;

import com.problemservice.dto.request.ProblemExampleRequest;
import com.problemservice.dto.request.TestCaseRequest;
import com.problemservice.dto.response.ProblemExampleResponse;
import com.problemservice.dto.response.TestCaseResponse;

import java.util.List;

public interface TestCaseService {
    // Example methods
    ProblemExampleResponse addExample(Long problemId, ProblemExampleRequest request);
    List<ProblemExampleResponse> getExamples(Long problemId);
    ProblemExampleResponse updateExample(Long problemId, Long exampleId, ProblemExampleRequest request);
    void deleteExample(Long problemId, Long exampleId);

    // TestCase methods
    TestCaseResponse addTestCase(Long problemId, TestCaseRequest request);
    List<TestCaseResponse> getTestCases(Long problemId);
    TestCaseResponse updateTestCase(Long problemId, Long testCaseId, TestCaseRequest request);
    void deleteTestCase(Long problemId, Long testCaseId);
}
