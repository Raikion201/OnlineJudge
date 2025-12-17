package com.judgeservice.client.dto;

import java.util.Set;

public class ProblemResponse {
    private Long id;
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private String difficulty;
    private int timeLimit;
    private int memoryLimit;
    private boolean isPublic;
    private double acceptanceRate;
    private int totalSubmissions;
    private int totalAccepted;
    private Set<TestCaseResponse> testCases;

    // LeetCode-style function metadata
    private String functionName;
    private String functionSignature;
    private String codeTemplate;

    public ProblemResponse() {
    }

    public ProblemResponse(Long id, String title, String description, String inputFormat, String outputFormat, String constraints, String difficulty, int timeLimit, int memoryLimit, boolean isPublic, double acceptanceRate, int totalSubmissions, int totalAccepted, Set<TestCaseResponse> testCases) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
        this.constraints = constraints;
        this.difficulty = difficulty;
        this.timeLimit = timeLimit;
        this.memoryLimit = memoryLimit;
        this.isPublic = isPublic;
        this.acceptanceRate = acceptanceRate;
        this.totalSubmissions = totalSubmissions;
        this.totalAccepted = totalAccepted;
        this.testCases = testCases;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public int getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(int totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public int getTotalAccepted() {
        return totalAccepted;
    }

    public void setTotalAccepted(int totalAccepted) {
        this.totalAccepted = totalAccepted;
    }

    public Set<TestCaseResponse> getTestCases() {
        return testCases;
    }

    public void setTestCases(Set<TestCaseResponse> testCases) {
        this.testCases = testCases;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionSignature() {
        return functionSignature;
    }

    public void setFunctionSignature(String functionSignature) {
        this.functionSignature = functionSignature;
    }

    public String getCodeTemplate() {
        return codeTemplate;
    }

    public void setCodeTemplate(String codeTemplate) {
        this.codeTemplate = codeTemplate;
    }
}

