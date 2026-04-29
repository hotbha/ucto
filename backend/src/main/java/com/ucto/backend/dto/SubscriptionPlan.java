package com.ucto.backend.dto;

public class SubscriptionPlan {
    private String tier;
    private int price;         // Price in INR paise (e.g., 2999 = ₹2,999)
    private int maxProjects;
    private int maxAgentRuns;
    private boolean hasAudit;
    private boolean hasCompliance;
    private boolean hasPrioritySupport;

    public SubscriptionPlan() {}

    public SubscriptionPlan(String tier, int price, int maxProjects, int maxAgentRuns,
                            boolean hasAudit, boolean hasCompliance, boolean hasPrioritySupport) {
        this.tier = tier;
        this.price = price;
        this.maxProjects = maxProjects;
        this.maxAgentRuns = maxAgentRuns;
        this.hasAudit = hasAudit;
        this.hasCompliance = hasCompliance;
        this.hasPrioritySupport = hasPrioritySupport;
    }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getMaxProjects() { return maxProjects; }
    public void setMaxProjects(int maxProjects) { this.maxProjects = maxProjects; }

    public int getMaxAgentRuns() { return maxAgentRuns; }
    public void setMaxAgentRuns(int maxAgentRuns) { this.maxAgentRuns = maxAgentRuns; }

    public boolean isHasAudit() { return hasAudit; }
    public void setHasAudit(boolean hasAudit) { this.hasAudit = hasAudit; }

    public boolean isHasCompliance() { return hasCompliance; }
    public void setHasCompliance(boolean hasCompliance) { this.hasCompliance = hasCompliance; }

    public boolean isHasPrioritySupport() { return hasPrioritySupport; }
    public void setHasPrioritySupport(boolean hasPrioritySupport) { this.hasPrioritySupport = hasPrioritySupport; }

    public String getFormattedPrice() {
        if (price == 0) return "Free";
        return "₹" + String.format("%,d", price);
    }

    public String getCurrency() { return "INR"; }
}
