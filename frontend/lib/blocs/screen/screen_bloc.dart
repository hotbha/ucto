import 'package:flutter_bloc/flutter_bloc.dart';
import '../../models/screen_model.dart';
import '../../services/api_service.dart';

part 'screen_event.dart';
part 'screen_state.dart';

class ScreenBloc extends Bloc<ScreenEvent, ScreenState> {
  final ApiService _api;

  ScreenBloc(this._api) : super(ScreenInitial()) {
    on<LoadScreens>(_onLoadScreens);
    on<GenerateScreens>(_onGenerateScreens);
    on<ApproveScreen>(_onApproveScreen);
    on<RejectScreen>(_onRejectScreen);
    on<RequestChanges>(_onRequestChanges);
  }

  Future<void> _onLoadScreens(LoadScreens event, Emitter<ScreenState> emit) async {
    emit(ScreenLoading());
    try {
      final response = await _api.get('/projects/${event.projectId}/screens', auth: true);
      final list = response as List;
      final screens = list.map((j) => ScreenModel.fromJson(j as Map<String, dynamic>)).toList();
      emit(ScreensLoaded(screens));
    } catch (e) {
      emit(ScreenError(e.toString()));
    }
  }

  Future<void> _onGenerateScreens(GenerateScreens event, Emitter<ScreenState> emit) async {
    emit(ScreenLoading());
    try {
      await _api.post('/screens/generate', {'projectId': event.projectId}, auth: true);
      // Reload screens after generation
      add(LoadScreens(event.projectId));
    } catch (e) {
      emit(ScreenError(e.toString()));
    }
  }

  Future<void> _onApproveScreen(ApproveScreen event, Emitter<ScreenState> emit) async {
    try {
      await _api.put('/screens/${event.screenId}/approve', {
        if (event.feedback != null) 'feedback': event.feedback,
      }, auth: true);
      emit(ScreenActionSuccess('Screen approved successfully'));
    } catch (e) {
      emit(ScreenError(e.toString()));
    }
  }

  Future<void> _onRejectScreen(RejectScreen event, Emitter<ScreenState> emit) async {
    try {
      await _api.put('/screens/${event.screenId}/reject', {
        'feedback': event.feedback,
      }, auth: true);
      emit(ScreenActionSuccess('Screen rejected'));
    } catch (e) {
      emit(ScreenError(e.toString()));
    }
  }

  Future<void> _onRequestChanges(RequestChanges event, Emitter<ScreenState> emit) async {
    try {
      await _api.put('/screens/${event.screenId}/changes-requested', {
        'feedback': event.feedback,
      }, auth: true);
      emit(ScreenActionSuccess('Changes requested'));
    } catch (e) {
      emit(ScreenError(e.toString()));
    }
  }
}
