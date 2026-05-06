class SubscriptionPlan {
  final String tier;
  final int price;
  final int maxProjects;
  final int maxAgentRuns;
  final bool hasAudit;
  final bool hasCompliance;
  final bool hasPrioritySupport;

  SubscriptionPlan({
    required this.tier,
    required this.price,
    required this.maxProjects,
    required this.maxAgentRuns,
    required this.hasAudit,
    required this.hasCompliance,
    required this.hasPrioritySupport,
  });

  factory SubscriptionPlan.fromJson(Map<String, dynamic> json) {
    return SubscriptionPlan(
      tier: json['tier'] as String,
      price: json['price'] as int,
      maxProjects: json['maxProjects'] as int,
      maxAgentRuns: json['maxAgentRuns'] as int,
      hasAudit: json['hasAudit'] as bool? ?? false,
      hasCompliance: json['hasCompliance'] as bool? ?? false,
      hasPrioritySupport: json['hasPrioritySupport'] as bool? ?? false,
    );
  }

  String get formattedPrice {
    if (price == 0) return 'Free';
    return '₹${price ~/ 100},${price % 100 == 0 ? (price % 100).toString().padLeft(2, '0') : (price % 100).toString()}';
  }

  String get formattedPriceUSD {
    if (price == 0) return '\$0';
    final usd = (price / 85).round();
    return '\$$usd';
  }
}
