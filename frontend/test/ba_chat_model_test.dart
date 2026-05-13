import 'package:flutter_test/flutter_test.dart';
import 'package:ucto_frontend/models/ba_chat_message.dart';

void main() {
  group('BAChatMessage model', () {
    test('fromJson creates BAChatMessage with all fields', () {
      final json = {
        'id': 1,
        'userMessage': 'Hello',
        'baResponse': 'Hi, how can I help?',
        'roundNumber': 1,
        'messageType': 'GREETING',
        'decisionsJson': null,
        'createdAt': '2026-05-01T10:00:00Z',
      };

      final message = BAChatMessage.fromJson(json);
      expect(message.id, 1);
      expect(message.userMessage, 'Hello');
      expect(message.baResponse, 'Hi, how can I help?');
      expect(message.roundNumber, 1);
      expect(message.messageType, 'GREETING');
      expect(message.decisionsJson, isNull);
      expect(message.createdAt, DateTime.utc(2026, 5, 1, 10, 0, 0));
    });

    test('fromJson handles null baResponse', () {
      final json = {
        'id': 2,
        'userMessage': 'Hi',
        'baResponse': null,
        'roundNumber': 2,
        'messageType': 'CLARIFICATION',
        'decisionsJson': null,
        'createdAt': '2026-05-01T11:00:00Z',
      };

      final message = BAChatMessage.fromJson(json);
      expect(message.baResponse, isNull);
      expect(message.messageType, 'CLARIFICATION');
    });

    test('fromJson parses CLARIFICATION message type', () {
      final json = {
        'id': 3,
        'userMessage': 'I want a fast app',
        'baResponse': 'What performance targets?',
        'roundNumber': 2,
        'messageType': 'CLARIFICATION',
        'decisionsJson': null,
        'createdAt': '2026-05-01T12:00:00Z',
      };

      final message = BAChatMessage.fromJson(json);
      expect(message.messageType, 'CLARIFICATION');
      expect(message.roundNumber, 2);
    });

    test('fromJson parses DECISION message type', () {
      final json = {
        'id': 4,
        'userMessage': 'Use Flutter, Spring Boot, PostgreSQL',
        'baResponse': 'Documenting decisions',
        'roundNumber': 1,
        'messageType': 'DECISION',
        'decisionsJson': '[{"tech":"Flutter"},{"tech":"Spring Boot"}]',
        'createdAt': '2026-05-01T13:00:00Z',
      };

      final message = BAChatMessage.fromJson(json);
      expect(message.messageType, 'DECISION');
      expect(message.decisionsJson, isNotNull);
    });

    test('fromJson parses FINALIZATION message type', () {
      final json = {
        'id': 5,
        'userMessage': 'Looks good, finalize',
        'baResponse': 'Requirements finalized',
        'roundNumber': 3,
        'messageType': 'FINALIZATION',
        'decisionsJson': null,
        'createdAt': '2026-05-01T14:00:00Z',
      };

      final message = BAChatMessage.fromJson(json);
      expect(message.messageType, 'FINALIZATION');
      expect(message.roundNumber, 3);
    });

    test('fromJson parses ESCALATION message type', () {
      final json = {
        'id': 6,
        'userMessage': 'Still need changes',
        'baResponse': 'Escalating to human supervisor',
        'roundNumber': 4,
        'messageType': 'ESCALATION',
        'decisionsJson': null,
        'createdAt': '2026-05-01T15:00:00Z',
      };

      final message = BAChatMessage.fromJson(json);
      expect(message.messageType, 'ESCALATION');
      expect(message.roundNumber, 4);
    });

    test('fromJson handles invalid createdAt date', () {
      final json = {
        'id': 7,
        'userMessage': 'test',
        'baResponse': 'response',
        'roundNumber': 1,
        'messageType': 'GREETING',
        'decisionsJson': null,
        'createdAt': 'invalid-date',
      };

      expect(() => BAChatMessage.fromJson(json), throwsFormatException);
    });
  });

  group('BAChatHistoryResponse model', () {
    test('fromJson creates BAChatHistoryResponse with messages', () {
      final json = {
        'messages': [
          {
            'id': 1,
            'userMessage': 'Hello',
            'baResponse': 'Hi!',
            'roundNumber': 1,
            'messageType': 'GREETING',
            'decisionsJson': null,
            'createdAt': '2026-05-01T10:00:00Z',
          },
        ],
        'currentRound': 1,
        'clarificationComplete': false,
        'needsEscalation': false,
      };

      final history = BAChatHistoryResponse.fromJson(json);
      expect(history.messages.length, 1);
      expect(history.currentRound, 1);
      expect(history.clarificationComplete, false);
      expect(history.needsEscalation, false);
    });

    test('fromJson with multiple messages', () {
      final json = {
        'messages': [
          {
            'id': 1,
            'userMessage': 'Hello',
            'baResponse': 'Hi!',
            'roundNumber': 1,
            'messageType': 'GREETING',
            'decisionsJson': null,
            'createdAt': '2026-05-01T10:00:00Z',
          },
          {
            'id': 2,
            'userMessage': 'I need a web app',
            'baResponse': 'Tell me more',
            'roundNumber': 2,
            'messageType': 'CLARIFICATION',
            'decisionsJson': null,
            'createdAt': '2026-05-01T10:05:00Z',
          },
        ],
        'currentRound': 2,
        'clarificationComplete': false,
        'needsEscalation': false,
      };

      final history = BAChatHistoryResponse.fromJson(json);
      expect(history.messages.length, 2);
      expect(history.currentRound, 2);
    });

    test('fromJson with escalation', () {
      final json = {
        'messages': [
          {
            'id': 1,
            'userMessage': 'test',
            'baResponse': 'response',
            'roundNumber': 3,
            'messageType': 'ESCALATION',
            'decisionsJson': null,
            'createdAt': '2026-05-01T10:00:00Z',
          },
        ],
        'currentRound': 3,
        'clarificationComplete': false,
        'needsEscalation': true,
      };

      final history = BAChatHistoryResponse.fromJson(json);
      expect(history.needsEscalation, true);
    });

    test('fromJson with empty messages list', () {
      final json = {
        'messages': [],
        'currentRound': 0,
        'clarificationComplete': false,
        'needsEscalation': false,
      };

      final history = BAChatHistoryResponse.fromJson(json);
      expect(history.messages, isEmpty);
      expect(history.currentRound, 0);
    });
  });
}
