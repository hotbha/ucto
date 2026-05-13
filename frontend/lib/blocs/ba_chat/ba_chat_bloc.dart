import 'dart:convert';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../models/ba_chat_message.dart';
import '../../services/api_service.dart';

part 'ba_chat_event.dart';
part 'ba_chat_state.dart';

class BAChatBloc extends Bloc<BAChatEvent, BAChatState> {
  final ApiService _api;

  BAChatBloc(this._api) : super(BAChatInitial()) {
    on<SendMessage>(_onSendMessage);
    on<LoadChatHistory>(_onLoadChatHistory);
    on<ClearChat>(_onClearChat);
  }

  Future<void> _onSendMessage(SendMessage event, Emitter<BAChatState> emit) async {
    emit(BAChatLoading());
    try {
      final response = await _api.post('/ba/chat', {
        'projectId': event.projectId,
        'message': event.message,
      }, auth: true);

      final message = BAChatMessage.fromJson(response as Map<String, dynamic>);
      final bool clarificationComplete = response['clarificationComplete'] as bool? ?? false;
      final bool needsEscalation = response['needsEscalation'] as bool? ?? false;
      final int currentRound = response['roundNumber'] as int? ?? 0;

      emit(BAChatMessageSent(
        message: message,
        currentRound: currentRound,
        clarificationComplete: clarificationComplete,
        needsEscalation: needsEscalation,
      ));
    } catch (e) {
      emit(BAChatError(e.toString()));
    }
  }

  Future<void> _onLoadChatHistory(LoadChatHistory event, Emitter<BAChatState> emit) async {
    emit(BAChatLoading());
    try {
      final response = await _api.get('/ba/chat/${event.projectId}', auth: true);
      final history = BAChatHistoryResponse.fromJson(response as Map<String, dynamic>);

      emit(BAChatLoaded(
        messages: history.messages,
        currentRound: history.currentRound,
        clarificationComplete: history.clarificationComplete,
        needsEscalation: history.needsEscalation,
      ));
    } catch (e) {
      emit(BAChatError(e.toString()));
    }
  }

  void _onClearChat(ClearChat event, Emitter<BAChatState> emit) {
    emit(BAChatInitial());
  }
}
