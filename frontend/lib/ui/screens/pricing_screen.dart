import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../blocs/subscription/subscription_bloc.dart';

class PricingScreen extends StatefulWidget {
  const PricingScreen({super.key});

  @override
  State<PricingScreen> createState() => _PricingScreenState();
}

class _PricingScreenState extends State<PricingScreen> {
  @override
  void initState() {
    super.initState();
    context.read<SubscriptionBloc>().add(LoadSubscription());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(title: const Text('Pricing Plans')),
      body: BlocBuilder<SubscriptionBloc, SubscriptionState>(
        builder: (context, state) {
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Choose the right plan for your startup',
                  style: TextStyle(color: Color(0xFF94A3B8), fontSize: 14),
                ),
                const SizedBox(height: 24),

                // Current plan banner
                if (state is SubscriptionLoaded)
                  _CurrentPlanBanner(usage: state.usage),

                const SizedBox(height: 16),

                // Plan Cards
                _PlanCard(
                  tier: 'FREE',
                  price: 'Free',
                  priceSub: 'forever',
                  features: ['1 project', '5 agent runs/month', 'BA + Screen generation', '1 deployment'],
                  color: const Color(0xFF22C55E),
                  isCurrent: state is SubscriptionLoaded && state.usage.tier == 'FREE',
                  onUpgrade: null,
                ),
                const SizedBox(height: 12),

                _PlanCard(
                  tier: 'STARTUP',
                  price: '₹2,499/mo',
                  priceSub: '≈ \$29/mo',
                  features: ['5 projects', '50 agent runs/month', 'All agents', 'Email support', 'Basic audit logs'],
                  color: const Color(0xFF3B82F6),
                  isPopular: true,
                  isCurrent: state is SubscriptionLoaded && (state.usage.tier == 'STARTUP' || state.usage.tier == 'STARTUP_TRIAL'),
                  onUpgrade: () => _handleUpgrade('STARTUP'),
                ),
                const SizedBox(height: 12),

                _PlanCard(
                  tier: 'GROWTH',
                  price: '₹7,999/mo',
                  priceSub: '≈ \$99/mo',
                  features: ['50 projects', '200 agent runs/month', 'Compliance (DPDP/GDPR)', 'Advanced audit', 'Priority support', 'Custom domain'],
                  color: const Color(0xFFF59E0B),
                  isCurrent: state is SubscriptionLoaded && state.usage.tier == 'GROWTH',
                  onUpgrade: () => _handleUpgrade('GROWTH'),
                ),
                const SizedBox(height: 12),

                _PlanCard(
                  tier: 'ENTERPRISE',
                  price: 'Custom',
                  priceSub: 'from ₹49,999/mo',
                  features: ['Unlimited projects', 'Unlimited agent runs', 'Dedicated infra', 'SLA', 'Custom integrations', 'White-glove support'],
                  color: const Color(0xFF8B5CF6),
                  isCurrent: state is SubscriptionLoaded && state.usage.tier == 'ENTERPRISE',
                  onUpgrade: () => _handleUpgrade('ENTERPRISE'),
                ),
                const SizedBox(height: 24),

                // Trial
                if (state is SubscriptionLoaded && state.usage.tier == 'FREE')
                  SizedBox(
                    width: double.infinity,
                    height: 48,
                    child: ElevatedButton.icon(
                      onPressed: () => context.read<SubscriptionBloc>().add(StartTrial()),
                      icon: const Icon(Icons.free_breakfast),
                      label: const Text('Start 14-Day Free Trial'),
                    ),
                  ),
              ],
            ),
          );
        },
      ),
    );
  }

  void _handleUpgrade(String tier) {
    context.read<SubscriptionBloc>().add(UpgradeSubscription(tier));
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Upgrading to $tier... (Chargebee integration pending)')),
    );
  }
}

class _CurrentPlanBanner extends StatelessWidget {
  final dynamic usage;
  const _CurrentPlanBanner({required this.usage});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Current Plan', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                _TierBadge(tier: usage.tier),
              ],
            ),
            const SizedBox(height: 12),
            Text('${usage.runsUsed}/${usage.maxAgentRuns} agent runs this month', style: TextStyle(fontSize: 13, color: Color(0xFFF1F5F9))),
            const SizedBox(height: 4),
            ClipRRect(
              borderRadius: BorderRadius.circular(4),
              child: LinearProgressIndicator(
                value: usage.maxAgentRuns > 0 ? usage.runsUsed / usage.maxAgentRuns : 0,
                backgroundColor: const Color(0xFF334155),
                color: const Color(0xFF7C3AED),
                minHeight: 6,
              ),
            ),
            const SizedBox(height: 8),
            Text('${usage.projectsUsed}/${usage.maxProjects} projects', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
          ],
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
    final colors = {'FREE': const Color(0xFF22C55E), 'STARTUP': const Color(0xFF3B82F6), 'STARTUP_TRIAL': const Color(0xFF3B82F6), 'GROWTH': const Color(0xFFF59E0B), 'ENTERPRISE': const Color(0xFF8B5CF6)};
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

class _PlanCard extends StatelessWidget {
  final String tier;
  final String price;
  final String priceSub;
  final List<String> features;
  final Color color;
  final bool isPopular;
  final bool isCurrent;
  final VoidCallback? onUpgrade;

  const _PlanCard({
    required this.tier,
    required this.price,
    required this.priceSub,
    required this.features,
    required this.color,
    this.isPopular = false,
    this.isCurrent = false,
    this.onUpgrade,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Container(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(12),
          border: isPopular ? Border.all(color: color, width: 2) : null,
        ),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Text(tier, style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: color)),
                    if (isPopular) ...[
                      const SizedBox(width: 8),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(color: color.withOpacity(0.2), borderRadius: BorderRadius.circular(12)),
                        child: Text('Popular', style: TextStyle(fontSize: 10, color: color, fontWeight: FontWeight.w600)),
                      ),
                    ],
                    if (isCurrent) ...[
                      const SizedBox(width: 8),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(color: const Color(0xFF22C55E).withOpacity(0.2), borderRadius: BorderRadius.circular(12)),
                        child: const Text('Current', style: TextStyle(fontSize: 10, color: Color(0xFF22C55E), fontWeight: FontWeight.w600)),
                      ),
                    ],
                  ],
                ),
              ],
            ),
            const SizedBox(height: 12),
            Text(price, style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFFF1F5F9))),
            Text(priceSub, style: TextStyle(fontSize: 12, color: Color(0xFF94A3B8))),
            const SizedBox(height: 16),
            ...features.map((f) => Padding(
                  padding: const EdgeInsets.only(bottom: 8),
                  child: Row(
                    children: [
                      Icon(Icons.check, size: 16, color: color),
                      const SizedBox(width: 8),
                      Text(f, style: const TextStyle(fontSize: 13, color: Color(0xFFF1F5F9))),
                    ],
                  ),
                )),
            if (onUpgrade != null && !isCurrent) ...[
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                height: 44,
                child: ElevatedButton(
                  onPressed: onUpgrade,
                  style: ElevatedButton.styleFrom(backgroundColor: color),
                  child: Text(tier == 'ENTERPRISE' ? 'Contact Sales' : 'Upgrade'),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
