package com.problemservice.mapper;

import com.problemservice.dto.request.ProblemExampleRequest;
import com.problemservice.dto.request.ProblemRequest;
import com.problemservice.dto.request.TestCaseRequest;
import com.problemservice.dto.response.ProblemExampleResponse;
import com.problemservice.dto.response.ProblemResponse;
import com.problemservice.dto.response.TestCaseResponse;
import com.problemservice.entity.Problem;
import com.problemservice.entity.ProblemExample;
import com.problemservice.entity.TestCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProblemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "acceptanceRate", ignore = true)
    @Mapping(target = "totalSubmissions", ignore = true)
    @Mapping(target = "totalAccepted", ignore = true)
    @Mapping(target = "testCases", ignore = true)
    @Mapping(target = "examples", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Problem toEntity(ProblemRequest request);

    ProblemResponse toResponse(Problem problem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "acceptanceRate", ignore = true)
    @Mapping(target = "totalSubmissions", ignore = true)
    @Mapping(target = "totalAccepted", ignore = true)
    @Mapping(target = "testCases", ignore = true)
    @Mapping(target = "examples", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateEntity(ProblemRequest request, @MappingTarget Problem problem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "problem", ignore = true)
    ProblemExample toExampleEntity(ProblemExampleRequest request);

    ProblemExampleResponse toExampleResponse(ProblemExample example);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "problem", ignore = true)
    TestCase toTestCaseEntity(TestCaseRequest request);

    TestCaseResponse toTestCaseResponse(TestCase testCase);
}
