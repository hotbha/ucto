import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../blocs/project/project_bloc.dart';
import '../../blocs/requirement/requirement_bloc.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/screen/screen_bloc.dart';
import '../../models/project.dart';
import '../../models/screen_model.dart';
import '../../models/requirement.dart';

class ProjectDetailScreen extends StatefulWidget {
  final int projectId;
  const ProjectDetailScreen({super.key, required this.projectId});

  @override
  State<ProjectDetailScreen> createState() => _ProjectDetailScreenState();
}

class _ProjectDetailScreenState extends State<ProjectDetailScreen> {
  @override
  void initState() {
    super.initState();
    context.read<ProjectBloc>().add(LoadProjects());
    context.read<RequirementBloc>().add(LoadRequirements(widget.projectId));
  }

  @override
  Widget build(BuildContext context) {
    final projectId = widget.projectId;
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
                          color: const Color(0xFF22C55E).withValues(alpha: 0.2),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(color: const Color(0xFF22C55E).withValues(alpha: 0.3)),
                        ),
                        child: const Text('Active', style: TextStyle(color: Color(0xFF22C55E), fontSize: 12)),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  const Text('Project progress', style: TextStyle(color: Color(0xFFF1F5F9), fontWeight: FontWeight.w600)),
                  const SizedBox(height: 8),
                  BlocBuilder<RequirementBloc, RequirementState>(
                    builder: (context, reqState) {
                      int reqCount = 0;
                      if (reqState is RequirementsLoaded) {
                        reqCount = reqState.requirements.length;
                      }
                      return Column(
                        children: [
                          _StatRow(label: 'Requirements', value: reqCount.toString()),
                        ],
                      );
                    },
                  ),
                  _StatRow(label: 'Screens Generated', value: 'TBD'),
                  _StatRow(label: 'Agent Runs', value: 'TBD'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            height: 48,
            child: ElevatedButton.icon(
              onPressed: () {
                // Trigger BA Agent - dispatch event to RequirementBloc to clarify
                final state = context.read<RequirementBloc>().state;
                if (state is RequirementsLoaded && state.requirements.isNotEmpty) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('BA Agent triggered (requires backend running)')),
                  );
                } else {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Add requirements first before running BA Agent')),
                  );
                }
              },
              icon: const Icon(Icons.play_arrow),
              label: const Text('Run BA Agent'),
            ),
          ),
          const SizedBox(height: 12),
          SizedBox(
            width: double.infinity,
            height: 48,
            child: OutlinedButton.icon(
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Screen generation triggered (requires backend running)')),
                );
              },
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
    return BlocBuilder<RequirementBloc, RequirementState>(
      builder: (context, state) {
        if (state is RequirementLoading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (state is RequirementsLoaded && state.requirements.isNotEmpty) {
          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: state.requirements.length,
            itemBuilder: (context, index) {
              final req = state.requirements[index];
              return Card(
                margin: const EdgeInsets.only(bottom: 8),
                child: ListTile(
                  title: Text(req.title, style: const TextStyle(color: Color(0xFFF1F5F9))),
                  subtitle: Text('Status: ${req.status}', style: const TextStyle(color: Color(0xFF94A3B8))),
                  trailing: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                    decoration: BoxDecoration(
                      color: const Color(0xFF7C3AED).withValues(alpha: 0.2),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(req.status, style: const TextStyle(color: Color(0xFF7C3AED), fontSize: 11)),
                  ),
                ),
              );
            },
          );
        }
        return Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.description_outlined, size: 48, color: Color(0xFF334155)),
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
      },
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
              context.read<RequirementBloc>().add(CreateRequirement(projectId, titleCtrl.text, descCtrl.text));
              Navigator.pop(ctx);
            },
            child: const Text('Add'),
          ),
        ],
      ),
    );
  }
}

class _ScreensTab extends StatefulWidget {
  final int projectId;
  const _ScreensTab({required this.projectId});

  @override
  State<_ScreensTab> createState() => _ScreensTabState();
}

class _ScreensTabState extends State<_ScreensTab> {
  @override
  void initState() {
    super.initState();
    context.read<ScreenBloc>().add(LoadScreens(widget.projectId));
  }

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<ScreenBloc, ScreenState>(
      listener: (context, state) {
        if (state is ScreenActionSuccess) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(state.message)),
          );
          // Reload screens after action
          context.read<ScreenBloc>().add(LoadScreens(widget.projectId));
        } else if (state is ScreenError) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(state.message), backgroundColor: const Color(0xFFEF4444)),
          );
        }
      },
      builder: (context, state) {
        if (state is ScreenLoading) {
          return const Center(child: CircularProgressIndicator(color: Color(0xFF7C3AED)));
        }
        if (state is ScreensLoaded && state.screens.isNotEmpty) {
          return Column(
            children: [
              // Generate Screens Button
              Padding(
                padding: const EdgeInsets.all(16),
                child: SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: ElevatedButton.icon(
                    onPressed: () => context.read<ScreenBloc>().add(GenerateScreens(widget.projectId)),
                    icon: const Icon(Icons.auto_awesome, size: 18),
                    label: const Text('Generate Screens'),
                  ),
                ),
              ),
              Expanded(
                child: ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  itemCount: state.screens.length,
                  itemBuilder: (context, index) {
                    final screen = state.screens[index];
                    return _ScreenPreviewCard(screen: screen);
                  },
                ),
              ),
            ],
          );
        }
        return Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.monitor_heart_outlined, size: 48, color: Color(0xFF334155)),
              const SizedBox(height: 12),
              const Text('No screens generated', style: TextStyle(color: Color(0xFF94A3B8))),
              const SizedBox(height: 4),
              const Text('Generate screens from requirements', style: TextStyle(color: Color(0xFF64748B), fontSize: 13)),
              const SizedBox(height: 16),
              ElevatedButton.icon(
                onPressed: () => context.read<ScreenBloc>().add(GenerateScreens(widget.projectId)),
                icon: const Icon(Icons.auto_awesome, size: 18),
                label: const Text('Generate Screens'),
              ),
            ],
          ),
        );
      },
    );
  }
}

