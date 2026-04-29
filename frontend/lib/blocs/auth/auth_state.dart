part of 'auth_bloc.dart';

abstract class AuthState {}

class AuthInitial extends AuthState {}

class AuthLoading extends AuthState {}

class AuthAuthenticated extends AuthState {
  final User user;
  AuthAuthenticated(this.user);
}

class AuthUnauthenticated extends AuthState {}

class AuthOtpSent extends AuthState {
  final String message;
  AuthOtpSent(this.message);
}

class AuthError extends AuthState {
  final String message;
  AuthError(this.message);
}
