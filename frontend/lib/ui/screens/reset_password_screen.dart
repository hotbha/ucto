import 'package:flutter/material.dart';
import '../../services/api_service.dart';

class ResetPasswordScreen extends StatefulWidget {
  final String token;
  const ResetPasswordScreen({super.key, required this.token});

  @override
  State<ResetPasswordScreen> createState() => _ResetPasswordScreenState();
}

class _ResetPasswordScreenState extends State<ResetPasswordScreen> {
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  bool _loading = false;
  bool _success = false;
  bool _obscure = true;

  @override
  void dispose() {
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  Future<void> _resetPassword() async {
    if (_passwordController.text != _confirmPasswordController.text) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Passwords do not match')),
      );
      return;
    }
    setState(() => _loading = true);
    try {
      final api = ApiService();
      await api.post('/email/reset-password', {
        'token': widget.token,
        'newPassword': _passwordController.text,
      }, auth: false);
      setState(() => _success = true);
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
        title: const Text('Set New Password'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const SizedBox(height: 32),
            if (!_success) ...[
              const Icon(Icons.password, size: 64, color: Color(0xFF7C3AED)),
              const SizedBox(height: 24),
              const Text(
                'Enter your new password',
                style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Color(0xFFF1F5F9)),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 24),
              TextField(
                controller: _passwordController,
                decoration: InputDecoration(
                  labelText: 'New Password',
                  hintText: 'Min 8 characters',
                  suffixIcon: IconButton(
                    icon: Icon(_obscure ? Icons.visibility_off : Icons.visibility),
                    onPressed: () => setState(() => _obscure = !_obscure),
                  ),
                ),
                obscureText: _obscure,
              ),
              const SizedBox(height: 16),
              TextField(
                controller: _confirmPasswordController,
                decoration: const InputDecoration(labelText: 'Confirm Password', hintText: 'Re-enter new password'),
                obscureText: true,
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton(
                  onPressed: _loading ? null : _resetPassword,
                  child: _loading
                      ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                      : const Text('Reset Password'),
                ),
              ),
            ] else ...[
              const Icon(Icons.check_circle_outline, size: 64, color: Color(0xFF22C55E)),
              const SizedBox(height: 16),
              const Text(
                'Password Reset Successful!',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600, color: Color(0xFFF1F5F9)),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton(
                  onPressed: () => Navigator.pushNamedAndRemoveUntil(context, '/login', (route) => false),
                  child: const Text('Back to Sign In'),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
