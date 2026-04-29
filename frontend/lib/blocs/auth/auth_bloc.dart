import 'dart:convert';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../models/user.dart';
import '../../services/api_service.dart';

part 'auth_event.dart';
part 'auth_state.dart';

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final ApiService _api;

  AuthBloc(this._api) : super(AuthInitial()) {
    on<AuthCheckStatus>(_onCheckStatus);
    on<AuthLoginRequested>(_onLogin);
    on<AuthRegisterRequested>(_onRegister);
    on<AuthGoogleLoginRequested>(_onGoogleLogin);
    on<AuthFacebookLoginRequested>(_onFacebookLogin);
    on<AuthOtpSendRequested>(_onOtpSend);
    on<AuthOtpVerifyRequested>(_onOtpVerify);
    on<AuthLogoutRequested>(_onLogout);
  }

  Future<void> _onCheckStatus(AuthCheckStatus event, Emitter<AuthState> emit) async {
    final token = await _api.getAccessToken();
    if (token != null) {
      final payload = _api.decodeJwt(token);
      if (payload != null) {
        final user = User(
          id: payload['userId'] as int? ?? 0,
          email: payload['sub'] as String? ?? '',
          role: payload['role'] as String? ?? 'FOUNDER',
          name: payload['name'] as String? ?? payload['sub'] as String? ?? '',
        );
        emit(AuthAuthenticated(user));
      } else {
        await _api.clearTokens();
        emit(AuthUnauthenticated());
      }
    } else {
      emit(AuthUnauthenticated());
    }
  }

  Future<void> _onLogin(AuthLoginRequested event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      final data = await _api.post('/auth/login', {
        'email': event.email,
        'password': event.password,
      }, auth: false);
      await _api.storeTokens(data['accessToken'], data['refreshToken']);
      final user = User.fromJson(data['user']);
      emit(AuthAuthenticated(user));
    } catch (e) {
      emit(AuthError(e.toString()));
    }
  }

  Future<void> _onRegister(AuthRegisterRequested event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      final data = await _api.post('/auth/register', {
        'email': event.email,
        'password': event.password,
        'role': event.role,
        'name': event.name,
      }, auth: false);
      await _api.storeTokens(data['accessToken'], data['refreshToken']);
      final user = User.fromJson(data['user']);
      emit(AuthAuthenticated(user));
    } catch (e) {
      emit(AuthError(e.toString()));
    }
  }

  Future<void> _onGoogleLogin(AuthGoogleLoginRequested event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      // For MVP: Redirect to backend OAuth endpoint
      // In production: Use google_sign_in package to get token, then post to /api/auth/oauth
      // Simulated for now:
      final data = await _api.post('/auth/oauth', {
        'provider': 'google',
        'token': 'simulated_google_token',
      }, auth: false);
      await _api.storeTokens(data['accessToken'], data['refreshToken']);
      final user = User.fromJson(data['user']);
      emit(AuthAuthenticated(user));
    } catch (e) {
      emit(AuthError(e.toString()));
    }
  }

  Future<void> _onFacebookLogin(AuthFacebookLoginRequested event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      final data = await _api.post('/auth/oauth', {
        'provider': 'facebook',
        'token': 'simulated_fb_token',
      }, auth: false);
      await _api.storeTokens(data['accessToken'], data['refreshToken']);
      final user = User.fromJson(data['user']);
      emit(AuthAuthenticated(user));
    } catch (e) {
      emit(AuthError(e.toString()));
    }
  }

  Future<void> _onOtpSend(AuthOtpSendRequested event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      final data = await _api.post('/auth/otp/send', {
        'phoneNumber': event.phoneNumber,
      }, auth: false);
      emit(AuthOtpSent(data['message']));
    } catch (e) {
      emit(AuthError(e.toString()));
    }
  }

  Future<void> _onOtpVerify(AuthOtpVerifyRequested event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      final data = await _api.post('/auth/otp/verify', {
        'phoneNumber': event.phoneNumber,
        'otp': event.otp,
      }, auth: false);
      await _api.storeTokens(data['accessToken'], data['refreshToken']);
      final user = User.fromJson(data['user']);
      emit(AuthAuthenticated(user));
    } catch (e) {
      emit(AuthError(e.toString()));
    }
  }

  Future<void> _onLogout(AuthLogoutRequested event, Emitter<AuthState> emit) async {
    await _api.clearTokens();
    emit(AuthUnauthenticated());
  }
}
