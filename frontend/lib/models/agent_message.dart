import 'dart:convert';

/// Structured message model for agent-to-agent communication.
/// Follows the standard message envelope defined in the agile methodology docs.
class AgentMessage {
  final int? id;
  final String fromAgent;
  final String toAgent;
  final String messageType; // REQUIREMENTS_PACKAGE, ARCHITECTURE_SPEC, UI_SPEC, etc.
  final int storyId;
  final int projectId;
  final String correlationId;
  final String? payloadJson;
  final bool needsHuman;
  final String? humanQuestionsJson;
  final String status; // PENDING, ROUTED, RESOLVED, ERROR
  final DateTime? createdAt;
  final DateTime? resolvedAt;

  AgentMessage({
    this.id,
    required this.fromAgent,
    required this.toAgent,
    required this.messageType,
    required this.storyId,
    required this.projectId,
    required this.correlationId,
    this.payloadJson,
    this.needsHuman = false,
    this.humanQuestionsJson,
    this.status = 'PENDING',
    this.createdAt,
    this.resolvedAt,
  });

  factory AgentMessage.fromJson(Map<String, dynamic> json) {
    return AgentMessage(
      id: json['id'] as int?,
      fromAgent: json['fromAgent'] as String,
      toAgent: json['toAgent'] as String,
      messageType: json['messageType'] as String,
      storyId: json['storyId'] as int,
      projectId: json['projectId'] as int,
      correlationId: json['correlationId'] as String,
      payloadJson: json['payloadJson'] as String?,
      needsHuman: json['needsHuman'] as bool? ?? false,
      humanQuestionsJson: json['humanQuestionsJson'] as String?,
      status: json['status'] as String? ?? 'PENDING',
      createdAt: json['createdAt'] != null ? DateTime.parse(json['createdAt'] as String) : null,
      resolvedAt: json['resolvedAt'] != null ? DateTime.parse(json['resolvedAt'] as String) : null,
    );
  }

  Map<String, dynamic> toJson() => {
    if (id != null) 'id': id,
    'fromAgent': fromAgent,
    'toAgent': toAgent,
    'messageType': messageType,
    'storyId': storyId,
    'projectId': projectId,
    'correlationId': correlationId,
    if (payloadJson != null) 'payloadJson': payloadJson,
    'needsHuman': needsHuman,
    if (humanQuestionsJson != null) 'humanQuestionsJson': humanQuestionsJson,
    'status': status,
  };

  /// Get human questions as a list (parsed from JSON).
  List<String> get humanQuestions {
    if (humanQuestionsJson == null || humanQuestionsJson!.isEmpty) return [];
    try {
      final List<dynamic> parsed = jsonDecode(humanQuestionsJson!) as List<dynamic>;
      return parsed.map((e) => e.toString()).toList();
    } catch (_) {
      return [humanQuestionsJson!];
    }
  }

  bool get isPendingHuman => needsHuman && status == 'PENDING';
  bool get isRouted => status == 'ROUTED';
  bool get isResolved => status == 'RESOLVED';
  bool get isError => status == 'ERROR';
}
