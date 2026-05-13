package com.ucto.backend.dto.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplianceOutput {
    @JsonProperty("checksPassed")
    private List<CheckItem> checksPassed = Collections.emptyList();
    @JsonProperty("checksFailed")
    private List<CheckItem> checksFailed = Collections.emptyList();
    private String overallStatus; // pass, pass_with_warnings, fail
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String riskLevel;
    private String reportUrl;
    private String correlationId;
    private String branch;
    private boolean simulation;
    @JsonProperty("findings")
    private List<FindingItem> findings = Collections.emptyList();
    private boolean needsHuman;
    private List<String> humanQuestions = Collections.emptyList();

    public List<CheckItem> getChecksPassed() { return checksPassed; }
    public void setChecksPassed(List<CheckItem> checksPassed) { this.checksPassed = checksPassed; }
    public List<CheckItem> getChecksFailed() { return checksFailed; }
    public void setChecksFailed(List<CheckItem> checksFailed) { this.checksFailed = checksFailed; }
    public String getOverallStatus() { return overallStatus; }
    public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getReportUrl() { return reportUrl; }
    public void setReportUrl(String reportUrl) { this.reportUrl = reportUrl; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public boolean isSimulation() { return simulation; }
    public void setSimulation(boolean simulation) { this.simulation = simulation; }
    public List<FindingItem> getFindings() { return findings; }
    public void setFindings(List<FindingItem> findings) { this.findings = findings; }
    public boolean isNeedsHuman() { return needsHuman; }
    public void setNeedsHuman(boolean needsHuman) { this.needsHuman = needsHuman; }
    public List<String> getHumanQuestions() { return humanQuestions; }
    public void setHumanQuestions(List<String> humanQuestions) { this.humanQuestions = humanQuestions; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CheckItem {
        private String name;
        private String status;
        private String details;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FindingItem {
        private String issue;
        private String impact;
        private String likelihood;
        private String mitigation;
        private String status;
        public String getIssue() { return issue; }
        public void setIssue(String issue) { this.issue = issue; }
        public String getImpact() { return impact; }
        public void setImpact(String impact) { this.impact = impact; }
        public String getLikelihood() { return likelihood; }
        public void setLikelihood(String likelihood) { this.likelihood = likelihood; }
        public String getMitigation() { return mitigation; }
        public void setMitigation(String mitigation) { this.mitigation = mitigation; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
