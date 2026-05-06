part of 'auth_bloc.dart';

abstract class AuthEvent {}

class AuthCheckStatus extends AuthEvent {}

class AuthLoginRequested extends AuthEvent {
  final String email;
  final String password;
  AuthLoginRequested(this.email, this.password);
}

class AuthRegisterRequested extends AuthEvent {
  final String email;
  final String password;
  final String role;
  final String name;
  AuthRegisterRequested(this.email, this.password, this.role, this.name);
}

class AuthGoogleLoginRequested extends AuthEvent {}


class AuthOtpSendRequested extends AuthEvent {
  final String phoneNumber;
  AuthOtpSendRequested(this.phoneNumber);
}

class AuthOtpVerifyRequested extends AuthEvent {
  final String phoneNumber;
  final String otp;
  AuthOtpVerifyRequested(this.phoneNumber, this.otp);
}

class AuthLogoutRequested extends AuthEvent {}
