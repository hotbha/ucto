import 'package:flutter/material.dart';

class AgentCard extends StatelessWidget {
  final String name;
  final IconData icon;
  final String description;
  final Color color;
  final bool enabled;
  final VoidCallback? onTap;

  const AgentCard({
    super.key,
    required this.name,
    required this.icon,
    required this.description,
    required this.color,
    this.enabled = true,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: enabled ? onTap : null,
      child: Opacity(
        opacity: enabled ? 1.0 : 0.5,
        child: Card(
          child: Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(12),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        color: color.withValues(alpha: 0.2),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: Icon(icon, color: color, size: 20),
                    ),
                    const Spacer(),
                    if (!enabled)
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: const Color(0xFFF59E0B).withValues(alpha: 0.2),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: const Text('LOCKED',
                            style: TextStyle(
                                color: Color(0xFFF59E0B),
                                fontSize: 9,
                                fontWeight: FontWeight.w600)),
                      ),
                  ],
                ),
                const SizedBox(height: 12),
                Text(name,
                    style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                        color: Color(0xFFF1F5F9))),
                const SizedBox(height: 4),
                Text(description,
                    style:
                        const TextStyle(fontSize: 11, color: Color(0xFF94A3B8)),
                    maxLines: 2),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
