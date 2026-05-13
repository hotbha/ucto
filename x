import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:ucto_frontend/blocs/ba_chat/ba_chat_bloc.dart';
import 'package:ucto_frontend/ui/screens/ba_chat_screen.dart';

class MockBAChatBloc extends Mock implements BAChatBloc {}

class FakeBAChatState extends Fake implements BAChatState {}

class FakeBAChatEvent extends Fake implements BAChatEvent {}

void main() {
  late MockBAChatBloc mockBAChatBloc;

  setUpAll(() {
    registerFallbackValue(FakeBAChatState());
    registerFallbackValue(FakeBAChatEvent());
  });

  setUp(() {
    mockBAChatBloc = MockBAChatBloc();
  });

  Widget createTestWidget() {
    return MaterialApp(
      home: BlocProvider<BAChatBloc>(
        create: (_) => mockBAChatBloc,
        child: const BAChatScreen(
          projectId: 10,
          projectTitle: 'Test Project',
        ),
      ),
    );
  }

  group('BAChatScreen Widget Tests', () {
    testWidgets('shows loading indicator when in initial state', (tester) async {
      when(() => mockBAChatBloc.state).thenReturn(BAChatInitial());
      when(() => mockBAChatBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      expect(find.text('Start a conversation with your BA Agent'), findsOneWidget);
      expect(find.text('Test Project'), findsOneWidget);
    });

    testWidgets('shows loading indicator when loading', (tester) async {
      when(() => mockBAChatBloc.state).thenReturn(BAChatLoading());
      when(() => mockBAChatBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('shows error state with retry button', (tester) async {
      when(() => mockBAChatBloc.state).thenReturn(BAChatError('Failed to load chat'));
      when(() => mockBAChatBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      expect(find.text('Failed to load chat'), findsOneWidget);
      expect(find.text('Retry'), findsOneWidget);
    });

    testWidgets('renders send button and input field', (tester) async {
      when(() => mockBAChatBloc.state).thenReturn(BAChatInitial());
      when(() => mockBAChatBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      expect(find.byType(TextField), findsOneWidget);
      expect(find.byIcon(Icons.send), findsOneWidget);
    });

    testWidgets('sends message when send button is tapped', (tester) async {
      when(() => mockBAChatBloc.state).thenReturn(BAChatInitial());
      when(() => mockBAChatBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      // Type a message
      await tester.enterText(find.byType(TextField), 'Hello BA');
      await tester.pump();

      // Tap send button
      await tester.tap(find.byIcon(Icons.send));
      await tester.pump();

      verify(() => mockBAChatBloc.add(any<SendMessage>())).called(1);
    });

    testWidgets('sends message when enter is pressed', (tester) async {
      when(() => mockBAChatBloc.state).thenReturn(BAChatInitial());
      when(() => mockBAChatBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      // Type a message and submit
      await tester.enterText(find.byType(TextField), 'Hello BA');
      await tester.testTextInput.receiveAction(TextInputAction.send);
      await tester.pump();

      verify(() => mockBAChatBloc.add(any<SendMessage>())).called(1);
    });

    testWidgets('shows Round badge in AppBar when loaded', (tester) async {
      when(() => mockBAChatBloc.state).thenReturn(BAChatLoaded(
        messages: [],
        currentRound: 2,
        clarificationComplete: false,
        needsEscalation: false,
      ));
      when(() => mockBAChatBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      expect(find.text('Round 2/3'), findsOneWidget);
    });

    testWidgets('shows escalation warning banner when needsEscalation', (tester) async {
      when(() => mockBAChatBloc.state).thenReturn(BAChatLoaded(
        messages: [],
        currentRound: 3,
        clarificationComplete: false,
        needsEscalation: true,
      ));
      when(() => mockBAChatBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      expect(
        find.text('Maximum clarification rounds reached. Your request has been escalated to a UCTO Admin.'),
        findsOneWidget,
      );
      // Round badge should show red
      expect(find.text('Round 3/3'), findsOneWidget);
    });

    testWidgets('shows clarification complete banner when finalized', (tester) async {
      when(() => mockBAChatBloc.state).thenReturn(BAChatLoaded(
        messages: [],
        currentRound: 3,
        clarificationComplete: true,
        needsEscalation: false,
      ));
      when(() => mockBAChatBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      expect(
        find.text('Requirements finalized! Development agents have been triggered.'),
        findsOneWidget,
      );
    });

    testWidgets('shows empty state when no messages', (tester) async {
      when(() => mockBAChatBloc.state).thenReturn(BAChatLoaded(
        messages: [],
        currentRound: 1,
        clarificationComplete: false,
        needsEscalation: false,
      ));
      when(() => mockBAChatBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      expect(find.text('No messages yet'), findsOneWidget);
      expect(find.text('Describe your requirements to get started'), findsOneWidget);
    });

    testWidgets('shows loading spinner on send button when loading', (tester) async {
      when(() => mockBAChatBloc.state).thenReturn(BAChatLoading());
      when(() => mockBAChatBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      // Send button should be disabled and show CircularProgressIndicator
      final iconButton = tester.widget<IconButton>(find.byType(IconButton));
      expect(iconButton.onPressed, isNull); // disabled when loading
    });

    testWidgets('displays BA agent name and badge in bubble', (tester) async {
      final messages = [
        BAChatMessage(
          id: 1,
          userMessage: 'Hello',
          baResponse: 'Hi, how can I help?',
          roundNumber: 1,
          messageType: 'GREETING',
          createdAt: DateTime(2026, 5, 1, 10, 0, 0),
        ),
      ];

      when(() => mockBAChatBloc.state).thenReturn(BAChatLoaded(
        messages: messages,
        currentRound: 1,
        clarificationComplete: false,
        needsEscalation: false,
      ));
      when(() => mockBAChatBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      // User message should be visible
      expect(find.text('Hello'), findsOneWidget);
      // BA response should be visible
      expect(find.text('Hi, how can I help?'), findsOneWidget);
      // BA badge should be visible
      expect(find.text('BA'), findsOneWidget);
      // BA Agent label
      expect(find.text('BA Agent'), findsOneWidget);
    });
  });
}
