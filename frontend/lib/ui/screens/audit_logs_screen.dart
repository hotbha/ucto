import 'package:flutter/material.dart';

class AuditLogsScreen extends StatelessWidget {
  const AuditLogsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(title: const Text('Audit Logs')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.history, size: 48, color: Color(0xFF334155)),
            const SizedBox(height: 12),
            const Text('Audit logs available on paid plans', style: TextStyle(color: Color(0xFF94A3B8))),
            const SizedBox(height: 4),
            const Text('Upgrade to Startup or higher to view audit logs', style: TextStyle(color: Color(0xFF64748B), fontSize: 13)),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => Navigator.pushNamed(context, '/subscription'),
              child: const Text('Upgrade Now'),
            ),
          ],
        ),
      ),
    );
  }
}
