package com.ucto.backend.dto.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runnable JUnit tests for all 5 agent DTO JSON parsing paths.
 * Covers valid JSON, malformed JSON, and unknown-field tolerance.
 */
class AgentDtoParsingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // ── BaRequirementsOutput ──

    @Test
    void baRequirements_validJson_parsesCorrectly() throws Exception {
        String json = """
                {"requirements":[{"id":"REQ-1","area":"Auth","description":"User login","risk":"High","qualityAttributes":["security"],"openQuestions":["SSO needed?"]}]}
                """;
        BaRequirementsOutput out = mapper.readValue(json, BaRequirementsOutput.class);
        assertEquals(1, out.getRequirements().size());
        assertEquals("REQ-1", out.getRequirements().get(0).getId());
        assertEquals("Auth", out.getRequirements().get(0).getArea());
        assertEquals("User login", out.getRequirements().get(0).getDescription());
        assertEquals("High", out.getRequirements().get(0).getRisk());
        assertEquals(1, out.getRequirements().get(0).getQualityAttributes().size());
        assertEquals("SSO needed?", out.getRequirements().get(0).getOpenQuestions().get(0));
    }

    @Test
    void baRequirements_malformedJson_throwsException() {
        assertThrows(Exception.class, () -> mapper.readValue("{broken", BaRequirementsOutput.class));
    }

    @Test
    void baRequirements_extraFields_ignored() throws Exception {
        String json = """
                {"requirements":[{"id":"R1","area":"Core","description":"X","risk":"Low","qualityAttributes":[],"openQuestions":[]}],"unexpectedField":"ignored"}
                """;
        BaRequirementsOutput out = mapper.readValue(json, BaRequirementsOutput.class);
        assertEquals(1, out.getRequirements().size());
    }

    @Test
    void baRequirements_emptyArray() throws Exception {
        String json = "{\"requirements\":[]}";
        BaRequirementsOutput out = mapper.readValue(json, BaRequirementsOutput.class);
        assertTrue(out.getRequirements().isEmpty());
    }

    @Test
    void baRequirements_nullFields_defaults() throws Exception {
        String json = "{}";
        BaRequirementsOutput out = mapper.readValue(json, BaRequirementsOutput.class);
        assertNotNull(out.getRequirements());
        assertTrue(out.getRequirements().isEmpty());
    }

    // ── ArchitectDesignOutput ──

    @Test
    void architectDesign_validJson_parsesCorrectly() throws Exception {
        String json = """
                {"components":[{"name":"API","technology":"Spring Boot","complexity":"Low","purpose":"REST endpoints"}],"services":[{"name":"UserService","api":"/api/users","description":"CRUD"}],"dataFlows":[{"from":"Frontend","to":"API","data":"JSON","description":"HTTPS"}],"feasible":true,"assumptions":["PG available"],"tradeOffs":["Monolith vs micro"],"risks":["None"]}
                """;
        ArchitectDesignOutput out = mapper.readValue(json, ArchitectDesignOutput.class);
        assertEquals(1, out.getComponents().size());
        assertEquals("API", out.getComponents().get(0).getName());
        assertEquals(1, out.getServices().size());
        assertEquals("/api/users", out.getServices().get(0).getApi());
        assertEquals(1, out.getDataFlows().size());
        assertTrue(out.isFeasible());
        assertEquals(1, out.getAssumptions().size());
    }

    @Test
    void architectDesign_missingOptionalFields_ok() throws Exception {
        String json = "{\"components\":[{\"name\":\"API\",\"technology\":\"SB\",\"complexity\":\"Low\"}]}";
        ArchitectDesignOutput out = mapper.readValue(json, ArchitectDesignOutput.class);
        assertEquals(1, out.getComponents().size());
        assertNull(out.getComponents().get(0).getPurpose());
    }

    // ── DevImplementOutput ──

    @Test
    void devImplement_validJson_parsesCorrectly() throws Exception {
        String json = """
                {"storyId":"STORY-001","acAddressed":["AC-1"],"changesSummary":"Added login","filesToChange":[{"path":"AuthController.java","action":"CREATE","summary":"Login endpoint","riskLevel":"Low"}],"rationale":"Minimal change","riskLevel":"Low","testCoverage":85,"needsHuman":false,"humanQuestions":[]}
                """;
        DevImplementOutput out = mapper.readValue(json, DevImplementOutput.class);
        assertEquals("STORY-001", out.getStoryId());
        assertEquals(1, out.getFilesToChange().size());
        assertEquals("CREATE", out.getFilesToChange().get(0).getAction());
        assertEquals("Minimal change", out.getRationale());
        assertEquals("Low", out.getRiskLevel());
        assertEquals(85, out.getTestCoverage(), 0.01);
    }

    @Test
    void devImplement_emptyFiles() throws Exception {
        String json = "{\"storyId\":\"S1\",\"filesToChange\":[],\"rationale\":\"None\",\"riskLevel\":\"Low\"}";
        DevImplementOutput out = mapper.readValue(json, DevImplementOutput.class);
        assertTrue(out.getFilesToChange().isEmpty());
    }

    // ── TesterOutput ──

    @Test
    void testerOutput_validJson_parsesCorrectly() throws Exception {
        String json = """
                {"testsRun":6,"testsPassed":5,"testsFailed":1,"testsSkipped":0,"coveragePercent":75.0,"overallStatus":"needs_fix","doDMet":false,"failures":[{"testCase":"test_login","expected":"200","actual":"500","assignedTo":"developer"}]}
                """;
        TesterOutput out = mapper.readValue(json, TesterOutput.class);
        assertEquals(6, out.getTestsRun());
        assertEquals(5, out.getTestsPassed());
        assertEquals(1, out.getTestsFailed());
        assertEquals(75.0, out.getCoveragePercent(), 0.01);
        assertEquals("needs_fix", out.getOverallStatus());
        assertEquals(1, out.getFailures().size());
        assertEquals("test_login", out.getFailures().get(0).getTestCase());
    }

    @Test
    void testerOutput_allPass() throws Exception {
        String json = "{\"testsRun\":5,\"testsPassed\":5,\"testsFailed\":0,\"coveragePercent\":100.0,\"overallStatus\":\"pass\",\"doDMet\":true,\"failures\":[]}";
        TesterOutput out = mapper.readValue(json, TesterOutput.class);
        assertEquals(5, out.getTestsRun());
        assertEquals(5, out.getTestsPassed());
        assertTrue(out.isDoDMet());
        assertTrue(out.getFailures().isEmpty());
    }

    @Test
    void testerOutput_missingOptionalFields_defaults() throws Exception {
        String json = "{\"testsRun\":0,\"testsPassed\":0,\"testsFailed\":0,\"coveragePercent\":0.0,\"overallStatus\":\"pass\"}";
        TesterOutput out = mapper.readValue(json, TesterOutput.class);
        assertEquals(0, out.getTestsSkipped());
        assertFalse(out.isDoDMet());
        assertTrue(out.getFailures().isEmpty());
    }

    // ── ComplianceOutput ──

    @Test
    void complianceOutput_validJson_parsesCorrectly() throws Exception {
        String json = """
                {"overallStatus":"pass","severity":"LOW","riskLevel":"low","reportUrl":"/reports/1","checksPassed":[{"name":"DPDP-01","status":"PASS","details":"Consent obtained"}],"checksFailed":[],"findings":[],"needsHuman":false,"humanQuestions":[]}
                """;
        ComplianceOutput out = mapper.readValue(json, ComplianceOutput.class);
        assertEquals("pass", out.getOverallStatus());
        assertEquals("LOW", out.getSeverity());
        assertEquals(1, out.getChecksPassed().size());
        assertEquals("DPDP-01", out.getChecksPassed().get(0).getName());
        assertTrue(out.getChecksFailed().isEmpty());
        assertFalse(out.isNeedsHuman());
    }

    @Test
    void complianceOutput_findings_populated() throws Exception {
        String json = """
                {"overallStatus":"pass_with_warnings","severity":"MEDIUM","findings":[{"issue":"Missing encryption","impact":"medium","likelihood":"possible","mitigation":"Enable TDE","status":"open"}],"checksPassed":[],"checksFailed":[]}
                """;
        ComplianceOutput out = mapper.readValue(json, ComplianceOutput.class);
        assertEquals(1, out.getFindings().size());
        assertEquals("Missing encryption", out.getFindings().get(0).getIssue());
        assertEquals("open", out.getFindings().get(0).getStatus());
    }

    @Test
    void complianceOutput_nullFields_defaults() throws Exception {
        String json = "{}";
        ComplianceOutput out = mapper.readValue(json, ComplianceOutput.class);
        assertNotNull(out.getChecksPassed());
        assertNotNull(out.getChecksFailed());
        assertNotNull(out.getFindings());
        assertNotNull(out.getHumanQuestions());
        assertTrue(out.getChecksPassed().isEmpty());
    }
}
