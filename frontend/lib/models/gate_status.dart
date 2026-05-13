/// Models for the quality gate status API response.
/// Maps to QualityGateService.GateStatusDTO from the backend.
class GateStatus {
  final int projectId;
  final String branch;
  final GateEvaluation? testGate;
  final GateEvaluation? complianceGate;
  final GateEvaluation? coordinatedGate;
  final bool overallPass;

  GateStatus({
    required this.projectId,
    required this.branch,
    this.testGate,
    this.complianceGate,
    this.coordinatedGate,
    required this.overallPass,
  });

  factory GateStatus.fromJson(Map<String, dynamic> json) {
    return GateStatus(
      projectId: json['projectId'] as int? ?? 0,
      branch: json['branch'] as String? ?? 'main',
      testGate: json['testGate'] != null
          ? GateEvaluation.fromJson(json['testGate'] as Map<String, dynamic>)
          : null,
      complianceGate: json['complianceGate'] != null
          ? GateEvaluation.fromJson(json['complianceGate'] as Map<String, dynamic>)
          : null,
      coordinatedGate: json['coordinatedGate'] != null
          ? GateEvaluation.fromJson(json['coordinatedGate'] as Map<String, dynamic>)
          : null,
      overallPass: json['overallPass'] as bool? ?? false,
    );
  }
}

class GateEvaluation {
  final int id;
  final int projectId;
  final String gateType;
  final bool passed;
  final int? testResultId;
  final int? complianceResultId;
  final String correlationId;
  final String? details;
  final String? branch;
  final bool simulation;
  final String? evaluatedAt;

  GateEvaluation({
    required this.id,
    required this.projectId,
    required this.gateType,
    required this.passed,
    this.testResultId,
    this.complianceResultId,
    required this.correlationId,
    this.details,
    this.branch,
    required this.simulation,
    this.evaluatedAt,
  });

  factory GateEvaluation.fromJson(Map<String, dynamic> json) {
    return GateEvaluation(
      id: json['id'] as int? ?? 0,
      projectId: json['projectId'] as int? ?? 0,
      gateType: json['gateType'] as String? ?? '',
      passed: json['passed'] as bool? ?? false,
      testResultId: json['testResultId'] as int?,
      complianceResultId: json['complianceResultId'] as int?,
      correlationId: json['correlationId'] as String? ?? '',
      details: json['details'] as String?,
      branch: json['branch'] as String?,
      simulation: json['simulation'] as bool? ?? false,
      evaluatedAt: json['evaluatedAt'] as String?,
    );
  }
}
