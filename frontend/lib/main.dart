import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'blocs/auth/auth_bloc.dart';
import 'blocs/project/project_bloc.dart';
import 'blocs/subscription/subscription_bloc.dart';
import 'blocs/requirement/requirement_bloc.dart';
import 'services/api_service.dart';
import 'ui/theme/app_theme.dart';
import 'ui/screens/splash_screen.dart';

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
          BlocProvider(create: (ctx) => AuthBloc(ctx.read<ApiService>())..add(AuthCheckStatus())),
          BlocProvider(create: (ctx) => ProjectBloc(ctx.read<ApiService>())),
          BlocProvider(create: (ctx) => SubscriptionBloc(ctx.read<ApiService>())),
          BlocProvider(create: (ctx) => RequirementBloc(ctx.read<ApiService>())),
        ],
        child: MaterialApp(
          title: 'UCTO',
          debugShowCheckedModeBanner: false,
          theme: AppTheme.darkTheme,
          home: const SplashScreen(),
        ),
      ),
    );
  }
}
