part of 'screen_bloc.dart';

abstract class ScreenEvent {}

class LoadScreens extends ScreenEvent {
  final int projectId;
  LoadScreens(this.projectId);
}

class GenerateScreens extends ScreenEvent {
  final int projectId;
  GenerateScreens(this.projectId);
}

class ApproveScreen extends ScreenEvent {
  final int screenId;
  final String? feedback;
  ApproveScreen(this.screenId, {this.feedback});
}

class RejectScreen extends ScreenEvent {
  final int screenId;
  final String feedback;
  RejectScreen(this.screenId, this.feedback);
}

class RequestChanges extends ScreenEvent {
  final int screenId;
  final String feedback;
  RequestChanges(this.screenId, this.feedback);
}
