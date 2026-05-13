package com.ucto.backend.dto.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaRequirementsOutput {

    @JsonProperty("requirements")
    private List<RequirementItem> requirements = Collections.emptyList();

    public List<RequirementItem> getRequirements() { return requirements; }
    public void setRequirements(List<RequirementItem> requirements) { this.requirements = requirements; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RequirementItem {
        private String id;
        private String area;
        private String description;
        private String risk; // BusinessCritical | High | Medium | Low
        @JsonProperty("qualityAttributes")
        private List<String> qualityAttributes = Collections.emptyList();
        @JsonProperty("openQuestions")
        private List<String> openQuestions = Collections.emptyList();

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getArea() { return area; }
        public void setArea(String area) { this.area = area; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getRisk() { return risk; }
        public void setRisk(String risk) { this.risk = risk; }
        public List<String> getQualityAttributes() { return qualityAttributes; }
        public void setQualityAttributes(List<String> qualityAttributes) { this.qualityAttributes = qualityAttributes; }
        public List<String> getOpenQuestions() { return openQuestions; }
        public void setOpenQuestions(List<String> openQuestions) { this.openQuestions = openQuestions; }
    }
}
