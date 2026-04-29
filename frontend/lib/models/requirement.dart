class Requirement {
  final int id;
  final int projectId;
  final String title;
  final String description;
  final String status;
  final int createdBy;
  final String? createdAt;

  Requirement({
    required this.id,
    required this.projectId,
    required this.title,
    this.description = '',
    this.status = 'DRAFT',
    required this.createdBy,
    this.createdAt,
  });

  factory Requirement.fromJson(Map<String, dynamic> json) {
    return Requirement(
      id: json['id'] as int,
      projectId: json['projectId'] as int,
      title: json['title'] as String,
      description: json['description'] as String? ?? '',
      status: json['status'] as String? ?? 'DRAFT',
      createdBy: json['createdBy'] as int? ?? 0,
      createdAt: json['createdAt'] as String?,
    );
  }

  Map<String, dynamic> toJson() => {
        'projectId': projectId,
        'title': title,
        'description': description,
        'status': status,
      };
}
