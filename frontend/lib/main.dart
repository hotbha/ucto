import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:ucto_frontend/ui/screens/ba_chat_screen.dart';
import 'package:ucto_frontend/ui/screens/dashboard_screen.dart';
import 'package:ucto_frontend/ui/screens/login_screen.dart';
import 'package:ucto_frontend/ui/screens/register_screen.dart';
import 'package:ucto_frontend/ui/screens/splash_screen.dart';
import 'package:ucto_frontend/ui/screens/onboarding_screen.dart';
import 'package:ucto_frontend/ui/screens/pricing_screen.dart';
import 'package:ucto_frontend/ui/screens/project_detail_screen.dart';
import 'package:ucto_frontend/ui/screens/audit_logs_screen.dart';
import 'package:ucto_frontend/ui/screens/forgot_password_screen.dart';
import 'package:ucto_frontend/ui/screens/reset_password_screen.dart';
import 'package:ucto_frontend/ui/screens/verify_email_screen.dart';
import 'blocs/auth/auth_bloc.dart';
import 'blocs/ba_chat/ba_chat_bloc.dart';
import 'blocs/project/project_bloc.dart';
import 'blocs/subscription/subscription_bloc.dart';
import 'blocs/requirement/requirement_bloc.dart';
import 'blocs/screen/screen_bloc.dart';
import 'services/api_service.dart';
import 'ui/theme/app_theme.dart';

void main() {
  runApp(const UctoApp());
}

class UctoApp extends StatelessWidget {
  const UctoApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiRepositoryProvider(
      providers: [
        RepositoryProvider(create: (_) => ApiService()),
      ],
      child: MultiBlocProvider(
        providers: [
          BlocProvider(
              create: (ctx) =>
                  AuthBloc(ctx.read<ApiService>())..add(AuthCheckStatus())),
          BlocProvider(create: (ctx) => ProjectBloc(ctx.read<ApiService>())),
          BlocProvider(
              create: (ctx) => SubscriptionBloc(ctx.read<ApiService>())),
          BlocProvider(
              create: (ctx) => RequirementBloc(ctx.read<ApiService>())),
          BlocProvider(
              create: (ctx) => ScreenBloc(ctx.read<ApiService>())),
          BlocProvider(
              create: (ctx) => BAChatBloc(ctx.read<ApiService>())),
        ],
        child: MaterialApp(
          title: 'UCTO',
          debugShowCheckedModeBanner: false,
          theme: AppTheme.darkTheme,
          home: const SplashScreen(),
          routes: {
            '/dashboard': (ctx) => const DashboardScreen(),
            '/login': (ctx) => const LoginScreen(),
            '/register': (ctx) => const RegisterScreen(),
            '/onboarding': (ctx) => const OnboardingScreen(),
            '/subscription': (ctx) => const PricingScreen(),
            '/audit-logs': (ctx) => const AuditLogsScreen(),
          },
          onGenerateRoute: (settings) {
            // Handle parameterized routes like /project/123
            if (settings.name != null && settings.name!.startsWith('/project/')) {
              final id = int.tryParse(settings.name!.split('/').last);
              if (id != null) {
                return MaterialPageRoute(
                  builder: (ctx) => ProjectDetailScreen(projectId: id),
                  settings: settings,
                );
              }
            }
            // Handle /verify-email?token=xxx
            if (settings.name == '/verify-email') {
              final token = (settings.arguments as Map<String, dynamic>?)?['token'] as String?;
              return MaterialPageRoute(
                builder: (ctx) => VerifyEmailScreen(token: token),
                settings: settings,
              );
            }
            // Handle /reset-password?token=xxx
            if (settings.name == '/reset-password') {
              final token = (settings.arguments as Map<String, dynamic>?)?['token'] as String? ?? '';
              return MaterialPageRoute(
                builder: (ctx) => ResetPasswordScreen(token: token),
                settings: settings,
              );
            }
            // Handle /ba-chat/:projectId route
            if (settings.name != null && settings.name!.startsWith('/ba-chat/')) {
              final parts = settings.name!.split('/');
              final id = int.tryParse(parts[2]);
              final title = parts.length > 3 ? Uri.decodeComponent(parts[3]) : 'Project';
              if (id != null) {
                return MaterialPageRoute(
                  builder: (ctx) => BAChatScreen(projectId: id, projectTitle: title),
                  settings: settings,
                );
              }
            }
            // Named routes
            final routes = <String, WidgetBuilder>{
              '/forgot-password': (ctx) => const ForgotPasswordScreen(),
              '/verify-email': (ctx) => const VerifyEmailScreen(),
              '/reset-password': (ctx) => const ResetPasswordScreen(token: ''),
            };
            final route = routes[settings.name];
            if (route != null) {
              return MaterialPageRoute(
                builder: (ctx) => route(ctx),
                settings: settings,
              );
            }
            // Default to dashboard
            return MaterialPageRoute(
              builder: (ctx) => const DashboardScreen(),
              settings: settings,
            );
          },
        ),
      ),  
    );
  }
}
