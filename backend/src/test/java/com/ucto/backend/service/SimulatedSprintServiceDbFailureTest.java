package com.ucto.backend.service;

import com.ucto.backend.dto.SimulatedSprintRequest;
import com.ucto.backend.dto.SimulatedSprintResponse;
import com.ucto.backend.entity.Project;
import com.ucto.backend.repository.ComplianceResultRepository;
import com.ucto.backend.repository.TestResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests DB failure handling in SimulatedSprintService.
 * Verifies that when repository.save() throws DataAccessException, the sprint
 * returns a response with completed BA/Architect/Dev summaries, dbError=true,
 * and does NOT throw an uncaught exception.
 */
@ExtendWith(MockitoExtension.class)
class SimulatedSprintServiceDbFailureTest {

    @Mock private ProjectService projectService;
    @Mock private RepoWorkspaceService repoWorkspaceService;
    @Mock private AuditLogService auditLogService;
    @Mock private TestResultRepository testResultRepository;
    @Mock private ComplianceResultRepository complianceResultRepository;
    @Mock private QualityGateService qualityGateService;

    private StubLLMAgentClient llmClient;
    private SimulatedSprintService service;

    @BeforeEach
    void setUp() {
        PromptCatalog catalog = new PromptCatalog();
        java.util.Map<String, PromptCatalog.PromptEntry> prompts = new java.util.HashMap<>();
        prompts.put("BA_REQUIREMENTS", new PromptCatalog.PromptEntry("BA_REQUIREMENTS", "BA prompt", "{}"));
        prompts.put("ARCHITECT_DESIGN", new PromptCatalog.PromptEntry("ARCHITECT_DESIGN", "Arch prompt", "{}"));
        prompts.put("DEV_IMPLEMENT", new PromptCatalog.PromptEntry("DEV_IMPLEMENT", "Dev prompt", "{}"));
        prompts.put("TEST_GENERATE", new PromptCatalog.PromptEntry("TEST_GENERATE", "Test prompt", "{}"));
        prompts.put("COMPLIANCE_CHECK", new PromptCatalog.PromptEntry("COMPLIANCE_CHECK", "Comp prompt", "{}"));
        ReflectionTestUtils.setField(catalog, "prompts", prompts);

        llmClient = new StubLLMAgentClient();
        ReflectionTestUtils.setField(llmClient, "promptCatalog", catalog);

        service = new SimulatedSprintService();
        ReflectionTestUtils.setField(service, "projectService", projectService);
        ReflectionTestUtils.setField(service, "repoWorkspaceService", repoWorkspaceService);
        ReflectionTestUtils.setField(service, "auditLogService", auditLogService);
        ReflectionTestUtils.setField(service, "llmAgentClient", llmClient);
        ReflectionTestUtils.setField(service, "testResultRepository", testResultRepository);
        ReflectionTestUtils.setField(service, "complianceResultRepository", complianceResultRepository);
        ReflectionTestUtils.setField(service, "qualityGateService", qualityGateService);
    }

    @Test
    void testSprint_dbFailureOnTestResultSave_returnsPartialResultsWithError() {
        Project project = new Project();
        project.setId(1L);
        project.setTitle("DB Fail Test");
        project.setRepoUrl("https://github.com/org/repo.git");

        when(projectService.getProjectById(1L)).thenReturn(project);

        // Simulate DB failure on TestResult save
        when(testResultRepository.save(any())).thenThrow(
                new DataAccessResourceFailureException("Connection refused to PostgreSQL"));

        SimulatedSprintRequest req = new SimulatedSprintRequest("main", "Add a feature");

        // This should NOT throw — the catch block should handle it
        SimulatedSprintResponse response = service.runSprint(1L, req);

        assertNotNull(response);
        assertEquals(1L, response.getProjectId());
        assertEquals("main", response.getBranch());

        // Steps 1-3 (BA, Architect, Developer) should be completed
        assertTrue(response.getSteps().size() >= 3);
        assertEquals("BA", response.getSteps().get(0).getAgent());
        assertEquals("ARCHITECT", response.getSteps().get(1).getAgent());
        assertEquals("DEVELOPER", response.getSteps().get(2).getAgent());

        // BA/Architect/Dev summaries should be present
        assertNotNull(response.getBaSummary());
        assertNotNull(response.getArchitectSummary());
        assertNotNull(response.getDevSummary());

        // dbError flag should be true
        assertTrue(response.isDbError());
        assertNotNull(response.getDbErrorMessage());
        assertTrue(response.getDbErrorMessage().contains("DB persistence failure"));

        // testResult and complianceResult should be null (never saved)
        assertNull(response.getTestResult());
        assertNull(response.getComplianceResult());

        // Gate status should be the default empty one (overallPass=false)
        assertNotNull(response.getGateStatus());
        assertFalse(response.getGateStatus().isOverallPass());
    }

