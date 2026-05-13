/// Represents a backlog item (epic, story, or task) in the product/sprint backlog.
/// Follows the story state machine: New → In Discovery → Ready → In Progress → In Review → Done.
class BacklogItem {
  final int? id;
  final int projectId;
  final String title;
  final String? description;
  final String itemType; // EPIC, STORY, TASK
  final String status; // New, InDiscovery, Ready, InProgress, InReview, Done, Blocked
  final int? parentId;
  final String? persona;
  final String? userStoryFormat;
  final String? acceptanceCriteriaJson;
  final String? constraintsJson;
  final String? dependenciesJson;
  final int priority;
  final int storyPoints;
  final bool dorPassed;
  final bool dodPassed;
  final String? dorChecklistJson;
  final String? dodChecklistJson;
  final int? sprintId;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  BacklogItem({
    this.id,
    required this.projectId,
    required this.title,
    this.description,
    this.itemType = 'STORY',
    this.status = 'New',
    this.parentId,
    this.persona,
    this.userStoryFormat,
    this.acceptanceCriteriaJson,
    this.constraintsJson,
    this.dependenciesJson,
    this.priority = 5,
    this.storyPoints = 0,
    this.dorPassed = false,
    this.dodPassed = false,
    this.dorChecklistJson,
    this.dodChecklistJson,
    this.sprintId,
    this.createdAt,
    this.updatedAt,
  });

  factory BacklogItem.fromJson(Map<String, dynamic> json) {
    return BacklogItem(
      id: json['id'] as int?,
      projectId: json['projectId'] as int,
      title: json['title'] as String,
      description: json['description'] as String?,
      itemType: json['itemType'] as String? ?? 'STORY',
      status: json['status'] as String? ?? 'New',
      parentId: json['parentId'] as int?,
      persona: json['persona'] as String?,
      userStoryFormat: json['userStoryFormat'] as String?,
      acceptanceCriteriaJson: json['acceptanceCriteriaJson'] as String?,
      constraintsJson: json['constraintsJson'] as String?,
      dependenciesJson: json['dependenciesJson'] as String?,
      priority: json['priority'] as int? ?? 5,
      storyPoints: json['storyPoints'] as int? ?? 0,
      dorPassed: json['dorPassed'] as bool? ?? false,
      dodPassed: json['dodPassed'] as bool? ?? false,
      dorChecklistJson: json['dorChecklistJson'] as String?,
      dodChecklistJson: json['dodChecklistJson'] as String?,
      sprintId: json['sprintId'] as int?,
      createdAt: json['createdAt'] != null ? DateTime.parse(json['createdAt'] as String) : null,
      updatedAt: json['updatedAt'] != null ? DateTime.parse(json['updatedAt'] as String) : null,
    );
  }

  Map<String, dynamic> toJson() => {
    if (id != null) 'id': id,
    'projectId': projectId,
    'title': title,
    if (description != null) 'description': description,
    'itemType': itemType,
    'status': status,
    if (parentId != null) 'parentId': parentId,
    if (persona != null) 'persona': persona,
    if (userStoryFormat != null) 'userStoryFormat': userStoryFormat,
    if (acceptanceCriteriaJson != null) 'acceptanceCriteriaJson': acceptanceCriteriaJson,
    if (constraintsJson != null) 'constraintsJson': constraintsJson,
    if (dependenciesJson != null) 'dependenciesJson': dependenciesJson,
    'priority': priority,
    'storyPoints': storyPoints,
    'dorPassed': dorPassed,
    'dodPassed': dodPassed,
    if (dorChecklistJson != null) 'dorChecklistJson': dorChecklistJson,
    if (dodChecklistJson != null) 'dodChecklistJson': dodChecklistJson,
    if (sprintId != null) 'sprintId': sprintId,
  };

  BacklogItem copyWith({
    int? id,
    int? projectId,
    String? title,
    String? description,
    String? itemType,
    String? status,
    int? parentId,
    String? persona,
    String? userStoryFormat,
    String? acceptanceCriteriaJson,
    String? constraintsJson,
    String? dependenciesJson,
    int? priority,
    int? storyPoints,
    bool? dorPassed,
    bool? dodPassed,
    String? dorChecklistJson,
    String? dodChecklistJson,
    int? sprintId,
  }) {
    return BacklogItem(
      id: id ?? this.id,
      projectId: projectId ?? this.projectId,
      title: title ?? this.title,
      description: description ?? this.description,
      itemType: itemType ?? this.itemType,
      status: status ?? this.status,
      parentId: parentId ?? this.parentId,
      persona: persona ?? this.persona,
      userStoryFormat: userStoryFormat ?? this.userStoryFormat,
      acceptanceCriteriaJson: acceptanceCriteriaJson ?? this.acceptanceCriteriaJson,
      constraintsJson: constraintsJson ?? this.constraintsJson,
      dependenciesJson: dependenciesJson ?? this.dependenciesJson,
      priority: priority ?? this.priority,
      storyPoints: storyPoints ?? this.storyPoints,
      dorPassed: dorPassed ?? this.dorPassed,
      dodPassed: dodPassed ?? this.dodPassed,
      dorChecklistJson: dorChecklistJson ?? this.dorChecklistJson,
      dodChecklistJson: dodChecklistJson ?? this.dodChecklistJson,
      sprintId: sprintId ?? this.sprintId,
    );
  }
}
