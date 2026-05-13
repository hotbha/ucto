package com.ucto.backend.dto;

public class SimulatedSprintRequest {
    private String branch;
    private String changeDescription;

    public SimulatedSprintRequest() {}

    public SimulatedSprintRequest(String branch, String changeDescription) {
        this.branch = branch;
        this.changeDescription = changeDescription;
    }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }
}