    @Test
    void testSprint_dbFailureOnComplianceResultSave_returnsPartialResultsWithError() {
        Project project = new Project();
        project.setId(2L);
        project.setTitle("Compliance DB Fail");
        project.setRepoUrl("https://github.com/org/repo.git");

        when(projectService.getProjectById(2L)).thenReturn(project);

        // TestResult save succeeds
        when(testResultRepository.save(any())).thenAnswer(i -> {
            var tr = i.<com.ucto.backend.entity.TestResult>getArgument(0);
            tr.setId(100L);
            return tr;
        });

        // ComplianceResult save fails
        when(complianceResultRepository.save(any())).thenThrow(
                new DataAccessResourceFailureException("DB write failed"));

        SimulatedSprintRequest req = new SimulatedSprintRequest("main", "Fix login bug");

        SimulatedSprintResponse response = service.runSprint(2L, req);

        assertNotNull(response);
        assertTrue(response.isDbError());
        assertTrue(response.getDbErrorMessage().contains("DB persistence failure"));

        // Test result should have been saved successfully (before the compliance failure)
        assertNotNull(response.getTestResult());
        assertNotNull(response.getTestResult().getId());
        assertEquals(100L, response.getTestResult().getId());
        // Compliance result should be null (save failed)
        assertNull(response.getComplianceResult());

        // BA/Architect/Dev summaries should be present
        assertNotNull(response.getBaSummary());
        assertNotNull(response.getArchitectSummary());
        assertNotNull(response.getDevSummary());
    }

    @Test
    void testSprint_noDbFailure_completesSuccessfully() {
        Project project = new Project();
        project.setId(3L);
        project.setTitle("Happy Path");
        project.setRepoUrl("https://github.com/org/repo.git");

        when(projectService.getProjectById(3L)).thenReturn(project);
        when(testResultRepository.save(any())).thenAnswer(i -> {
            var tr = i.<com.ucto.backend.entity.TestResult>getArgument(0);
            tr.setId(101L); return tr;
        });
        when(complianceResultRepository.save(any())).thenAnswer(i -> {
            var cr = i.<com.ucto.backend.entity.ComplianceResult>getArgument(0);
            cr.setId(201L); return cr;
        });
        when(qualityGateService.evaluateTestGate(anyLong(), anyString(), any(), anyBoolean(), anyString()))
                .thenAnswer(i -> { var ge = new com.ucto.backend.entity.GateEvaluation(); ge.setPassed(true); return ge; });
        when(qualityGateService.evaluateComplianceGate(anyLong(), anyString(), any(), anyBoolean(), anyString()))
                .thenAnswer(i -> { var ge = new com.ucto.backend.entity.GateEvaluation(); ge.setPassed(true); return ge; });
        when(qualityGateService.getGateStatus(anyLong(), anyString()))
                .thenAnswer(i -> new com.ucto.backend.service.QualityGateService.GateStatusDTO());

        SimulatedSprintRequest req = new SimulatedSprintRequest("main", "Add profile page");

        SimulatedSprintResponse response = service.runSprint(3L, req);

        assertNotNull(response);
        assertFalse(response.isDbError());
        assertNull(response.getDbErrorMessage());
        assertNotNull(response.getTestResult());
        assertNotNull(response.getComplianceResult());
        assertEquals(5, response.getSteps().size());
    }
}
