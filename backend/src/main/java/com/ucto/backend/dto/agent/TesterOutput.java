package com.ucto.backend.dto.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TesterOutput {
    private int testsRun;
    private int testsPassed;
    private int testsFailed;
    private int testsSkipped;
    private double coveragePercent;
    @JsonProperty("failures")
    private List<FailureItem> failures = Collections.emptyList();
    private String overallStatus; // pass, needs_fix, failed
    private boolean doDMet;
    private String storyId;
    private String correlationId;
    private String branch;
    private boolean simulation;

    public int getTestsRun() { return testsRun; }
    public void setTestsRun(int testsRun) { this.testsRun = testsRun; }
    public int getTestsPassed() { return testsPassed; }
    public void setTestsPassed(int testsPassed) { this.testsPassed = testsPassed; }
    public int getTestsFailed() { return testsFailed; }
    public void setTestsFailed(int testsFailed) { this.testsFailed = testsFailed; }
    public int getTestsSkipped() { return testsSkipped; }
    public void setTestsSkipped(int testsSkipped) { this.testsSkipped = testsSkipped; }
    public double getCoveragePercent() { return coveragePercent; }
    public void setCoveragePercent(double coveragePercent) { this.coveragePercent = coveragePercent; }
    public List<FailureItem> getFailures() { return failures; }
    public void setFailures(List<FailureItem> failures) { this.failures = failures; }
    public String getOverallStatus() { return overallStatus; }
    public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
    public boolean isDoDMet() { return doDMet; }
    public void setDoDMet(boolean doDMet) { this.doDMet = doDMet; }
    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public boolean isSimulation() { return simulation; }
    public void setSimulation(boolean simulation) { this.simulation = simulation; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FailureItem {
        private String testCase;
        private String expected;
        private String actual;
        private String assignedTo;
        public String getTestCase() { return testCase; }
        public void setTestCase(String testCase) { this.testCase = testCase; }
        public String getExpected() { return expected; }
        public void setExpected(String expected) { this.expected = expected; }
        public String getActual() { return actual; }
        public void setActual(String actual) { this.actual = actual; }
        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    }
}
