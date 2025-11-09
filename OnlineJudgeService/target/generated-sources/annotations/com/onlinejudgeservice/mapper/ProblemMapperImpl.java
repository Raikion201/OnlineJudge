package com.onlinejudgeservice.mapper;

import com.onlinejudgeservice.dto.request.ProblemExampleRequest;
import com.onlinejudgeservice.dto.request.ProblemRequest;
import com.onlinejudgeservice.dto.request.TestCaseRequest;
import com.onlinejudgeservice.dto.response.ProblemExampleResponse;
import com.onlinejudgeservice.dto.response.ProblemResponse;
import com.onlinejudgeservice.dto.response.TagResponse;
import com.onlinejudgeservice.dto.response.TestCaseResponse;
import com.onlinejudgeservice.entity.Problem;
import com.onlinejudgeservice.entity.ProblemExample;
import com.onlinejudgeservice.entity.Tag;
import com.onlinejudgeservice.entity.TestCase;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-25T16:43:43+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class ProblemMapperImpl implements ProblemMapper {

    @Override
    public Problem toEntity(ProblemRequest request) {
        if ( request == null ) {
            return null;
        }

        Problem.ProblemBuilder problem = Problem.builder();

        problem.title( request.getTitle() );
        problem.description( request.getDescription() );
        problem.inputFormat( request.getInputFormat() );
        problem.outputFormat( request.getOutputFormat() );
        problem.constraints( request.getConstraints() );
        problem.difficulty( request.getDifficulty() );
        problem.timeLimit( request.getTimeLimit() );
        problem.memoryLimit( request.getMemoryLimit() );

        return problem.build();
    }

    @Override
    public ProblemResponse toResponse(Problem problem) {
        if ( problem == null ) {
            return null;
        }

        ProblemResponse.ProblemResponseBuilder problemResponse = ProblemResponse.builder();

        problemResponse.id( problem.getId() );
        problemResponse.title( problem.getTitle() );
        problemResponse.description( problem.getDescription() );
        problemResponse.inputFormat( problem.getInputFormat() );
        problemResponse.outputFormat( problem.getOutputFormat() );
        problemResponse.constraints( problem.getConstraints() );
        problemResponse.difficulty( problem.getDifficulty() );
        problemResponse.timeLimit( problem.getTimeLimit() );
        problemResponse.memoryLimit( problem.getMemoryLimit() );
        problemResponse.acceptanceRate( problem.getAcceptanceRate() );
        problemResponse.totalSubmissions( problem.getTotalSubmissions() );
        problemResponse.totalAccepted( problem.getTotalAccepted() );
        problemResponse.createdAt( problem.getCreatedAt() );
        problemResponse.updatedAt( problem.getUpdatedAt() );
        problemResponse.testCases( testCaseSetToTestCaseResponseSet( problem.getTestCases() ) );
        problemResponse.examples( problemExampleSetToProblemExampleResponseSet( problem.getExamples() ) );
        problemResponse.tags( tagSetToTagResponseSet( problem.getTags() ) );

        return problemResponse.build();
    }

    @Override
    public void updateEntity(ProblemRequest request, Problem problem) {
        if ( request == null ) {
            return;
        }

        problem.setTitle( request.getTitle() );
        problem.setDescription( request.getDescription() );
        problem.setInputFormat( request.getInputFormat() );
        problem.setOutputFormat( request.getOutputFormat() );
        problem.setConstraints( request.getConstraints() );
        problem.setDifficulty( request.getDifficulty() );
        problem.setTimeLimit( request.getTimeLimit() );
        problem.setMemoryLimit( request.getMemoryLimit() );
        problem.setPublic( request.isPublic() );
    }

    @Override
    public ProblemExample toExampleEntity(ProblemExampleRequest request) {
        if ( request == null ) {
            return null;
        }

        ProblemExample.ProblemExampleBuilder problemExample = ProblemExample.builder();

        problemExample.input( request.getInput() );
        problemExample.output( request.getOutput() );
        problemExample.explanation( request.getExplanation() );
        problemExample.ordering( request.getOrdering() );

        return problemExample.build();
    }

    @Override
    public ProblemExampleResponse toExampleResponse(ProblemExample example) {
        if ( example == null ) {
            return null;
        }

        ProblemExampleResponse.ProblemExampleResponseBuilder problemExampleResponse = ProblemExampleResponse.builder();

        problemExampleResponse.id( example.getId() );
        problemExampleResponse.input( example.getInput() );
        problemExampleResponse.output( example.getOutput() );
        problemExampleResponse.explanation( example.getExplanation() );
        problemExampleResponse.ordering( example.getOrdering() );
        problemExampleResponse.createdAt( example.getCreatedAt() );

        return problemExampleResponse.build();
    }

    @Override
    public TestCase toTestCaseEntity(TestCaseRequest request) {
        if ( request == null ) {
            return null;
        }

        TestCase.TestCaseBuilder testCase = TestCase.builder();

        testCase.input( request.getInput() );
        testCase.expectedOutput( request.getExpectedOutput() );
        testCase.points( request.getPoints() );
        testCase.timeLimit( request.getTimeLimit() );
        testCase.memoryLimit( request.getMemoryLimit() );
        testCase.ordering( request.getOrdering() );

        return testCase.build();
    }

    @Override
    public TestCaseResponse toTestCaseResponse(TestCase testCase) {
        if ( testCase == null ) {
            return null;
        }

        TestCaseResponse.TestCaseResponseBuilder testCaseResponse = TestCaseResponse.builder();

        testCaseResponse.id( testCase.getId() );
        testCaseResponse.input( testCase.getInput() );
        testCaseResponse.expectedOutput( testCase.getExpectedOutput() );
        testCaseResponse.points( testCase.getPoints() );
        testCaseResponse.timeLimit( testCase.getTimeLimit() );
        testCaseResponse.memoryLimit( testCase.getMemoryLimit() );
        testCaseResponse.ordering( testCase.getOrdering() );
        testCaseResponse.createdAt( testCase.getCreatedAt() );

        return testCaseResponse.build();
    }

    protected Set<TestCaseResponse> testCaseSetToTestCaseResponseSet(Set<TestCase> set) {
        if ( set == null ) {
            return null;
        }

        Set<TestCaseResponse> set1 = LinkedHashSet.newLinkedHashSet( set.size() );
        for ( TestCase testCase : set ) {
            set1.add( toTestCaseResponse( testCase ) );
        }

        return set1;
    }

    protected Set<ProblemExampleResponse> problemExampleSetToProblemExampleResponseSet(Set<ProblemExample> set) {
        if ( set == null ) {
            return null;
        }

        Set<ProblemExampleResponse> set1 = LinkedHashSet.newLinkedHashSet( set.size() );
        for ( ProblemExample problemExample : set ) {
            set1.add( toExampleResponse( problemExample ) );
        }

        return set1;
    }

    protected TagResponse tagToTagResponse(Tag tag) {
        if ( tag == null ) {
            return null;
        }

        TagResponse.TagResponseBuilder tagResponse = TagResponse.builder();

        tagResponse.id( tag.getId() );
        tagResponse.name( tag.getName() );
        tagResponse.slug( tag.getSlug() );
        tagResponse.description( tag.getDescription() );
        tagResponse.createdAt( tag.getCreatedAt() );

        return tagResponse.build();
    }

    protected Set<TagResponse> tagSetToTagResponseSet(Set<Tag> set) {
        if ( set == null ) {
            return null;
        }

        Set<TagResponse> set1 = LinkedHashSet.newLinkedHashSet( set.size() );
        for ( Tag tag : set ) {
            set1.add( tagToTagResponse( tag ) );
        }

        return set1;
    }
}
