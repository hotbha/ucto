import 'package:flutter/material.dart';
import '../../services/api_service.dart';

class VerifyEmailScreen extends StatelessWidget {
  final String? token;
  const VerifyEmailScreen({super.key, this.token});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      body: FutureBuilder<String>(
        future: _verify(token),
        builder: (context, snapshot) {
          return Center(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  if (snapshot.connectionState == ConnectionState.waiting) ...[
                    const SizedBox(width: 48, height: 48, child: CircularProgressIndicator(color: Color(0xFF7C3AED))),
                    const SizedBox(height: 24),
                    const Text('Verifying your email...', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 16)),
                  ] else if (snapshot.hasError) ...[
                    const Icon(Icons.error_outline, size: 64, color: Color(0xFFEF4444)),
                    const SizedBox(height: 16),
                    const Text('Verification Failed', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Color(0xFFF1F5F9))),
                    const SizedBox(height: 8),
                    Text(snapshot.error.toString(), style: const TextStyle(color: Color(0xFF94A3B8)), textAlign: TextAlign.center),
                    const SizedBox(height: 24),
                    ElevatedButton(onPressed: () => Navigator.pushReplacementNamed(context, '/login'), child: const Text('Back to Login')),
                  ] else ...[
                    const Icon(Icons.verified, size: 64, color: Color(0xFF22C55E)),
                    const SizedBox(height: 16),
                    const Text('Email Verified!', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Color(0xFFF1F5F9))),
                    const SizedBox(height: 8),
                    const Text('Your email has been verified. You can now use all features.', style: TextStyle(color: Color(0xFF94A3B8)), textAlign: TextAlign.center),
                    const SizedBox(height: 24),
                    ElevatedButton(onPressed: () => Navigator.pushReplacementNamed(context, '/login'), child: const Text('Continue to Login')),
                  ],
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Future<String> _verify(String? token) async {
    if (token == null || token.isEmpty) {
      throw Exception('Invalid verification link');
    }
    final api = ApiService();
    await api.get('/email/verify?token=$token', auth: false);
    return 'verified';
  }
}
