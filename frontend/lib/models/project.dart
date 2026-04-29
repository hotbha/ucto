class Project {
  final int id;
  final String title;
  final String description;
  final String status;
  final String tier;
  final int ownerId;
  final String? createdAt;
  final String? updatedAt;
  final List<dynamic>? screens;
  final List<dynamic>? requirements;

  Project({
    required this.id,
    required this.title,
    this.description = '',
    this.status = 'ACTIVE',
    this.tier = 'FREE',
    required this.ownerId,
    this.createdAt,
    this.updatedAt,
    this.screens,
    this.requirements,
  });

  factory Project.fromJson(Map<String, dynamic> json) {
    return Project(
      id: json['id'] as int,
      title: json['title'] as String? ?? 'Untitled',
      description: json['description'] as String? ?? '',
      status: json['status'] as String? ?? 'ACTIVE',
      tier: json['tier'] as String? ?? 'FREE',
      ownerId: json['ownerId'] as int? ?? json['createdBy'] as int? ?? 0,
      createdAt: json['createdAt'] as String?,
      updatedAt: json['updatedAt'] as String?,
      screens: json['screens'] as List<dynamic>?,
      requirements: json['requirements'] as List<dynamic>?,
    );
  }

  Map<String, dynamic> toJson() => {
        'title': title,
        'description': description,
        'status': status,
        'tier': tier,
      };
}
