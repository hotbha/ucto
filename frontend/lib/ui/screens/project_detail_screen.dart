import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../blocs/project/project_bloc.dart';

class ProjectDetailScreen extends StatelessWidget {
  final int projectId;
  const ProjectDetailScreen({super.key, required this.projectId});

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 4,
      child: Scaffold(
        backgroundColor: const Color(0xFF0F172A),
        appBar: AppBar(
          title: const Text('Project'),
          bottom: const TabBar(
            labelColor: Color(0xFF7C3AED),
            unselectedLabelColor: Color(0xFF94A3B8),
            indicatorColor: Color(0xFF7C3AED),
            tabs: [
              Tab(text: 'Overview'),
              Tab(text: 'Requirements'),
              Tab(text: 'Screens'),
              Tab(text: 'Deploy'),
            ],
          ),
        ),
        body: TabBarView(
          children: [
            _OverviewTab(projectId: projectId),
            _RequirementsTab(projectId: projectId),
            _ScreensTab(projectId: projectId),
            _DeployTab(projectId: projectId),
          ],
        ),
      ),
    );
  }
}

class _OverviewTab extends StatelessWidget {
  final int projectId;
  const _OverviewTab({required this.projectId});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      const Text('Status: ', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13)),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(
                          color: const Color(0xFF22C55E).withOpacity(0.2),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(color: const Color(0xFF22C55E).withOpacity(0.3)),
                        ),
                        child: const Text('Active', style: TextStyle(color: Color(0xFF22C55E), fontSize: 12)),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  const Text('Project progress', style: TextStyle(color: Color(0xFFF1F5F9), fontWeight: FontWeight.w600)),
                  const SizedBox(height: 8),
                  _StatRow(label: 'Requirements', value: '0'),
                  _StatRow(label: 'Screens Generated', value: '0'),
                  _StatRow(label: 'Agent Runs', value: '0'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            height: 48,
            child: ElevatedButton.icon(
              onPressed: () {},
              icon: const Icon(Icons.play_arrow),
              label: const Text('Run BA Agent'),
            ),
          ),
          const SizedBox(height: 12),
          SizedBox(
            width: double.infinity,
            height: 48,
            child: OutlinedButton.icon(
              onPressed: () {},
              icon: const Icon(Icons.design_services),
              label: const Text('Generate Screens'),
              style: OutlinedButton.styleFrom(
                foregroundColor: const Color(0xFFF1F5F9),
                side: const BorderSide(color: Color(0xFF334155)),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _StatRow extends StatelessWidget {
  final String label;
  final String value;
  const _StatRow({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 13)),
          Text(value, style: const TextStyle(color: Color(0xFFF1F5F9), fontWeight: FontWeight.w500)),
        ],
      ),
    );
  }
}

class _RequirementsTab extends StatelessWidget {
  final int projectId;
  const _RequirementsTab({required this.projectId});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.description_outlined, size: 48, color: Color(0xFF334155)),
          const SizedBox(height: 12),
          const Text('No requirements yet', style: TextStyle(color: Color(0xFF94A3B8))),
          const SizedBox(height: 8),
          ElevatedButton.icon(
            onPressed: () => _showAddRequirementDialog(context),
            icon: const Icon(Icons.add, size: 18),
            label: const Text('Add Requirement'),
          ),
        ],
      ),
    );
  }

  void _showAddRequirementDialog(BuildContext context) {
    final titleCtrl = TextEditingController();
    final descCtrl = TextEditingController();
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: const Color(0xFF1E293B),
        title: const Text('Add Requirement'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(controller: titleCtrl, decoration: const InputDecoration(labelText: 'Title')),
            const SizedBox(height: 12),
            TextField(controller: descCtrl, decoration: const InputDecoration(labelText: 'Description'), maxLines: 3),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () {
              context.read<ProjectBloc>().add(CreateProject(titleCtrl.text, descCtrl.text));
              Navigator.pop(ctx);
            },
            child: const Text('Add'),
          ),
        ],
      ),
    );
  }
}

class _ScreensTab extends StatelessWidget {
  final int projectId;
  const _ScreensTab({required this.projectId});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.monitor_heart_outlined, size: 48, color: Color(0xFF334155)),
          const SizedBox(height: 12),
          const Text('No screens generated', style: TextStyle(color: Color(0xFF94A3B8))),
          const SizedBox(height: 4),
          const Text('Add requirements and run BA Agent first', style: TextStyle(color: Color(0xFF64748B), fontSize: 13)),
        ],
      ),
    );
  }
}

class _DeployTab extends StatelessWidget {
  final int projectId;
  const _DeployTab({required this.projectId});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Deployment', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600, color: Color(0xFFF1F5F9))),
                  const SizedBox(height: 12),
                  const Text('Your app will be deployed when ready.', style: TextStyle(color: Color(0xFF94A3B8))),
                  const SizedBox(height: 16),
                  SizedBox(
                    width: double.infinity,
                    height: 48,
                    child: ElevatedButton.icon(
                      onPressed: () {},
                      icon: const Icon(Icons.cloud_upload),
                      label: const Text('Deploy to Staging'),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
