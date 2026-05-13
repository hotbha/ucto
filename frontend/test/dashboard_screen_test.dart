import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:ucto_frontend/blocs/project/project_bloc.dart';
import 'package:ucto_frontend/blocs/subscription/subscription_bloc.dart';
import 'package:ucto_frontend/services/api_service.dart';
import 'package:ucto_frontend/ui/screens/dashboard_screen.dart';

class MockApiService extends Mock implements ApiService {}

class MockSubscriptionBloc extends Mock implements SubscriptionBloc {}

class FakeSubscriptionState extends Fake implements SubscriptionState {}

class FakeSubscriptionEvent extends Fake implements SubscriptionEvent {}

void main() {
  late MockApiService mockApiService;
  late ProjectBloc projectBloc;
  late MockSubscriptionBloc mockSubscriptionBloc;

  setUpAll(() {
    registerFallbackValue(<String, dynamic>{});
    registerFallbackValue(FakeSubscriptionState());
    registerFallbackValue(FakeSubscriptionEvent());
  });

  setUp(() {
    mockApiService = MockApiService();
    projectBloc = ProjectBloc(mockApiService);
    mockSubscriptionBloc = MockSubscriptionBloc();
    when(() => mockSubscriptionBloc.state).thenReturn(SubscriptionInitial());
    when(() => mockSubscriptionBloc.stream)
        .thenAnswer((_) => const Stream.empty());
  });

  tearDown(() {
    projectBloc.close();
  });

  Widget createTestWidget() {
    return MaterialApp(
      home: MultiBlocProvider(
        providers: [
          BlocProvider<ProjectBloc>.value(value: projectBloc),
          BlocProvider<SubscriptionBloc>.value(value: mockSubscriptionBloc),
        ],
        child: const DashboardScreen(),
      ),
    );
  }

  /// Helper: pump until the bloc state matches the matcher, using runAsync
  /// to allow mock Future.value microtasks to resolve.
  Future<void> pumpUntilState(
    WidgetTester tester,
    bool Function(ProjectState) matcher, {
    int maxPumps = 20,
  }) async {
    for (int i = 0; i < maxPumps; i++) {
      if (matcher(projectBloc.state)) return;
      await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 1)));
      await tester.pump();
    }
    if (!matcher(projectBloc.state)) {
      fail('Timed out waiting for bloc state. Current state: ${projectBloc.state.runtimeType}');
    }
  }

  testWidgets('creates a project and shows it in the project list',
      (tester) async {
    const projectJson = {
      'id': 1,
      'title': 'My Awesome App',
      'description': 'A test project',
      'status': 'ACTIVE',
      'tier': 'FREE',
      'ownerId': 42,
      'createdAt': '2026-05-08T00:00:00Z',
      'updatedAt': '2026-05-08T00:00:00Z',
    };

    var getProjectCalls = 0;
    when(() => mockApiService.get('/projects', auth: true)).thenAnswer((_) {
      getProjectCalls += 1;
      return Future.value(getProjectCalls == 1 ? [] : [projectJson]);
    });

    when(() => mockApiService.post('/projects', any(), auth: true))
        .thenAnswer((_) => Future.value(projectJson));

    await tester.pumpWidget(createTestWidget());
    await pumpUntilState(tester, (s) => s is ProjectsLoaded);

    expect(projectBloc.state, isA<ProjectsLoaded>());
    expect(find.text('New Project'), findsOneWidget);

    // Open create dialog
    await tester.tap(find.text('New Project'));
    await tester.pumpAndSettle();

    // Fill form
    await tester.enterText(find.byType(TextField).first, 'My Awesome App');
    await tester.enterText(find.byType(TextField).at(1), 'A test project');

    // Tap Create
    await tester.tap(find.text('Create'));
    await tester.pump();

    // Process the CreateProject async chain (post → emit Created → add LoadProjects)
    await pumpUntilState(tester, (s) => s is ProjectsLoaded);

    // SnackBar should be visible from BlocListener reacting to ProjectCreated
    expect(find.text('Project created successfully.'), findsOneWidget);

    await tester.pumpAndSettle();

    expect(projectBloc.state, isA<ProjectsLoaded>());
    verify(() => mockApiService.post('/projects', any(), auth: true)).called(1);
    expect(getProjectCalls, 2);
    expect(find.text('My Awesome App'), findsOneWidget);
  });
}
