import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:ucto_frontend/services/api_service.dart';

void main() {
  group('ApiService JWT decode', () {
    late ApiService apiService;

    setUp(() {
      apiService = ApiService();
    });

    test('decodeJwt returns null for invalid token', () {
      expect(apiService.decodeJwt('not-a-jwt'), isNull);
    });

    test('decodeJwt returns null for malformed token', () {
      expect(apiService.decodeJwt('header.payload'), isNull);
    });

    test('decodeJwt returns null for empty string', () {
      expect(apiService.decodeJwt(''), isNull);
    });

    test('decodeJwt extracts userId from valid token', () {
      // Create a minimal valid JWT-like token
      final header = _base64UrlEncode('{"alg":"HS256"}');
      final payload = _base64UrlEncode('{"userId":1,"sub":"test@test.com","role":"FOUNDER"}');
      final signature = _base64UrlEncode('signature');
      final token = '$header.$payload.$signature';

      final result = apiService.decodeJwt(token);
      expect(result, isNotNull);
      expect(result!['userId'], 1);
      expect(result['sub'], 'test@test.com');
      expect(result['role'], 'FOUNDER');
    });

    test('decodeJwt handles null-like payload gracefully', () {
      final header = _base64UrlEncode('{"alg":"HS256"}');
      final token = '$header..signature';
      expect(apiService.decodeJwt(token), isNull);
    });
  });

  group('ApiException', () {
    test('toString returns the message', () {
      final exception = ApiException('Not found', 404);
      expect(exception.toString(), 'Not found');
    });

    test('stores statusCode correctly', () {
      final exception = ApiException('Server error', 500);
      expect(exception.statusCode, 500);
    });

    test('stores message correctly', () {
      final exception = ApiException('Bad request', 400);
      expect(exception.message, 'Bad request');
    });
  });
}

String _base64UrlEncode(String data) {
  return base64Url.encode(utf8.encode(data));
}
