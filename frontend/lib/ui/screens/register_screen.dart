import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../blocs/auth/auth_bloc.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  String _selectedRole = 'FOUNDER';
  bool _obscurePassword = true;

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      body: SafeArea(
        child: BlocConsumer<AuthBloc, AuthState>(
          listener: (context, state) {
            if (state is AuthAuthenticated) {
              Navigator.pushReplacementNamed(context, '/onboarding');
            }
          },
          builder: (context, state) {
            return SingleChildScrollView(
              padding: const EdgeInsets.all(24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const SizedBox(height: 32),
                  const Text('Create Account', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFFF1F5F9))),
                  const SizedBox(height: 8),
                  const Text('Start building your app in minutes', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 14)),
                  const SizedBox(height: 32),

                  TextField(
                    controller: _nameController,
                    decoration: const InputDecoration(labelText: 'Full Name', hintText: 'John Doe'),
                  ),
                  const SizedBox(height: 16),

                  TextField(
                    controller: _emailController,
                    decoration: const InputDecoration(labelText: 'Email', hintText: 'john@example.com'),
                    keyboardType: TextInputType.emailAddress,
                  ),
                  const SizedBox(height: 16),

                  // Role Selector
                  DropdownButtonFormField<String>(
                    initialValue: _selectedRole,
                    decoration: const InputDecoration(labelText: 'Role'),
                    items: const [
                      DropdownMenuItem(value: 'FOUNDER', child: Text('Founder / Entrepreneur')),
                      DropdownMenuItem(value: 'DEVELOPER', child: Text('Developer')),
                      DropdownMenuItem(value: 'VIEWER', child: Text('Viewer / Stakeholder')),
                    ],
                    onChanged: (v) => setState(() => _selectedRole = v!),
                    dropdownColor: const Color(0xFF1E293B),
                  ),
                  const SizedBox(height: 16),

                  TextField(
                    controller: _passwordController,
                    decoration: InputDecoration(
                      labelText: 'Password',
                      hintText: 'Min 8 characters',
                      suffixIcon: IconButton(
                        icon: Icon(_obscurePassword ? Icons.visibility_off : Icons.visibility),
                        onPressed: () => setState(() => _obscurePassword = !_obscurePassword),
                      ),
                    ),
                    obscureText: _obscurePassword,
                  ),
                  const SizedBox(height: 16),

                  TextField(
                    controller: _confirmPasswordController,
                    decoration: const InputDecoration(labelText: 'Confirm Password', hintText: 'Re-enter password'),
                    obscureText: true,
                  ),
                  const SizedBox(height: 24),

                  SizedBox(
                    width: double.infinity,
                    height: 48,
                    child: ElevatedButton(
                      onPressed: state is AuthLoading ? null : _handleRegister,
                      child: state is AuthLoading
                          ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                          : const Text('Create Account'),
                    ),
                  ),
                  const SizedBox(height: 16),

                  TextButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('Already have an account? Sign In'),
                  ),

                  if (state is AuthError)
                    Padding(
                      padding: const EdgeInsets.only(top: 16),
                      child: Text(state.message, style: const TextStyle(color: Color(0xFFEF4444), fontSize: 13), textAlign: TextAlign.center),
                    ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  void _handleRegister() {
    if (_passwordController.text != _confirmPasswordController.text) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Passwords do not match')),
      );
      return;
    }
    context.read<AuthBloc>().add(AuthRegisterRequested(
      _emailController.text.trim(),
      _passwordController.text,
      _selectedRole,
      _nameController.text.trim(),
    ));
  }
}
