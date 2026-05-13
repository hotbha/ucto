package com.ucto.backend.controller;

import com.ucto.backend.dto.SimulatedSprintRequest;
import com.ucto.backend.dto.SimulatedSprintResponse;
import com.ucto.backend.entity.ComplianceResult;
import com.ucto.backend.entity.Project;
import com.ucto.backend.entity.TestResult;
import com.ucto.backend.repository.ComplianceResultRepository;
import com.ucto.backend.repository.TestResultRepository;
import com.ucto.backend.service.ProjectService;
import com.ucto.backend.service.SimulatedSprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/projects/{id}")
public class SimulatedSprintController {

    @Autowired
    private SimulatedSprintService simulatedSprintService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private ComplianceResultRepository complianceResultRepository;

    @PostMapping("/simulated-sprint")
    public ResponseEntity<?> runSimulatedSprint(@PathVariable Long id,
                                                 @RequestBody SimulatedSprintRequest request,
                                                 Authentication auth) {
        Project project = projectService.getProjectById(id);
        if (project == null) return ResponseEntity.notFound().build();

        try {
            SimulatedSprintResponse response = simulatedSprintService.runSprint(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/branches/{branch}/test-results/latest")
    public ResponseEntity<?> getLatestTestResult(@PathVariable Long id, @PathVariable String branch) {
        Project project = projectService.getProjectById(id);
        if (project == null) return ResponseEntity.notFound().build();

        var result = testResultRepository.findTopByProjectIdAndBranchOrderByCreatedAtDesc(id, branch);
        if (result.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.get());
    }

    @GetMapping("/branches/{branch}/compliance-results/latest")
    public ResponseEntity<?> getLatestComplianceResult(@PathVariable Long id, @PathVariable String branch) {
        Project project = projectService.getProjectById(id);
        if (project == null) return ResponseEntity.notFound().build();

        var result = complianceResultRepository.findTopByProjectIdAndBranchOrderByCreatedAtDesc(id, branch);
        if (result.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.get());
    }
}
