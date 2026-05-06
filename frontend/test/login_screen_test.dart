import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:ucto_frontend/blocs/auth/auth_bloc.dart';
import 'package:ucto_frontend/ui/screens/login_screen.dart';

class MockAuthBloc extends Mock implements AuthBloc {}

class FakeAuthState extends Fake implements AuthState {}

void main() {
  late MockAuthBloc mockAuthBloc;

  setUpAll(() {
    registerFallbackValue(FakeAuthState());
    registerFallbackValue(FakeAuthEvent());
  });

  setUp(() {
    mockAuthBloc = MockAuthBloc();
  });

  Widget createTestWidget() {
    return MaterialApp(
      home: BlocProvider<AuthBloc>(
        create: (_) => mockAuthBloc,
        child: const LoginScreen(),
      ),
      routes: {
        '/dashboard': (context) => const Scaffold(body: Text('Dashboard')),
      },
    );
  }

  group('LoginScreen Widget Tests', () {
    testWidgets('renders all login form elements', (tester) async {
      when(() => mockAuthBloc.state).thenReturn(AuthInitial());
      when(() => mockAuthBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      // Verify key elements are present
      expect(find.text('Welcome to UCTO'), findsOneWidget);
      expect(find.text('Continue with Google'), findsOneWidget);
      expect(find.text('Sign In'), findsOneWidget);
      expect(find.text("Don't have an account? Sign Up"), findsOneWidget);
      expect(find.text('Sign in with Phone (OTP)'), findsOneWidget);
    });

    testWidgets('shows loading state on AuthLoading', (tester) async {
      when(() => mockAuthBloc.state).thenReturn(AuthLoading());
      when(() => mockAuthBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());
      await tester.pump();

      // Should show CircularProgressIndicator instead of Sign In text
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('shows error message on AuthError', (tester) async {
      when(() => mockAuthBloc.state)
          .thenReturn(AuthError('Invalid credentials'));
      when(() => mockAuthBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      expect(find.text('Invalid credentials'), findsOneWidget);
    });

    testWidgets('switches to OTP flow when Link is tapped', (tester) async {
      when(() => mockAuthBloc.state).thenReturn(AuthInitial());
      when(() => mockAuthBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      // Tap "Sign in with Phone (OTP)"
      await tester.tap(find.text('Sign in with Phone (OTP)'));
      await tester.pump();

      // OTP fields should now show
      expect(find.text('Send OTP'), findsOneWidget);
      expect(find.text('Verify & Sign In'), findsOneWidget);
      expect(find.text('Back to Email Login'), findsOneWidget);
    });

    testWidgets('switches back to email flow from OTP flow', (tester) async {
      when(() => mockAuthBloc.state).thenReturn(AuthInitial());
      when(() => mockAuthBloc.stream).thenAnswer((_) => const Stream.empty());

      await tester.pumpWidget(createTestWidget());

      // Switch to OTP flow
      await tester.tap(find.text('Sign in with Phone (OTP)'));
      await tester.pump();

      // Switch back
      await tester.tap(find.text('Back to Email Login'));
      await tester.pump();

      // Email login should be visible again
      expect(find.text('Sign In'), findsOneWidget);
    });

    testWidgets('shows OTP sent message via SnackBar', (tester) async {
      when(() => mockAuthBloc.state).thenReturn(AuthInitial());
      // Simulate listener transition
      when(() => mockAuthBloc.stream).thenAnswer((_) => Stream.value(
            AuthOtpSent('OTP sent to +919876543210'),
          ));

      await tester.pumpWidget(createTestWidget());

      // Emit the OTP sent state
      await tester.pump();

      // SnackBar should show
      expect(find.text('OTP sent to +919876543210'), findsOneWidget);
    });
  });
}

// Fake event class needed for registerFallbackValue
class FakeAuthEvent extends Fake implements AuthEvent {}
