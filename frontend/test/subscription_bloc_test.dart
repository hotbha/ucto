import 'package:flutter_test/flutter_test.dart';
import 'package:bloc_test/bloc_test.dart';
import 'package:mocktail/mocktail.dart'; // Switched to mocktail
import 'package:ucto_frontend/blocs/subscription/subscription_bloc.dart';
import 'package:ucto_frontend/services/api_service.dart';

class MockApiService extends Mock implements ApiService {}

void main() {
  late MockApiService mockApiService;
  late SubscriptionBloc subscriptionBloc;

  setUpAll(() {
    // Register fallback for map arguments used in POST calls
    registerFallbackValue(<String, dynamic>{});
  });

  setUp(() {
    mockApiService = MockApiService();
    subscriptionBloc = SubscriptionBloc(mockApiService);
  });

  tearDown(() {
    subscriptionBloc.close();
  });

  group('SubscriptionBloc', () {
    test('initial state is SubscriptionInitial', () {
      expect(subscriptionBloc.state, isA<SubscriptionInitial>());
    });

    blocTest<SubscriptionBloc, SubscriptionState>(
      'emits [SubscriptionLoading, SubscriptionLoaded] when load succeeds',
      build: () {
        // Mocktail syntax: use a lambda () => and any(named: 'auth')
        when(() => mockApiService.get(any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {
                  'tier': 'FREE',
                  'maxProjects': 1,
                  'projectsUsed': 0,
                  'projectsRemaining': 1,
                  'maxAgentRuns': 5,
                  'runsUsed': 2,
                  'runsRemaining': 3,
                  'hasAudit': false,
                  'hasCompliance': false,
                  'hasPrioritySupport': false,
                  'needsUpgrade': false,
                  'canRun': true,
                });
        return subscriptionBloc;
      },
      act: (bloc) => bloc.add(LoadSubscription()),
      expect: () => [
        isA<SubscriptionLoading>(),
        isA<SubscriptionLoaded>(),
      ],
    );

    blocTest<SubscriptionBloc, SubscriptionState>(
      'emits [SubscriptionLoading, SubscriptionError] when load fails',
      build: () {
        when(() => mockApiService.get(any(), auth: any(named: 'auth')))
            .thenThrow(ApiException('Server error', 500));
        return subscriptionBloc;
      },
      act: (bloc) => bloc.add(LoadSubscription()),
      expect: () => [
        isA<SubscriptionLoading>(),
        isA<SubscriptionError>(),
      ],
    );

    blocTest<SubscriptionBloc, SubscriptionState>(
      'emits [SubscriptionLoading, SubscriptionUpgraded] when upgrade succeeds',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {'message': 'Upgraded to STARTUP'});
        return subscriptionBloc;
      },
      act: (bloc) => bloc.add(UpgradeSubscription('STARTUP')),
      expect: () => [
        isA<SubscriptionLoading>(),
        isA<SubscriptionUpgraded>(),
      ],
    );

    blocTest<SubscriptionBloc, SubscriptionState>(
      'emits [SubscriptionLoading, SubscriptionError] when upgrade fails',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenThrow(ApiException('Payment failed', 402));
        return subscriptionBloc;
      },
      act: (bloc) => bloc.add(UpgradeSubscription('GROWTH')),
      expect: () => [
        isA<SubscriptionLoading>(),
        isA<SubscriptionError>(),
      ],
    );

    blocTest<SubscriptionBloc, SubscriptionState>(
      'emits [SubscriptionLoading, SubscriptionUpgraded] when trial starts',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {'message': 'Trial started!'});
        return subscriptionBloc;
      },
      act: (bloc) => bloc.add(StartTrial()),
      expect: () => [
        isA<SubscriptionLoading>(),
        isA<SubscriptionUpgraded>(),
      ],
    );
  });
}
