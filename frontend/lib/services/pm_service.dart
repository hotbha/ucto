import 'dart:convert';
import '../models/backlog_item.dart';
import '../models/sprint.dart';
import 'api_service.dart';

/// Service for PM/Scrum Master API operations.
/// Handles backlog management, sprint lifecycle, loop coordination, DoR/DoD enforcement.
class PmService {
  final ApiService _api;

  PmService(this._api);

  /// Create a new sprint.
  Future<Sprint> createSprint({
    required int projectId,
    required String name,
    required String startDate,
    required String endDate,
    String? goalDescription,
  }) async {
    final data = await _api.post('/pm/action', {
      'projectId': projectId,
      'action': 'CREATE_SPRINT',
      'sprintName': name,
      'startDate': startDate,
      'endDate': endDate,
      if (goalDescription != null) 'goalDescription': goalDescription,
    }, auth: true);
    return Sprint.fromJson(data['data']);
  }

  /// Add a backlog item (epic, story, or task).
  Future<BacklogItem> addBacklogItem({
    required int projectId,
    required String title,
    String? description,
    String itemType = 'STORY',
    String? persona,
    String? userStoryFormat,
    List<String>? acceptanceCriteria,
    List<String>? constraints,
    List<String>? dependencies,
    int priority = 5,
    int storyPoints = 0,
    int? parentId,
    int? sprintId,
  }) async {
    final data = await _api.post('/pm/action', {
      'projectId': projectId,
      'action': 'ADD_BACKLOG_ITEM',
      'title': title,
      if (description != null) 'description': description,
      'itemType': itemType,
      if (persona != null) 'persona': persona,
      if (userStoryFormat != null) 'userStoryFormat': userStoryFormat,
      if (acceptanceCriteria != null) 'acceptanceCriteria': acceptanceCriteria,
      if (constraints != null) 'constraints': constraints,
      if (dependencies != null) 'dependencies': dependencies,
      'priority': priority,
      'storyPoints': storyPoints,
      if (parentId != null) 'parentId': parentId,
      if (sprintId != null) 'sprintId': sprintId,
    }, auth: true);
    return BacklogItem.fromJson(data['data']);
  }

  /// Update the status of a backlog item (with DoR/DoD enforcement).
  Future<BacklogItem> updateStatus({
    required int itemId,
    required String newStatus,
  }) async {
    final data = await _api.post('/pm/action', {
      'action': 'UPDATE_STATUS',
      'sprintId': itemId,
      'newStatus': newStatus,
    }, auth: true);
    return BacklogItem.fromJson(data['data']);
  }

  /// Activate a loop for the current sprint.
  Future<Map<String, dynamic>> runLoop({
    required int projectId,
    required String loopType,
  }) async {
    return await _api.post('/pm/action', {
      'projectId': projectId,
      'action': 'RUN_LOOP',
      'loopType': loopType,
    }, auth: true) as Map<String, dynamic>;
  }

  /// Check Definition of Ready for a backlog item.
  Future<Map<String, dynamic>> checkDoR(int itemId) async {
    return await _api.post('/pm/action', {
      'action': 'CHECK_DOR',
      'sprintId': itemId,
    }, auth: true) as Map<String, dynamic>;
  }

  /// Check Definition of Done for a backlog item.
  Future<Map<String, dynamic>> checkDoD(int itemId) async {
    return await _api.post('/pm/action', {
      'action': 'CHECK_DOD',
      'sprintId': itemId,
    }, auth: true) as Map<String, dynamic>;
  }

  /// Get backlog for a project.
  Future<List<BacklogItem>> getBacklog(int projectId, {int? sprintId}) async {
    if (sprintId != null) {
      final data = await _api.post('/pm/action', {
        'projectId': projectId,
        'action': 'GET_BACKLOG',
        'sprintId': sprintId,
      }, auth: true);
      return (data['data'] as List).map((e) => BacklogItem.fromJson(e)).toList();
    }
    final response = await _api.get('/pm/backlog/$projectId', auth: true);
    return (response['data'] as List).map((e) => BacklogItem.fromJson(e)).toList();
  }

  /// Get sprints for a project.
  Future<List<Sprint>> getSprints(int projectId) async {
    final response = await _api.get('/pm/sprints/$projectId', auth: true);
    return (response['data'] as List).map((e) => Sprint.fromJson(e)).toList();
  }
}

/// Exception thrown when DoR or DoD validation fails.
class DoValidationException implements Exception {
  final String message;
  DoValidationException(this.message);

  @override
  String toString() => message;
}
