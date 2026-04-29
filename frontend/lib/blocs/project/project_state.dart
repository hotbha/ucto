part of 'project_bloc.dart';

abstract class ProjectState {}

class ProjectInitial extends ProjectState {}

class ProjectLoading extends ProjectState {}

class ProjectsLoaded extends ProjectState {
  final List<Project> projects;
  ProjectsLoaded(this.projects);
}

class ProjectCreated extends ProjectState {
  final Project project;
  ProjectCreated(this.project);
}

class ProjectError extends ProjectState {
  final String message;
  ProjectError(this.message);
}
