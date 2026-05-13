import 'dart:convert';
import 'api_service.dart';

/// Service for Orchestrator API operations.
/// Decides which loop to run, routes messages, and reports loop status.
class OrchestratorService {
  final ApiService _api;

  OrchestratorService(this._api);

  /// Evaluate which loop should run next.
  Future<Map<String, dynamic>> evaluateNextLoop(int projectId) async {
    return await _api.get('/orchestrator/evaluate/$projectId',
        auth: true) as Map<String, dynamic>;
  }

  /// Get the full loop status including pending human questions.
  Future<Map<String, dynamic>> getLoopStatus(int projectId) async {
    return await _api.get('/orchestrator/status/$projectId',
        auth: true) as Map<String, dynamic>;
  }

  /// Route a message to the correct agent.
  Future<Map<String, dynamic>> routeMessage(int messageId) async {
    return await _api.post('/orchestrator/action', {
      'action': 'ROUTE_MESSAGE',
      'messageId': messageId,
    }, auth: true) as Map<String, dynamic>;
  }
}
