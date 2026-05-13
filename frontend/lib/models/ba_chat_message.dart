class BAChatMessage {
  final int id;
  final String userMessage;
  final String? baResponse;
  final int roundNumber;
  final String messageType;
  final String? decisionsJson;
  final DateTime createdAt;

  BAChatMessage({
    required this.id,
    required this.userMessage,
    this.baResponse,
    required this.roundNumber,
    required this.messageType,
    this.decisionsJson,
    required this.createdAt,
  });

  factory BAChatMessage.fromJson(Map<String, dynamic> json) {
    return BAChatMessage(
      id: json['id'] as int,
      userMessage: json['userMessage'] as String,
      baResponse: json['baResponse'] as String?,
      roundNumber: json['roundNumber'] as int,
      messageType: json['messageType'] as String,
      decisionsJson: json['decisionsJson'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }
}

class BAChatHistoryResponse {
  final List<BAChatMessage> messages;
  final int currentRound;
  final bool clarificationComplete;
  final bool needsEscalation;

  BAChatHistoryResponse({
    required this.messages,
    required this.currentRound,
    required this.clarificationComplete,
    required this.needsEscalation,
  });

  factory BAChatHistoryResponse.fromJson(Map<String, dynamic> json) {
    return BAChatHistoryResponse(
      messages: (json['messages'] as List<dynamic>)
          .map((m) => BAChatMessage.fromJson(m as Map<String, dynamic>))
          .toList(),
      currentRound: json['currentRound'] as int,
      clarificationComplete: json['clarificationComplete'] as bool,
      needsEscalation: json['needsEscalation'] as bool,
    );
  }
}
