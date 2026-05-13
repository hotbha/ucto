part of 'ba_chat_bloc.dart';

abstract class BAChatEvent {}

class SendMessage extends BAChatEvent {
  final int projectId;
  final String message;
  SendMessage(this.projectId, this.message);
}

class LoadChatHistory extends BAChatEvent {
  final int projectId;
  LoadChatHistory(this.projectId);
}

class ClearChat extends BAChatEvent {}
