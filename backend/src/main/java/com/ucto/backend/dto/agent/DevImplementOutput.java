package com.ucto.backend.dto.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DevImplementOutput {

    @JsonProperty("filesToChange")
    private List<FileChangeItem> filesToChange = Collections.emptyList();

    private String rationale;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String storyId;
    private List<String> acAddressed = Collections.emptyList();
    private String changesSummary;
    private double testCoverage;
    private boolean needsHuman;
    private List<String> humanQuestions = Collections.emptyList();

    public List<FileChangeItem> getFilesToChange() { return filesToChange; }
    public void setFilesToChange(List<FileChangeItem> filesToChange) { this.filesToChange = filesToChange; }
    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }
    public List<String> getAcAddressed() { return acAddressed; }
    public void setAcAddressed(List<String> acAddressed) { this.acAddressed = acAddressed; }
    public String getChangesSummary() { return changesSummary; }
    public void setChangesSummary(String changesSummary) { this.changesSummary = changesSummary; }
    public double getTestCoverage() { return testCoverage; }
    public void setTestCoverage(double testCoverage) { this.testCoverage = testCoverage; }
    public boolean isNeedsHuman() { return needsHuman; }
    public void setNeedsHuman(boolean needsHuman) { this.needsHuman = needsHuman; }
    public List<String> getHumanQuestions() { return humanQuestions; }
    public void setHumanQuestions(List<String> humanQuestions) { this.humanQuestions = humanQuestions; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileChangeItem {
        private String path;
        private String action; // CREATE, MODIFY, DELETE
        private String summary;
        private String riskLevel;
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }
}
