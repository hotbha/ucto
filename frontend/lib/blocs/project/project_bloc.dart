import 'dart:convert';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../models/project.dart';
import '../../services/api_service.dart';

part 'project_event.dart';
part 'project_state.dart';

class ProjectBloc extends Bloc<ProjectEvent, ProjectState> {
  final ApiService _api;

  ProjectBloc(this._api) : super(ProjectInitial()) {
    on<LoadProjects>(_onLoadProjects);
    on<CreateProject>(_onCreateProject);
    on<UpdateProject>(_onUpdateProject);
    on<DeleteProject>(_onDeleteProject);
  }

  Future<void> _onLoadProjects(LoadProjects event, Emitter<ProjectState> emit) async {
    emit(ProjectLoading());
    try {
      final response = await _api.get('/projects', auth: true);
      // Backend returns a JSON array, not HAL _embedded format
      final list = response as List;
      final projects = list.map((j) => Project.fromJson(j as Map<String, dynamic>)).toList();
      emit(ProjectsLoaded(projects));
    } catch (e) {
      emit(ProjectError(e.toString()));
    }
  }

  Future<void> _onCreateProject(CreateProject event, Emitter<ProjectState> emit) async {
    emit(ProjectLoading());
    try {
      final data = await _api.post('/projects', {
        'title': event.title,
        'description': event.description,
        'tier': event.tier,
      }, auth: true);
      final project = Project.fromJson(data as Map<String, dynamic>);
      emit(ProjectCreated(project));
    } catch (e) {
      emit(ProjectError(e.toString()));
    }
  }

  Future<void> _onUpdateProject(UpdateProject event, Emitter<ProjectState> emit) async {
    emit(ProjectLoading());
    try {
      final body = <String, dynamic>{};
      if (event.title != null) body['title'] = event.title;
      if (event.description != null) body['description'] = event.description;
      if (event.status != null) body['status'] = event.status;
      await _api.put('/projects/${event.id}', body);
      add(LoadProjects());
    } catch (e) {
      emit(ProjectError(e.toString()));
    }
  }

  Future<void> _onDeleteProject(DeleteProject event, Emitter<ProjectState> emit) async {
    emit(ProjectLoading());
    try {
      await _api.delete('/projects/${event.id}');
      add(LoadProjects());
    } catch (e) {
      emit(ProjectError(e.toString()));
    }
  }
}
