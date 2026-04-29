part of 'project_bloc.dart';

abstract class ProjectEvent {}

class LoadProjects extends ProjectEvent {}

class CreateProject extends ProjectEvent {
  final String title;
  final String description;
  final String tier;
  CreateProject(this.title, this.description, {this.tier = 'FREE'});
}

class UpdateProject extends ProjectEvent {
  final int id;
  final String? title;
  final String? description;
  final String? status;
  UpdateProject(this.id, {this.title, this.description, this.status});
}

class DeleteProject extends ProjectEvent {
  final int id;
  DeleteProject(this.id);
}
