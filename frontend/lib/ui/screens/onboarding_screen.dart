import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/project/project_bloc.dart';

class OnboardingScreen extends StatefulWidget {
  const OnboardingScreen({super.key});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {
  final _pageController = PageController();
  int _currentPage = 0;

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  void _onGetStarted() {
    // Navigate to dashboard and prompt user to create first project
    // Use pushAndRemoveUntil to clear onboarding from stack
    Navigator.pushNamedAndRemoveUntil(context, '/dashboard', (route) => false);
    
    // After a brief delay to let dashboard load, show create project dialog 
    // by dispatching a first-time-user event
    Future.delayed(const Duration(milliseconds: 500), () {
      if (context.mounted) {
        // The dashboard will check if user has projects and auto-prompt
        // This is handled by DashboardScreen's FirstTimeUserBanner
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: PageView(
                controller: _pageController,
                onPageChanged: (i) => setState(() => _currentPage = i),
                children: [
                  const _OnboardingPage(
                    icon: Icons.rocket_launch,
                    title: 'Welcome to UCTO!',
                    description: 'Build your full-stack startup app in hours, not months. Our AI agents work together to take you from idea to deployed app.',
                    step: 'Step 1 of 3',
                  ),
                  const _OnboardingPage(
                    icon: Icons.description,
                    title: 'Describe Your Idea',
                    description: 'Tell our Business Analyst agent what you want to build. The BA will clarify requirements and generate a detailed specification.',
                    step: 'Step 2 of 3',
                  ),
                  const _OnboardingPage(
                    icon: Icons.design_services,
                    title: 'Approve Screens & Deploy',
                    description: 'Preview AI-generated screens, approve or request changes. Once approved, your app is ready for deployment.',
                    step: 'Step 3 of 3',
                  ),
                ],
              ),
            ),
            // Dots
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(3, (i) => Container(
                margin: const EdgeInsets.symmetric(horizontal: 4),
                width: _currentPage == i ? 24 : 8,
                height: 8,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(4),
                  color: _currentPage == i ? const Color(0xFF7C3AED) : const Color(0xFF334155),
                ),
              )),
            ),
            const SizedBox(height: 32),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              child: SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton(
                  onPressed: () {
                    if (_currentPage < 2) {
                      _pageController.nextPage(duration: const Duration(milliseconds: 300), curve: Curves.easeInOut);
                    } else {
                      _onGetStarted();
                    }
                  },
                  child: Text(_currentPage < 2 ? 'Next' : 'Get Started'),
                ),
              ),
            ),
            if (_currentPage < 2)
              TextButton(
                onPressed: _onGetStarted,
                child: const Text('Skip'),
              ),
            const SizedBox(height: 32),
          ],
        ),
      ),
    );
  }
}

class _OnboardingPage extends StatelessWidget {
  final IconData icon;
  final String title;
  final String description;
  final String step;

  const _OnboardingPage({required this.icon, required this.title, required this.description, required this.step});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(icon, size: 72, color: const Color(0xFF7C3AED)),
          const SizedBox(height: 32),
          Text(step, style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12, letterSpacing: 1)),
          const SizedBox(height: 12),
          Text(title, style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFFF1F5F9)), textAlign: TextAlign.center),
          const SizedBox(height: 16),
          Text(description, style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 15, height: 1.5), textAlign: TextAlign.center),
        ],
      ),
    );
  }
}
