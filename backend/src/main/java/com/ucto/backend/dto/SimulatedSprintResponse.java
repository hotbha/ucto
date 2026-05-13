package com.ucto.backend.dto;

import com.ucto.backend.service.QualityGateService.GateStatusDTO;

import java.util.List;

public class SimulatedSprintResponse {
    private Long projectId;
    private String branch;
    private String changeDescription;
    private String correlationId;
    private List<StepResult> steps;
    private GateStatusDTO gateStatus;
    private TestResultSummary testResult;
    private ComplianceResultSummary complianceResult;
    private BaSummary baSummary;
    private ArchitectSummary architectSummary;
    private DevSummary devSummary;
    private boolean dbError;
    private String dbErrorMessage;


    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public List<StepResult> getSteps() { return steps; }
    public void setSteps(List<StepResult> steps) { this.steps = steps; }
    public GateStatusDTO getGateStatus() { return gateStatus; }
    public void setGateStatus(GateStatusDTO gateStatus) { this.gateStatus = gateStatus; }
    public TestResultSummary getTestResult() { return testResult; }
    public void setTestResult(TestResultSummary testResult) { this.testResult = testResult; }
    public ComplianceResultSummary getComplianceResult() { return complianceResult; }
    public void setComplianceResult(ComplianceResultSummary complianceResult) { this.complianceResult = complianceResult; }
    public BaSummary getBaSummary() { return baSummary; }
    public void setBaSummary(BaSummary baSummary) { this.baSummary = baSummary; }
    public ArchitectSummary getArchitectSummary() { return architectSummary; }
    public void setArchitectSummary(ArchitectSummary architectSummary) { this.architectSummary = architectSummary; }
    public DevSummary getDevSummary() { return devSummary; }
    public void setDevSummary(DevSummary devSummary) { this.devSummary = devSummary; }
    public boolean isDbError() { return dbError; }
    public void setDbError(boolean dbError) { this.dbError = dbError; }
    public String getDbErrorMessage() { return dbErrorMessage; }
    public void setDbErrorMessage(String dbErrorMessage) { this.dbErrorMessage = dbErrorMessage; }

    public static class StepResult {

        private String agent; private String status; private boolean simulation; private String eventId;
        public StepResult() {}
        public StepResult(String agent, String status, boolean simulation, String eventId) {
            this.agent = agent; this.status = status; this.simulation = simulation; this.eventId = eventId;
        }
        public String getAgent() { return agent; }
        public void setAgent(String agent) { this.agent = agent; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isSimulation() { return simulation; }
        public void setSimulation(boolean simulation) { this.simulation = simulation; }
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
    }

    public static class TestResultSummary {
        private int testsRun; private int testsPassed; private int testsFailed;
        private double coveragePercent; private String status; private Long id;
        public int getTestsRun() { return testsRun; }
        public void setTestsRun(int testsRun) { this.testsRun = testsRun; }
        public int getTestsPassed() { return testsPassed; }
        public void setTestsPassed(int testsPassed) { this.testsPassed = testsPassed; }
        public int getTestsFailed() { return testsFailed; }
        public void setTestsFailed(int testsFailed) { this.testsFailed = testsFailed; }
        public double getCoveragePercent() { return coveragePercent; }
        public void setCoveragePercent(double coveragePercent) { this.coveragePercent = coveragePercent; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class ComplianceResultSummary {
        private String overallStatus; private String severity; private Long id;
        public String getOverallStatus() { return overallStatus; }
        public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class BaSummary {
        private int requirementCount;
        private String requirementsSummary;
        private int acceptanceCriteriaCount;
        public int getRequirementCount() { return requirementCount; }
        public void setRequirementCount(int requirementCount) { this.requirementCount = requirementCount; }
        public String getRequirementsSummary() { return requirementsSummary; }
        public void setRequirementsSummary(String requirementsSummary) { this.requirementsSummary = requirementsSummary; }
        public int getAcceptanceCriteriaCount() { return acceptanceCriteriaCount; }
        public void setAcceptanceCriteriaCount(int acceptanceCriteriaCount) { this.acceptanceCriteriaCount = acceptanceCriteriaCount; }
    }

    public static class ArchitectSummary {
        private int componentCount;
        private String componentsSummary;
        private boolean feasible;
        private String risks;
        public int getComponentCount() { return componentCount; }
        public void setComponentCount(int componentCount) { this.componentCount = componentCount; }
        public String getComponentsSummary() { return componentsSummary; }
        public void setComponentsSummary(String componentsSummary) { this.componentsSummary = componentsSummary; }
        public boolean isFeasible() { return feasible; }
        public void setFeasible(boolean feasible) { this.feasible = feasible; }
        public String getRisks() { return risks; }
        public void setRisks(String risks) { this.risks = risks; }
    }

    public static class DevSummary {
        private int fileChangeCount;
        private String filesSummary;
        private String rationale;
        private String riskLevel;
        public int getFileChangeCount() { return fileChangeCount; }
        public void setFileChangeCount(int fileChangeCount) { this.fileChangeCount = fileChangeCount; }
        public String getFilesSummary() { return filesSummary; }
        public void setFilesSummary(String filesSummary) { this.filesSummary = filesSummary; }
        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }
}
