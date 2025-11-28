package com.judgeservice.client.dto;

public class TestCaseResponse {
    private Long id;
    private String input;
    private String expectedOutput;
    private boolean isSample;
    private int points;
    private Integer timeLimit;
    private Integer memoryLimit;
    private int ordering;

    public TestCaseResponse() {
    }

    public TestCaseResponse(Long id, String input, String expectedOutput, boolean isSample, int points, Integer timeLimit, Integer memoryLimit, int ordering) {
        this.id = id;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.isSample = isSample;
        this.points = points;
        this.timeLimit = timeLimit;
        this.memoryLimit = memoryLimit;
        this.ordering = ordering;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public boolean isSample() {
        return isSample;
    }

    public void setSample(boolean isSample) {
        this.isSample = isSample;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Integer getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(Integer memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }
}

