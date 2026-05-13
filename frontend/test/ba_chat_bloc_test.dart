import 'package:flutter_test/flutter_test.dart';
import 'package:bloc_test/bloc_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:ucto_frontend/blocs/ba_chat/ba_chat_bloc.dart';
import 'package:ucto_frontend/services/api_service.dart';

class MockApiService extends Mock implements ApiService {}

void main() {
  late MockApiService mockApiService;
  late BAChatBloc baChatBloc;

  setUpAll(() {
    registerFallbackValue(<String, dynamic>{});
  });

  setUp(() {
    mockApiService = MockApiService();
    baChatBloc = BAChatBloc(mockApiService);
  });

  tearDown(() {
    baChatBloc.close();
  });

  group('BAChatBloc', () {
    test('initial state is BAChatInitial', () {
      expect(baChatBloc.state, isA<BAChatInitial>());
    });

    // ── Send Message ────────────────────────────────────────────────

    blocTest<BAChatBloc, BAChatState>(
      'emits [BAChatLoading, BAChatMessageSent] when send message succeeds',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {
                  'id': 1,
                  'userMessage': 'Hello',
                  'baResponse': 'Hi, how can I help?',
                  'roundNumber': 1,
                  'messageType': 'GREETING',
                  'decisionsJson': null,
                  'createdAt': '2026-05-01T10:00:00Z',
                  'clarificationComplete': false,
                  'needsEscalation': false,
                });
        return baChatBloc;
      },
      act: (bloc) => bloc.add(SendMessage(10, 'Hello')),
      expect: () => [
        isA<BAChatLoading>(),
        isA<BAChatMessageSent>(),
      ],
    );

    blocTest<BAChatBloc, BAChatState>(
      'emits [BAChatLoading, BAChatError] when send message fails',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenThrow(ApiException('Agent run limit exceeded', 402));
        return baChatBloc;
      },
      act: (bloc) => bloc.add(SendMessage(10, 'Hello')),
      expect: () => [
        isA<BAChatLoading>(),
        isA<BAChatError>(),
      ],
    );

    blocTest<BAChatBloc, BAChatState>(
      'emits BAChatMessageSent with correct message type for CLARIFICATION',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {
                  'id': 2,
                  'userMessage': 'I want a fast app',
                  'baResponse': 'What performance targets do you need?',
                  'roundNumber': 1,
                  'messageType': 'CLARIFICATION',
                  'decisionsJson': null,
                  'createdAt': '2026-05-01T10:01:00Z',
                  'clarificationComplete': false,
                  'needsEscalation': false,
                });
        return baChatBloc;
      },
      act: (bloc) => bloc.add(SendMessage(10, 'I want a fast app')),
      verify: (bloc) {
        final state = bloc.state as BAChatMessageSent;
        expect(state.message.messageType, 'CLARIFICATION');
        expect(state.clarificationComplete, false);
        expect(state.needsEscalation, false);
      },
    );

    blocTest<BAChatBloc, BAChatState>(
      'emits BAChatMessageSent with escalation flag',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {
                  'id': 3,
                  'userMessage': 'Still need changes',
                  'baResponse': 'Escalating to human supervisor',
                  'roundNumber': 4,
                  'messageType': 'ESCALATION',
                  'decisionsJson': null,
                  'createdAt': '2026-05-01T10:05:00Z',
                  'clarificationComplete': false,
                  'needsEscalation': true,
                });
        return baChatBloc;
      },
      act: (bloc) => bloc.add(SendMessage(10, 'Still need changes')),
      verify: (bloc) {
        final state = bloc.state as BAChatMessageSent;
        expect(state.message.messageType, 'ESCALATION');
        expect(state.needsEscalation, true);
        expect(state.currentRound, 4);
      },
    );

    blocTest<BAChatBloc, BAChatState>(
      'emits BAChatMessageSent with clarification complete flag',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {
                  'id': 4,
                  'userMessage': 'Looks good, finalize',
                  'baResponse': 'Requirements finalized!',
                  'roundNumber': 3,
                  'messageType': 'FINALIZATION',
                  'decisionsJson': null,
                  'createdAt': '2026-05-01T10:10:00Z',
                  'clarificationComplete': true,
                  'needsEscalation': false,
                });
        return baChatBloc;
      },
      act: (bloc) => bloc.add(SendMessage(10, 'Looks good, finalize')),
      verify: (bloc) {
        final state = bloc.state as BAChatMessageSent;
        expect(state.message.messageType, 'FINALIZATION');
        expect(state.clarificationComplete, true);
      },
    );

    // ── Load Chat History ───────────────────────────────────────────

    blocTest<BAChatBloc, BAChatState>(
      'emits [BAChatLoading, BAChatLoaded] when load history succeeds',
      build: () {
        when(() => mockApiService.get(any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {
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
                });
        return baChatBloc;
      },
      act: (bloc) => bloc.add(LoadChatHistory(10)),
      expect: () => [
        isA<BAChatLoading>(),
        isA<BAChatLoaded>(),
      ],
    );

    blocTest<BAChatBloc, BAChatState>(
      'emits [BAChatLoading, BAChatError] when load history fails',
      build: () {
        when(() => mockApiService.get(any(), auth: any(named: 'auth')))
            .thenThrow(ApiException('Not found', 404));
        return baChatBloc;
      },
      act: (bloc) => bloc.add(LoadChatHistory(999)),
      expect: () => [
        isA<BAChatLoading>(),
        isA<BAChatError>(),
      ],
    );

    blocTest<BAChatBloc, BAChatState>(
      'BAChatLoaded contains correct state from history',
      build: () {
        when(() => mockApiService.get(any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {
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
                      'userMessage': 'I need web app',
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
                });
        return baChatBloc;
      },
      act: (bloc) => bloc.add(LoadChatHistory(10)),
      verify: (bloc) {
        final state = bloc.state as BAChatLoaded;
        expect(state.messages.length, 2);
        expect(state.currentRound, 2);
        expect(state.clarificationComplete, false);
        expect(state.needsEscalation, false);
      },
    );

    // ── Clear Chat ──────────────────────────────────────────────────

    blocTest<BAChatBloc, BAChatState>(
      'emits BAChatInitial when clear chat',
      build: () => baChatBloc,
      act: (bloc) => bloc.add(ClearChat()),
      expect: () => [isA<BAChatInitial>()],
    );
  });
}
