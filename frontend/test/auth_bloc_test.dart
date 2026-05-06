import 'package:flutter_test/flutter_test.dart';
import 'package:bloc_test/bloc_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:ucto_frontend/blocs/auth/auth_bloc.dart';
import 'package:ucto_frontend/services/api_service.dart';

// Use Mocktail's Mock class
class MockApiService extends Mock implements ApiService {}

void main() {
  late MockApiService mockApiService;
  late AuthBloc authBloc;

  setUpAll(() {
    // Required for mocktail when using any() with custom types
    registerFallbackValue(<String, dynamic>{});
  });

  setUp(() {
    mockApiService = MockApiService();
    authBloc = AuthBloc(mockApiService);
  });

  tearDown(() {
    authBloc.close();
  });

  group('AuthBloc', () {
    test('initial state is AuthInitial', () {
      expect(authBloc.state, isA<AuthInitial>());
    });

    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoading, AuthAuthenticated] when login is successful',
      build: () {
        when(() => mockApiService.post(
              any(),
              any(),
              auth: any(named: 'auth'),
            )).thenAnswer((_) async => {
              'accessToken': 'access123',
              'refreshToken': 'refresh123',
              'user': {
                'id': 1,
                'email': 'test@test.com',
                'role': 'FOUNDER',
                'name': 'Test User'
              },
            });
        when(() => mockApiService.storeTokens(any(), any()))
            .thenAnswer((_) async => {});
        return authBloc;
      },
      act: (bloc) =>
          bloc.add(AuthLoginRequested('test@test.com', 'password123')),
      expect: () => [
        isA<AuthLoading>(),
        isA<AuthAuthenticated>(),
      ],
    );

    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoading, AuthError] when login fails',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenThrow(ApiException('Invalid credentials', 401));
        return authBloc;
      },
      act: (bloc) => bloc.add(AuthLoginRequested('wrong@test.com', 'wrong')),
      expect: () => [
        isA<AuthLoading>(),
        isA<AuthError>(),
      ],
    );

    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoading, AuthAuthenticated] when registration succeeds',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {
                  'accessToken': 'access123',
                  'refreshToken': 'refresh123',
                  'user': {
                    'id': 2,
                    'email': 'new@test.com',
                    'role': 'FOUNDER',
                    'name': 'New User'
                  },
                });
        when(() => mockApiService.storeTokens(any(), any()))
            .thenAnswer((_) async => {});
        return authBloc;
      },
      act: (bloc) => bloc.add(AuthRegisterRequested(
          'new@test.com', 'pass123', 'FOUNDER', 'New User')),
      expect: () => [
        isA<AuthLoading>(),
        isA<AuthAuthenticated>(),
      ],
    );

    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoading, AuthError] when registration fails (duplicate email)',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenThrow(ApiException('Email already registered', 400));
        return authBloc;
      },
      act: (bloc) => bloc.add(AuthRegisterRequested(
          'existing@test.com', 'pass', 'FOUNDER', 'User')),
      expect: () => [
        isA<AuthLoading>(),
        isA<AuthError>(),
      ],
    );

    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoading, AuthAuthenticated] when OTP verify succeeds',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {
                  'accessToken': 'access123',
                  'refreshToken': 'refresh123',
                  'user': {
                    'id': 3,
                    'email': 'user@phone.ucto.app',
                    'role': 'FOUNDER',
                    'name': null
                  },
                });
        when(() => mockApiService.storeTokens(any(), any()))
            .thenAnswer((_) async => {});
        return authBloc;
      },
      act: (bloc) =>
          bloc.add(AuthOtpVerifyRequested('+919876543210', '123456')),
      expect: () => [
        isA<AuthLoading>(),
        isA<AuthAuthenticated>(),
      ],
    );

    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoading, AuthAuthenticated] when OAuth login succeeds',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {
                  'accessToken': 'google_access',
                  'refreshToken': 'google_refresh',
                  'user': {
                    'id': 4,
                    'email': 'google_user@example.com',
                    'role': 'FOUNDER',
                    'name': 'Google User'
                  },
                });
        when(() => mockApiService.storeTokens(any(), any()))
            .thenAnswer((_) async => {});
        return authBloc;
      },
      act: (bloc) => bloc.add(AuthGoogleLoginRequested()),
      expect: () => [
        isA<AuthLoading>(),
        isA<AuthAuthenticated>(),
      ],
    );

    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoading, AuthOtpSent] when OTP send succeeds',
      build: () {
        when(() => mockApiService.post(any(), any(), auth: any(named: 'auth')))
            .thenAnswer((_) async => {'message': 'OTP sent successfully'});
        return authBloc;
      },
      act: (bloc) => bloc.add(AuthOtpSendRequested('+919876543210')),
      expect: () => [
        isA<AuthLoading>(),
        isA<AuthOtpSent>(),
      ],
    );

    blocTest<AuthBloc, AuthState>(
      'emits AuthUnauthenticated when logout is requested',
      build: () {
        when(() => mockApiService.clearTokens()).thenAnswer((_) async => {});
        return authBloc;
      },
      act: (bloc) => bloc.add(AuthLogoutRequested()),
      expect: () => [isA<AuthUnauthenticated>()],
    );

    blocTest<AuthBloc, AuthState>(
      'emits AuthUnauthenticated when token check finds no token',
      build: () {
        when(() => mockApiService.getAccessToken())
            .thenAnswer((_) async => null);
        return authBloc;
      },
      act: (bloc) => bloc.add(AuthCheckStatus()),
      expect: () => [isA<AuthUnauthenticated>()],
    );

    blocTest<AuthBloc, AuthState>(
      'emits AuthAuthenticated when valid token is found',
      build: () {
        when(() => mockApiService.getAccessToken())
            .thenAnswer((_) async => 'valid.jwt.token');
        when(() => mockApiService.decodeJwt(any())).thenReturn(
            {'userId': 1, 'sub': 'test@test.com', 'role': 'FOUNDER'});
        return authBloc;
      },
      act: (bloc) => bloc.add(AuthCheckStatus()),
      expect: () => [isA<AuthAuthenticated>()],
    );

    blocTest<AuthBloc, AuthState>(
      'emits AuthUnauthenticated when invalid token is found',
      build: () {
        when(() => mockApiService.getAccessToken())
            .thenAnswer((_) async => 'invalid.token');
        when(() => mockApiService.decodeJwt(any())).thenReturn(null);
        when(() => mockApiService.clearTokens()).thenAnswer((_) async => {});
        return authBloc;
      },
      act: (bloc) => bloc.add(AuthCheckStatus()),
      expect: () => [isA<AuthUnauthenticated>()],
    );
  });
}
