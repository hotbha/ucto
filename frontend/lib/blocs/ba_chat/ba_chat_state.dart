part of 'ba_chat_bloc.dart';

abstract class BAChatState {}

class BAChatInitial extends BAChatState {}

class BAChatLoading extends BAChatState {}

class BAChatLoaded extends BAChatState {
  final List<BAChatMessage> messages;
  final int currentRound;
  final bool clarificationComplete;
  final bool needsEscalation;
  BAChatLoaded({
    required this.messages,
    required this.currentRound,
    required this.clarificationComplete,
    required this.needsEscalation,
  });
}

class BAChatMessageSent extends BAChatState {
  final BAChatMessage message;
  final int currentRound;
  final bool clarificationComplete;
  final bool needsEscalation;
  BAChatMessageSent({
    required this.message,
    required this.currentRound,
    required this.clarificationComplete,
    required this.needsEscalation,
  });
}

class BAChatError extends BAChatState {
  final String message;
  BAChatError(this.message);
}