class _ScreenPreviewCard extends StatelessWidget {
  final ScreenModel screen;
  const _ScreenPreviewCard({required this.screen});

  Color _statusColor(String status) {
    switch (status) {
      case 'APPROVED': return const Color(0xFF22C55E);
      case 'REJECTED': return const Color(0xFFEF4444);
      case 'CHANGES_REQUESTED': return const Color(0xFFF59E0B);
      default: return const Color(0xFF94A3B8);
    }
  }

  IconData _statusIcon(String status) {
    switch (status) {
      case 'APPROVED': return Icons.check_circle;
      case 'REJECTED': return Icons.cancel;
      case 'CHANGES_REQUESTED': return Icons.edit;
      default: return Icons.hourglass_empty;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Screen preview placeholder
          Container(
            height: 200,
            width: double.infinity,
            decoration: BoxDecoration(
              color: const Color(0xFF1E293B),
              borderRadius: const BorderRadius.vertical(top: Radius.circular(12)),
              border: Border.all(color: const Color(0xFF334155)),
            ),
            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    screen.type == 'WIREFRAME' ? Icons.border_style :
                    screen.type == 'MOCKUP' ? Icons.palette : Icons.design_services,
                    size: 48,
                    color: const Color(0xFF334155),
                  ),
                  const SizedBox(height: 8),
                  Text(screen.type, style: const TextStyle(color: Color(0xFF64748B), fontSize: 12)),
                  if (screen.storageUrl != null) ...[
                    const SizedBox(height: 4),
                    Text(screen.storageUrl!, style: const TextStyle(color: Color(0xFF7C3AED), fontSize: 11), textAlign: TextAlign.center),
                  ],
                ],
              ),
            ),
          ),
          // Status and action buttons
          Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(_statusIcon(screen.status), size: 16, color: _statusColor(screen.status)),
                    const SizedBox(width: 6),
                    Text(screen.status, style: TextStyle(color: _statusColor(screen.status), fontSize: 12, fontWeight: FontWeight.w500)),
                    if (screen.revisionCount > 0) ...[
                      const Spacer(),
                      Text('Rev ${screen.revisionCount}', style: const TextStyle(color: Color(0xFF64748B), fontSize: 11)),
                    ],
                  ],
                ),
                if (screen.feedback != null && screen.feedback!.isNotEmpty) ...[
                  const SizedBox(height: 8),
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: const Color(0xFF1E293B),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(screen.feedback!, style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                  ),
                ],
                if (screen.status == 'PENDING') ...[
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: SizedBox(
                          height: 36,
                          child: ElevatedButton.icon(
                            onPressed: () => context.read<ScreenBloc>().add(ApproveScreen(screen.id)),
                            icon: const Icon(Icons.check, size: 16),
                            label: const Text('Approve', style: TextStyle(fontSize: 12)),
                            style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF22C55E), foregroundColor: Colors.white),
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: SizedBox(
                          height: 36,
                          child: OutlinedButton.icon(
                            onPressed: () => _showFeedbackDialog(context, screen.id),
                            icon: const Icon(Icons.close, size: 16),
                            label: const Text('Reject', style: TextStyle(fontSize: 12)),
                            style: OutlinedButton.styleFrom(foregroundColor: const Color(0xFFEF4444), side: const BorderSide(color: Color(0xFFEF4444))),
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  void _showFeedbackDialog(BuildContext context, int screenId) {
    final feedbackCtrl = TextEditingController();
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: const Color(0xFF1E293B),
        title: const Text('Feedback', style: TextStyle(color: Color(0xFFF1F5F9))),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Provide feedback for rejection or changes:', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13)),
            const SizedBox(height: 12),
            TextField(
              controller: feedbackCtrl,
              decoration: const InputDecoration(hintText: 'Describe what needs to change...'),
              maxLines: 3,
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          TextButton(
            onPressed: () {
              context.read<ScreenBloc>().add(RejectScreen(screenId, feedbackCtrl.text));
              Navigator.pop(ctx);
            },
            child: const Text('Reject', style: TextStyle(color: Color(0xFFEF4444))),
          ),
          ElevatedButton(
            onPressed: () {
              context.read<ScreenBloc>().add(RequestChanges(screenId, feedbackCtrl.text));
              Navigator.pop(ctx);
            },
            child: const Text('Request Changes'),
          ),
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
