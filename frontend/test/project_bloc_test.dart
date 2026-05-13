import 'package:flutter_test/flutter_test.dart';
import 'package:bloc_test/bloc_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:ucto_frontend/blocs/project/project_bloc.dart';
import 'package:ucto_frontend/services/api_service.dart';

class MockApiService extends Mock implements ApiService {}

void main() {
  late MockApiService mockApiService;
  late ProjectBloc projectBloc;

  setUpAll(() {
    registerFallbackValue(<String, dynamic>{});
  });

  setUp(() {
    mockApiService = MockApiService();
    projectBloc = ProjectBloc(mockApiService);
  });

  tearDown(() {
    projectBloc.close();
  });

  group('ProjectBloc', () {
    test('initial state is ProjectInitial', () {
      expect(projectBloc.state, isA<ProjectInitial>());
    });

    blocTest<ProjectBloc, ProjectState>(
      'emits loading, created, loading, and loaded states when project creation succeeds',
      build: () {
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

        when(() => mockApiService.post(
              '/projects',
              any(),
              auth: true,
            )).thenAnswer((_) async => projectJson);

        when(() => mockApiService.get(
              '/projects',
              auth: true,
            )).thenAnswer((_) async => [projectJson]);

        return projectBloc;
      },
      act: (bloc) =>
          bloc.add(CreateProject('My Awesome App', 'A test project')),
      expect: () => [
        isA<ProjectLoading>(),
        isA<ProjectCreated>()
            .having((state) => state.project.title, 'title', 'My Awesome App'),
        isA<ProjectLoading>(),
        isA<ProjectsLoaded>()
            .having((state) => state.projects.length, 'projects length', 1),
      ],
      verify: (_) {
        verify(() => mockApiService.post(
              '/projects',
              any(),
              auth: true,
            )).called(1);
        verify(() => mockApiService.get(
              '/projects',
              auth: true,
            )).called(1);
      },
    );

    blocTest<ProjectBloc, ProjectState>(
      'emits loading and error when project creation fails',
      build: () {
        when(() => mockApiService.post(
              '/projects',
              any(),
              auth: true,
            )).thenThrow(ApiException('Project creation failed', 400));
        return projectBloc;
      },
      act: (bloc) =>
          bloc.add(CreateProject('Duplicate App', 'This should fail')),
      expect: () => [
        isA<ProjectLoading>(),
        isA<ProjectError>().having(
            (state) => state.message, 'message', 'Project creation failed'),
      ],
      verify: (_) {
        verify(() => mockApiService.post(
              '/projects',
              any(),
              auth: true,
            )).called(1);
      },
    );
  });
}
