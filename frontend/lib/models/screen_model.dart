class ScreenModel {
  final int id;
  final int projectId;
  final int? requirementId;
  final String type; // WIREFRAME, MOCKUP, DESIGN_SPEC
  final String status; // PENDING, APPROVED, REJECTED, CHANGES_REQUESTED
  final String? storageUrl;
  final String? feedback;
  final String? mimeType;
  final int revisionCount;
  final int? approvedBy;
  final String? approvedAt;
  final String? createdAt;

  ScreenModel({
    required this.id,
    required this.projectId,
    this.requirementId,
    this.type = 'WIREFRAME',
    this.status = 'PENDING',
    this.storageUrl,
    this.feedback,
    this.mimeType,
    this.revisionCount = 0,
    this.approvedBy,
    this.approvedAt,
    this.createdAt,
  });

  factory ScreenModel.fromJson(Map<String, dynamic> json) {
    return ScreenModel(
      id: json['id'] as int,
      projectId: json['projectId'] as int,
      requirementId: json['requirementId'] as int?,
      type: json['type'] as String? ?? 'WIREFRAME',
      status: json['status'] as String? ?? 'PENDING',
      storageUrl: json['storageUrl'] as String?,
      feedback: json['feedback'] as String?,
      mimeType: json['mimeType'] as String?,
      revisionCount: json['revisionCount'] as int? ?? 0,
      approvedBy: json['approvedBy'] as int?,
      approvedAt: json['approvedAt'] as String?,
      createdAt: json['createdAt'] as String?,
    );
  }

  Map<String, dynamic> toJson() => {
        'projectId': projectId,
        'type': type,
        'status': status,
        'storageUrl': storageUrl,
        'feedback': feedback,
        'mimeType': mimeType,
      };

  ScreenModel copyWith({
    int? id,
    int? projectId,
    int? requirementId,
    String? type,
    String? status,
    String? storageUrl,
    String? feedback,
    String? mimeType,
    int? revisionCount,
    int? approvedBy,
    String? approvedAt,
  }) {
    return ScreenModel(
      id: id ?? this.id,
      projectId: projectId ?? this.projectId,
      requirementId: requirementId ?? this.requirementId,
      type: type ?? this.type,
      status: status ?? this.status,
      storageUrl: storageUrl ?? this.storageUrl,
      feedback: feedback ?? this.feedback,
      mimeType: mimeType ?? this.mimeType,
      revisionCount: revisionCount ?? this.revisionCount,
      approvedBy: approvedBy ?? this.approvedBy,
      approvedAt: approvedAt ?? this.approvedAt,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
