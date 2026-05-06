import 'package:flutter/material.dart';
import '../../services/api_service.dart';

class ForgotPasswordScreen extends StatefulWidget {
  const ForgotPasswordScreen({super.key});

  @override
  State<ForgotPasswordScreen> createState() => _ForgotPasswordScreenState();
}

class _ForgotPasswordScreenState extends State<ForgotPasswordScreen> {
  final _emailController = TextEditingController();
  bool _loading = false;
  bool _sent = false;

  @override
  void dispose() {
    _emailController.dispose();
    super.dispose();
  }

  Future<void> _sendResetLink() async {
    setState(() => _loading = true);
    try {
      final api = ApiService();
      await api.post('/email/forgot-password', {
        'email': _emailController.text.trim(),
      }, auth: false);
      setState(() => _sent = true);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    } finally {
      setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F172A),
        title: const Text('Reset Password'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const SizedBox(height: 32),
            const Icon(Icons.lock_reset, size: 64, color: Color(0xFF7C3AED)),
            const SizedBox(height: 24),
            const Text(
              'Forgot Password?',
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Color(0xFFF1F5F9)),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            const Text(
              'Enter your email and we\'ll send you a reset link.',
              style: TextStyle(color: Color(0xFF94A3B8), fontSize: 14),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 32),
            if (!_sent) ...[
              TextField(
                controller: _emailController,
                decoration: const InputDecoration(
                  labelText: 'Email',
                  hintText: 'your@email.com',
                ),
                keyboardType: TextInputType.emailAddress,
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton(
                  onPressed: _loading ? null : _sendResetLink,
                  child: _loading
                      ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                      : const Text('Send Reset Link'),
                ),
              ),
            ] else ...[
              const Icon(Icons.check_circle_outline, size: 64, color: Color(0xFF22C55E)),
              const SizedBox(height: 16),
              const Text(
                'Check your email',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600, color: Color(0xFFF1F5F9)),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 8),
              Text(
                'A password reset link has been sent to ${_emailController.text.trim()}',
                style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 14),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: OutlinedButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Back to Sign In'),
                ),
              ),
            ],
            const SizedBox(height: 16),
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Back to Login'),
            ),
          ],
        ),
      ),
    );
  }
}
