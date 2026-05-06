part of 'screen_bloc.dart';

abstract class ScreenState {}

class ScreenInitial extends ScreenState {}

class ScreenLoading extends ScreenState {}

class ScreensLoaded extends ScreenState {
  final List<ScreenModel> screens;
  ScreensLoaded(this.screens);
}

class ScreenActionSuccess extends ScreenState {
  final String message;
  ScreenActionSuccess(this.message);
}

class ScreenError extends ScreenState {
  final String message;
  ScreenError(this.message);
}
