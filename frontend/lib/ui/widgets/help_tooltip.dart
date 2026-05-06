import 'package:flutter/material.dart';

/// A reusable help tooltip widget that displays an info icon with a tooltip.
class HelpTooltip extends StatelessWidget {
  final String message;
  final IconData icon;
  final double iconSize;
  final Color? iconColor;

  const HelpTooltip({
    super.key,
    required this.message,
    this.icon = Icons.help_outline,
    this.iconSize = 18,
    this.iconColor,
  });

  @override
  Widget build(BuildContext context) {
    return Tooltip(
      message: message,
      preferBelow: true,
      verticalOffset: 24,
      decoration: BoxDecoration(
        color: const Color(0xFF1E293B),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: const Color(0xFF334155)),
      ),
      textStyle: const TextStyle(color: Color(0xFFF1F5F9), fontSize: 13),
      child: Icon(
        icon,
        size: iconSize,
        color: iconColor ?? const Color(0xFF64748B),
      ),
    );
  }
}

/// A help section widget for onboarding hints within screens.
class HelpSection extends StatelessWidget {
  final String title;
  final List<String> tips;
  final IconData icon;

  const HelpSection({
    super.key,
    required this.title,
    required this.tips,
    this.icon = Icons.lightbulb_outline,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFF7C3AED).withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: const Color(0xFF7C3AED).withValues(alpha: 0.2)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, size: 16, color: const Color(0xFF7C3AED)),
              const SizedBox(width: 8),
              Text(title, style: const TextStyle(color: Color(0xFF7C3AED), fontSize: 13, fontWeight: FontWeight.w600)),
            ],
          ),
          const SizedBox(height: 8),
          ...tips.map((tip) => Padding(
            padding: const EdgeInsets.only(bottom: 4),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('• ', style: TextStyle(color: Color(0xFF7C3AED), fontSize: 12)),
                Expanded(child: Text(tip, style: const TextStyle(color: Color(0xFFCBD5E1), fontSize: 12))),
              ],
            ),
          )),
        ],
      ),
    );
  }
}

/// Help overlay widget that displays contextual help for a given screen.
class HelpOverlay extends StatelessWidget {
  final List<HelpItem> items;

  const HelpOverlay({super.key, required this.items});

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      backgroundColor: const Color(0xFF1E293B),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      title: Row(
        children: [
          const Icon(Icons.help, color: Color(0xFF7C3AED), size: 24),
          const SizedBox(width: 8),
          const Text('Help', style: TextStyle(color: Color(0xFFF1F5F9), fontSize: 18)),
        ],
      ),
      content: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: items.map((item) => Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(item.title, style: const TextStyle(color: Color(0xFF7C3AED), fontSize: 14, fontWeight: FontWeight.w600)),
                const SizedBox(height: 4),
                Text(item.description, style: const TextStyle(color: Color(0xFFCBD5E1), fontSize: 13)),
              ],
            ),
          )).toList(),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Got it'),
        ),
      ],
    );
  }
}

class HelpItem {
  final String title;
  final String description;
  HelpItem({required this.title, required this.description});
}
