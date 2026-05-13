package com.ucto.backend.dto.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArchitectDesignOutput {

    @JsonProperty("components")
    private List<ComponentItem> components = Collections.emptyList();

    @JsonProperty("services")
    private List<ServiceItem> services = Collections.emptyList();

    @JsonProperty("dataFlows")
    private List<DataFlowItem> dataFlows = Collections.emptyList();

    private boolean feasible;
    private List<String> assumptions = Collections.emptyList();
    private List<String> tradeOffs = Collections.emptyList();
    private List<String> risks = Collections.emptyList();

    public List<ComponentItem> getComponents() { return components; }
    public void setComponents(List<ComponentItem> components) { this.components = components; }
    public List<ServiceItem> getServices() { return services; }
    public void setServices(List<ServiceItem> services) { this.services = services; }
    public List<DataFlowItem> getDataFlows() { return dataFlows; }
    public void setDataFlows(List<DataFlowItem> dataFlows) { this.dataFlows = dataFlows; }
    public boolean isFeasible() { return feasible; }
    public void setFeasible(boolean feasible) { this.feasible = feasible; }
    public List<String> getAssumptions() { return assumptions; }
    public void setAssumptions(List<String> assumptions) { this.assumptions = assumptions; }
    public List<String> getTradeOffs() { return tradeOffs; }
    public void setTradeOffs(List<String> tradeOffs) { this.tradeOffs = tradeOffs; }
    public List<String> getRisks() { return risks; }
    public void setRisks(List<String> risks) { this.risks = risks; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ComponentItem {
        private String name;
        private String technology;
        private String complexity;
        private String purpose;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTechnology() { return technology; }
        public void setTechnology(String technology) { this.technology = technology; }
        public String getComplexity() { return complexity; }
        public void setComplexity(String complexity) { this.complexity = complexity; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServiceItem {
        private String name;
        private String api;
        private String description;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getApi() { return api; }
        public void setApi(String api) { this.api = api; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataFlowItem {
        private String from;
        private String to;
        private String data;
        private String description;
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
