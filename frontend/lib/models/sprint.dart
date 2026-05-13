/// Represents a sprint in the PM/Scrum Master workflow.
/// Tracks the lifecycle of iterations aligned with closed-loop workflows.
class Sprint {
  final int? id;
  final int projectId;
  final String name;
  final DateTime startDate;
  final DateTime endDate;
  final String status; // Planning, Active, InReview, Closed
  final String activeLoop; // DISCOVERY, BUILD, RISK, UX_DOC, IDLE
  final int totalStoryPoints;
  final int completedStoryPoints;
  final String? goalDescription;
  final String? retrospectiveNotes;
  final DateTime? createdAt;

  Sprint({
    this.id,
    required this.projectId,
    required this.name,
    required this.startDate,
    required this.endDate,
    this.status = 'Planning',
    this.activeLoop = 'IDLE',
    this.totalStoryPoints = 0,
    this.completedStoryPoints = 0,
    this.goalDescription,
    this.retrospectiveNotes,
    this.createdAt,
  });

  factory Sprint.fromJson(Map<String, dynamic> json) {
    return Sprint(
      id: json['id'] as int?,
      projectId: json['projectId'] as int,
      name: json['name'] as String,
      startDate: DateTime.parse(json['startDate'] as String),
      endDate: DateTime.parse(json['endDate'] as String),
      status: json['status'] as String? ?? 'Planning',
      activeLoop: json['activeLoop'] as String? ?? 'IDLE',
      totalStoryPoints: json['totalStoryPoints'] as int? ?? 0,
      completedStoryPoints: json['completedStoryPoints'] as int? ?? 0,
      goalDescription: json['goalDescription'] as String?,
      retrospectiveNotes: json['retrospectiveNotes'] as String?,
      createdAt: json['createdAt'] != null ? DateTime.parse(json['createdAt'] as String) : null,
    );
  }

  Map<String, dynamic> toJson() => {
    if (id != null) 'id': id,
    'projectId': projectId,
    'name': name,
    'startDate': startDate.toIso8601String().split('T')[0],
    'endDate': endDate.toIso8601String().split('T')[0],
    'status': status,
    'activeLoop': activeLoop,
    'totalStoryPoints': totalStoryPoints,
    'completedStoryPoints': completedStoryPoints,
    if (goalDescription != null) 'goalDescription': goalDescription,
    if (retrospectiveNotes != null) 'retrospectiveNotes': retrospectiveNotes,
  };

  /// Calculate sprint completion percentage.
  double get completionPercentage {
    if (totalStoryPoints == 0) return 0;
    return completedStoryPoints / totalStoryPoints;
  }

  Sprint copyWith({
    int? id,
    int? projectId,
    String? name,
    DateTime? startDate,
    DateTime? endDate,
    String? status,
    String? activeLoop,
    int? totalStoryPoints,
    int? completedStoryPoints,
    String? goalDescription,
    String? retrospectiveNotes,
  }) {
    return Sprint(
      id: id ?? this.id,
      projectId: projectId ?? this.projectId,
      name: name ?? this.name,
      startDate: startDate ?? this.startDate,
      endDate: endDate ?? this.endDate,
      status: status ?? this.status,
      activeLoop: activeLoop ?? this.activeLoop,
      totalStoryPoints: totalStoryPoints ?? this.totalStoryPoints,
      completedStoryPoints: completedStoryPoints ?? this.completedStoryPoints,
      goalDescription: goalDescription ?? this.goalDescription,
      retrospectiveNotes: retrospectiveNotes ?? this.retrospectiveNotes,
    );
  }
}
