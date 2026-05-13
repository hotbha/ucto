import 'dart:convert';
import 'api_service.dart';

/// Service for Documentation Agent API operations.
/// Handles generation, update, publishing, and archiving of living documentation.
class DocService {
  final ApiService _api;

  DocService(this._api);

  /// Generate a new document.
  Future<Map<String, dynamic>> generateDoc({
    required int projectId,
    required String docType,
    required String title,
    String? content,
    int? storyId,
    int? adrId,
    String? version,
  }) async {
    return await _api.post('/docs/action', {
      'projectId': projectId,
      'action': 'GENERATE',
      'docType': docType,
      'title': title,
      if (content != null) 'content': content,
      if (storyId != null) 'storyId': storyId,
      if (adrId != null) 'adrId': adrId,
      if (version != null) 'version': version,
    }, auth: true) as Map<String, dynamic>;
  }

  /// Update an existing document.
  Future<Map<String, dynamic>> updateDoc({
    required int docId,
    String? title,
    String? content,
    String? version,
  }) async {
    return await _api.post('/docs/action', {
      'docId': docId,
      'action': 'UPDATE',
      if (title != null) 'title': title,
      if (content != null) 'content': content,
      if (version != null) 'version': version,
    }, auth: true) as Map<String, dynamic>;
  }

  /// Publish a document.
  Future<Map<String, dynamic>> publishDoc(int docId) async {
    return await _api.post('/docs/action', {
      'docId': docId,
      'action': 'PUBLISH',
    }, auth: true) as Map<String, dynamic>;
  }

  /// Archive a document.
  Future<Map<String, dynamic>> archiveDoc(int docId) async {
    return await _api.post('/docs/action', {
      'docId': docId,
      'action': 'ARCHIVE',
    }, auth: true) as Map<String, dynamic>;
  }

  /// Get documents for a project.
  Future<List<Map<String, dynamic>>> getDocsByProject(int projectId) async {
    final response = await _api.get('/docs/project/$projectId', auth: true);
    return List<Map<String, dynamic>>.from(response['data'] ?? []);
  }

  /// Get documents by type for a project.
  Future<List<Map<String, dynamic>>> getDocsByType(
      int projectId, String docType) async {
    final response =
        await _api.get('/docs/project/$projectId/type/$docType', auth: true);
    return List<Map<String, dynamic>>.from(response['data'] ?? []);
  }
}
