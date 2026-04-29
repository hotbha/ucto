class UsageStatus {
  final String tier;
  final int maxProjects;
  final int projectsUsed;
  final int projectsRemaining;
  final int maxAgentRuns;
  final int runsUsed;
  final int runsRemaining;
  final bool hasAudit;
  final bool hasCompliance;
  final bool hasPrioritySupport;
  final bool needsUpgrade;
  final bool canRun;

  UsageStatus({
    required this.tier,
    required this.maxProjects,
    required this.projectsUsed,
    required this.projectsRemaining,
    required this.maxAgentRuns,
    required this.runsUsed,
    required this.runsRemaining,
    required this.hasAudit,
    required this.hasCompliance,
    required this.hasPrioritySupport,
    required this.needsUpgrade,
    this.canRun = true,
  });

  factory UsageStatus.fromJson(Map<String, dynamic> json) {
    return UsageStatus(
      tier: json['tier'] as String? ?? 'FREE',
      maxProjects: json['maxProjects'] as int? ?? 1,
      projectsUsed: json['projectsUsed'] as int? ?? 0,
      projectsRemaining: json['projectsRemaining'] as int? ?? 1,
      maxAgentRuns: json['maxAgentRuns'] as int? ?? 5,
      runsUsed: json['runsUsed'] as int? ?? 0,
      runsRemaining: json['runsRemaining'] as int? ?? 5,
      hasAudit: json['hasAudit'] as bool? ?? false,
      hasCompliance: json['hasCompliance'] as bool? ?? false,
      hasPrioritySupport: json['hasPrioritySupport'] as bool? ?? false,
      needsUpgrade: json['needsUpgrade'] as bool? ?? false,
      canRun: json['canRun'] as bool? ?? true,
    );
  }
}
