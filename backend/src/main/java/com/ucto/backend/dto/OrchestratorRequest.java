package com.ucto.backend.dto;

import java.util.Map;

/**
 * Request DTO for Orchestrator operations.
 */
public class OrchestratorRequest {
    private Long projectId;
    private String action; // EVALUATE_NEXT_LOOP, ROUTE_MESSAGE, GET_LOOP_STATUS, TRIGGER_AGENT
    private String loopType; // DISCOVERY, BUILD, RISK, UX_DOC
    private Long messageId;
    private Map<String, Object> parameters;

    public OrchestratorRequest() {}

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getLoopType() { return loopType; }
    public void setLoopType(String loopType) { this.loopType = loopType; }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}
