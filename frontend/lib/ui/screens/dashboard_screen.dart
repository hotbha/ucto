import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/subscription/subscription_bloc.dart';
import '../../blocs/project/project_bloc.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  @override
  void initState() {
    super.initState();
    context.read<SubscriptionBloc>().add(LoadSubscription());
    context.read<ProjectBloc>().add(LoadProjects());
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F172A),
        title: const Text('UCTO Dashboard'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout, color: Color(0xFF94A3B8)),
            onPressed: () => context.read<AuthBloc>().add(AuthLogoutRequested()),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Subscription Banner
            BlocBuilder<SubscriptionBloc, SubscriptionState>(
              builder: (context, state) {
                if (state is SubscriptionLoaded) {
                  return Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Row(
                        children: [
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Text('Plan: ', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                                    _TierBadge(tier: state.usage.tier),
                                  ],
                                ),
                                const SizedBox(height: 8),
                                Text('${state.usage.runsUsed}/${state.usage.maxAgentRuns} agent runs used', style: const TextStyle(fontSize: 13)),
                                const SizedBox(height: 4),
                                ClipRRect(
                                  borderRadius: BorderRadius.circular(4),
                                  child: LinearProgressIndicator(
                                    value: state.usage.maxAgentRuns > 0 ? state.usage.runsUsed / state.usage.maxAgentRuns : 0,
                                    backgroundColor: const Color(0xFF334155),
                                    color: const Color(0xFF7C3AED),
                                    minHeight: 6,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          if (state.usage.needsUpgrade)
                            TextButton(
                              onPressed: () => Navigator.pushNamed(context, '/subscription'),
                              child: const Text('Upgrade'),
                            ),
                        ],
                      ),
                    ),
                  );
                }
                return const SizedBox.shrink();
              },
            ),
            const SizedBox(height: 20),

            // Quick Actions
            const Text('Quick Actions', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFFF1F5F9))),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(child: _ActionCard(icon: Icons.add_circle_outline, label: 'New Project', color: const Color(0xFF7C3AED), onTap: () => _showCreateProjectDialog(context))),
                const SizedBox(width: 12),
                Expanded(child: _ActionCard(icon: Icons.rocket_launch_outlined, label: 'Run Agent', color: const Color(0xFFF59E0B), onTap: () => _showAgentDialog(context))),
                const SizedBox(width: 12),
                Expanded(child: _ActionCard(icon: Icons.account_tree_outlined, label: 'My Projects', color: const Color(0xFF22C55E), onTap: () => {})),
              ],
            ),
            const SizedBox(height: 24),

            // Recent Activity
            const Text('Recent Activity', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFFF1F5F9))),
            const SizedBox(height: 12),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Center(
                  child: Column(
                    children: [
                      Icon(Icons.inbox_outlined, size: 40, color: Color(0xFF334155)),
                      const SizedBox(height: 8),
                      const Text('No recent activity', style: TextStyle(color: Color(0xFF94A3B8))),
                      const SizedBox(height: 4),
                      const Text('Create a project to get started', style: TextStyle(color: Color(0xFF64748B), fontSize: 13)),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Pricing Button
            Center(
              child: TextButton.icon(
                onPressed: () => Navigator.pushNamed(context, '/subscription'),
                icon: const Icon(Icons.credit_card),
                label: const Text('View Pricing & Upgrade'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showCreateProjectDialog(BuildContext context) {
    final titleCtrl = TextEditingController();
    final descCtrl = TextEditingController();
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: const Color(0xFF1E293B),
        title: const Text('Create New Project', style: TextStyle(color: Color(0xFFF1F5F9))),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: titleCtrl,
              decoration: const InputDecoration(labelText: 'Project Title', hintText: 'My Awesome App'),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: descCtrl,
              decoration: const InputDecoration(labelText: 'Description', hintText: 'What does your app do?'),
              maxLines: 3,
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () {
              context.read<ProjectBloc>().add(CreateProject(titleCtrl.text, descCtrl.text));
              Navigator.pop(ctx);
            },
            child: const Text('Create'),
          ),
        ],
      ),
    );
  }

  void _showAgentDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: const Color(0xFF1E293B),
        title: const Text('Run Agent', style: TextStyle(color: Color(0xFFF1F5F9))),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: ['BA', 'Developer', 'Tester', 'Compliance', 'UI/UX', 'Architect'].map((agent) {
            final icons = {
              'BA': Icons.person_search,
              'Developer': Icons.code,
              'Tester': Icons.check_circle_outline,
              'Compliance': Icons.shield_outlined,
              'UI/UX': Icons.design_services,
              'Architect': Icons.account_tree,
            };
            return ListTile(
              leading: Icon(icons[agent] ?? Icons.smart_toy, color: const Color(0xFF7C3AED)),
              title: Text('$agent Agent', style: const TextStyle(color: Color(0xFFF1F5F9))),
              onTap: () {
                Navigator.pop(ctx);
                ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('$agent Agent triggered')));
              },
            );
          }).toList(),
        ),
      ),
    );
  }
}

class _TierBadge extends StatelessWidget {
  final String tier;
  const _TierBadge({required this.tier});

  @override
  Widget build(BuildContext context) {
    final colors = {
      'FREE': const Color(0xFF22C55E),
      'STARTUP': const Color(0xFF3B82F6),
      'STARTUP_TRIAL': const Color(0xFF3B82F6),
      'GROWTH': const Color(0xFFF59E0B),
      'ENTERPRISE': const Color(0xFF8B5CF6),
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: (colors[tier] ?? const Color(0xFF22C55E)).withOpacity(0.2),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: (colors[tier] ?? const Color(0xFF22C55E)).withOpacity(0.3)),
      ),
      child: Text(tier.replaceAll('_', ' '), style: TextStyle(fontSize: 11, fontWeight: FontWeight.w500, color: colors[tier] ?? const Color(0xFF22C55E))),
    );
  }
}

class _ActionCard extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;

  const _ActionCard({required this.icon, required this.label, required this.color, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              Icon(icon, color: color, size: 28),
              const SizedBox(height: 8),
              Text(label, style: const TextStyle(fontSize: 12, color: Color(0xFF94A3B8)), textAlign: TextAlign.center),
            ],
          ),
        ),
      ),
    );
  }
}
