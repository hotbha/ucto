import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../blocs/ba_chat/ba_chat_bloc.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../models/ba_chat_message.dart';

class BAChatScreen extends StatefulWidget {
  final int projectId;
  final String projectTitle;
  const BAChatScreen({
    super.key,
    required this.projectId,
    required this.projectTitle,
  });

  @override
  State<BAChatScreen> createState() => _BAChatScreenState();
}

class _BAChatScreenState extends State<BAChatScreen> {
  final _messageController = TextEditingController();
  final _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    context.read<BAChatBloc>().add(LoadChatHistory(widget.projectId));
  }

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  void _sendMessage() {
    final message = _messageController.text.trim();
    if (message.isEmpty) return;

    context.read<BAChatBloc>().add(SendMessage(widget.projectId, message));
    _messageController.clear();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F172A),
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'BA Agent Chat',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600, color: Color(0xFFF1F5F9)),
            ),
            Text(
              widget.projectTitle,
              style: const TextStyle(fontSize: 12, color: Color(0xFF94A3B8)),
            ),
          ],
        ),
        actions: [
          BlocBuilder<BAChatBloc, BAChatState>(
            builder: (context, state) {
              if (state is BAChatLoaded || state is BAChatMessageSent) {
                final round = state is BAChatLoaded ? state.currentRound : (state as BAChatMessageSent).currentRound;
                final needsEscalation = state is BAChatLoaded ? state.needsEscalation : (state as BAChatMessageSent).needsEscalation;
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: Center(
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: needsEscalation
                            ? const Color(0xFFDC2626).withValues(alpha: 0.2)
                            : const Color(0xFF7C3AED).withValues(alpha: 0.2),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(
                          color: needsEscalation
                              ? const Color(0xFFDC2626).withValues(alpha: 0.3)
                              : const Color(0xFF7C3AED).withValues(alpha: 0.3),
                        ),
                      ),
                      child: Text(
                        'Round $round/3',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w500,
                          color: needsEscalation ? const Color(0xFFFCA5A5) : const Color(0xFFC4B5FD),
                        ),
                      ),
                    ),
                  ),
                );
              }
              return const SizedBox.shrink();
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // Chat messages area
          Expanded(
            child: BlocConsumer<BAChatBloc, BAChatState>(
              listener: (context, state) {
                if (state is BAChatLoaded || state is BAChatMessageSent) {
                  _scrollToBottom();
                }
              },
              builder: (context, state) {
                if (state is BAChatInitial) {
                  return const Center(
                    child: Text(
                      'Start a conversation with your BA Agent',
                      style: TextStyle(color: Color(0xFF94A3B8)),
                    ),
                  );
                }

                if (state is BAChatLoading) {
                  return const Center(
                    child: CircularProgressIndicator(color: Color(0xFF7C3AED)),
                  );
                }

                if (state is BAChatError && state is! BAChatLoaded && state is! BAChatMessageSent) {
                  return Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(Icons.error_outline, color: Color(0xFFEF4444), size: 48),
                        const SizedBox(height: 16),
                        Text(
                          state.message,
                          style: const TextStyle(color: Color(0xFF94A3B8)),
                          textAlign: TextAlign.center,
                        ),
                        const SizedBox(height: 16),
                        ElevatedButton(
                          onPressed: () => context.read<BAChatBloc>().add(LoadChatHistory(widget.projectId)),
                          style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF7C3AED)),
                          child: const Text('Retry'),
                        ),
                      ],
                    ),
                  );
                }

                final messages = state is BAChatLoaded
                    ? state.messages
                    : state is BAChatMessageSent
                        ? [state.message]
                        : <BAChatMessage>[];
                final isComplete = state is BAChatLoaded
                    ? state.clarificationComplete
                    : state is BAChatMessageSent
                        ? state.clarificationComplete
                        : false;
                final needsEscalation = state is BAChatLoaded
                    ? state.needsEscalation
                    : state is BAChatMessageSent
                        ? state.needsEscalation
                        : false;

                return Column(
                  children: [
                    // Escalation warning banner
                    if (needsEscalation)
                      Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(12),
                        color: const Color(0xFF7F1D1D),
                        child: const Row(
                          children: [
                            Icon(Icons.warning_amber, color: Color(0xFFFCA5A5), size: 20),
                            SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                'Maximum clarification rounds reached. Your request has been escalated to a UCTO Admin.',
                                style: TextStyle(color: Color(0xFFFCA5A5), fontSize: 13),
                              ),
                            ),
                          ],
                        ),
                      ),

                    // Clarification complete banner
                    if (isComplete)
                      Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(12),
                        color: const Color(0xFF14532D),
                        child: const Row(
                          children: [
                            Icon(Icons.check_circle, color: Color(0xFF86EFAC), size: 20),
                            SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                'Requirements finalized! Development agents have been triggered.',
                                style: TextStyle(color: Color(0xFF86EFAC), fontSize: 13),
                              ),
                            ),
                          ],
                        ),
                      ),

                    // Messages list
                    Expanded(
                      child: messages.isEmpty
                          ? const Center(
                              child: Column(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Icon(Icons.chat_bubble_outline, color: Color(0xFF334155), size: 64),
                                  SizedBox(height: 16),
                                  Text(
                                    'No messages yet',
                                    style: TextStyle(color: Color(0xFF94A3B8), fontSize: 16),
                                  ),
                                  SizedBox(height: 8),
                                  Text(
                                    'Describe your requirements to get started',
                                    style: TextStyle(color: Color(0xFF64748B), fontSize: 13),
                                  ),
                                ],
                              ),
                            )
                          : ListView.builder(
                              controller: _scrollController,
                              padding: const EdgeInsets.all(16),
                              itemCount: messages.length,
                              itemBuilder: (context, index) {
                                final msg = messages[index];
                                return _ChatMessageBubble(message: msg);
                              },
                            ),
                    ),
                  ],
                );
              },
            ),
          ),

          // Text input area
          Container(
            decoration: const BoxDecoration(
              color: Color(0xFF1E293B),
              border: Border(
                top: BorderSide(color: Color(0xFF334155), width: 1),
              ),
            ),
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _messageController,
                    style: const TextStyle(color: Color(0xFFF1F5F9)),
                    decoration: InputDecoration(
                      hintText: 'Type your message...',
                      hintStyle: const TextStyle(color: Color(0xFF64748B)),
                      filled: true,
                      fillColor: const Color(0xFF0F172A),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(24),
                        borderSide: const BorderSide(color: Color(0xFF334155)),
                      ),
                      enabledBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(24),
                        borderSide: const BorderSide(color: Color(0xFF334155)),
                      ),
                      focusedBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(24),
                        borderSide: const BorderSide(color: Color(0xFF7C3AED)),
                      ),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
                    ),
                    textInputAction: TextInputAction.send,
                    onSubmitted: (_) => _sendMessage(),
                    maxLines: 3,
                    minLines: 1,
                  ),
                ),
                const SizedBox(width: 8),
                BlocBuilder<BAChatBloc, BAChatState>(
                  builder: (context, state) {
                    final isLoading = state is BAChatLoading;
                    return Container(
                      decoration: const BoxDecoration(
                        color: Color(0xFF7C3AED),
                        shape: BoxShape.circle,
                      ),
                      child: IconButton(
                        icon: isLoading
                            ? const SizedBox(
                                width: 20,
                                height: 20,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                  color: Colors.white,
                                ),
                              )
                            : const Icon(Icons.send, color: Colors.white, size: 20),
                        onPressed: isLoading ? null : _sendMessage,
                      ),
                    );
                  },
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ChatMessageBubble extends StatelessWidget {
  final BAChatMessage message;
  const _ChatMessageBubble({required this.message});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // User message (right-aligned)
        Align(
          alignment: Alignment.centerRight,
          child: Container(
            margin: const EdgeInsets.only(bottom: 4),
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
            decoration: BoxDecoration(
              color: const Color(0xFF7C3AED),
              borderRadius: BorderRadius.only(
                topLeft: const Radius.circular(20),
                topRight: const Radius.circular(4),
                bottomLeft: const Radius.circular(20),
                bottomRight: const Radius.circular(20),
              ),
            ),
            constraints: BoxConstraints(
              maxWidth: MediaQuery.of(context).size.width * 0.75,
            ),
            child: Text(
              message.userMessage,
              style: const TextStyle(color: Colors.white, fontSize: 14),
            ),
          ),
        ),
        const SizedBox(height: 4),

        // Message type label
        if (message.messageType == 'GREETING' || message.messageType == 'CLARIFICATION' || message.messageType == 'DECISION')
          Padding(
            padding: const EdgeInsets.only(left: 4, bottom: 4),
            child: Text(
              _messageTypeLabel(message.messageType),
              style: TextStyle(
                fontSize: 11,
                color: _messageTypeColor(message.messageType),
                fontWeight: FontWeight.w500,
              ),
            ),
          ),

        // BA response (left-aligned bubble)
        if (message.baResponse != null && message.baResponse!.isNotEmpty)
          Align(
            alignment: Alignment.centerLeft,
            child: Container(
              margin: const EdgeInsets.only(bottom: 4),
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
              decoration: BoxDecoration(
                color: const Color(0xFF1E293B),
                borderRadius: BorderRadius.only(
                  topLeft: const Radius.circular(4),
                  topRight: const Radius.circular(20),
                  bottomLeft: const Radius.circular(20),
                  bottomRight: const Radius.circular(20),
                ),
                border: Border.all(color: const Color(0xFF334155)),
              ),
              constraints: BoxConstraints(
                maxWidth: MediaQuery.of(context).size.width * 0.75,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // BA identifier
                  Row(
                    children: [
                      Container(
                        width: 18,
                        height: 18,
                        decoration: const BoxDecoration(
                          color: Color(0xFF7C3AED),
                          shape: BoxShape.circle,
                        ),
                        child: const Center(
                          child: Text(
                            'BA',
                            style: TextStyle(fontSize: 8, color: Colors.white, fontWeight: FontWeight.bold),
                          ),
                        ),
                      ),
                      const SizedBox(width: 6),
                      const Text(
                        'BA Agent',
                        style: TextStyle(fontSize: 11, color: Color(0xFF7C3AED), fontWeight: FontWeight.w600),
                      ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  Text(
                    message.baResponse!,
                    style: const TextStyle(color: Color(0xFFE2E8F0), fontSize: 14, height: 1.5),
                  ),
                ],
              ),
            ),
          ),

        // Round number and timestamp
        Padding(
          padding: const EdgeInsets.only(left: 4, bottom: 12),
          child: Row(
            children: [
              Text(
                _formatTime(message.createdAt),
                style: const TextStyle(fontSize: 10, color: Color(0xFF64748B)),
              ),
            ],
          ),
        ),
      ],
    );
  }

  String _messageTypeLabel(String type) {
    switch (type) {
      case 'GREETING': return '🤝 Greeting';
      case 'CLARIFICATION': return '❓ Clarification needed';
      case 'DECISION': return '📝 Decision documented';
      case 'FINALIZATION': return '✅ Finalization';
      case 'ESCALATION': return '⚠️ Escalated';
      default: return type;
    }
  }

  Color _messageTypeColor(String type) {
    switch (type) {
      case 'GREETING': return const Color(0xFF22C55E);
      case 'CLARIFICATION': return const Color(0xFFF59E0B);
      case 'DECISION': return const Color(0xFF3B82F6);
      case 'FINALIZATION': return const Color(0xFF22C55E);
      case 'ESCALATION': return const Color(0xFFEF4444);
      default: return const Color(0xFF94A3B8);
    }
  }

  String _formatTime(DateTime dt) {
    final hour = dt.hour.toString().padLeft(2, '0');
    final minute = dt.minute.toString().padLeft(2, '0');
    return '$hour:$minute';
  }
}
