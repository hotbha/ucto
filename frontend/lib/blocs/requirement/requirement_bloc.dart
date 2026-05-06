import 'package:flutter_bloc/flutter_bloc.dart';
import '../../models/requirement.dart';
import '../../services/api_service.dart';

part 'requirement_event.dart';
part 'requirement_state.dart';

class RequirementBloc extends Bloc<RequirementEvent, RequirementState> {
  final ApiService _api;

  RequirementBloc(this._api) : super(RequirementInitial()) {
    on<LoadRequirements>(_onLoad);
    on<CreateRequirement>(_onCreate);
    on<UpdateRequirementStatus>(_onUpdateStatus);
  }

  Future<void> _onLoad(LoadRequirements event, Emitter<RequirementState> emit) async {
    emit(RequirementLoading());
    try {
      // Backend endpoint: GET /api/requirements/project/{projectId} returns JSON array
      final response = await _api.get('/requirements/project/${event.projectId}', auth: true);
      final list = response as List;
      final requirements = list.map((j) => Requirement.fromJson(j as Map<String, dynamic>)).toList();
      emit(RequirementsLoaded(requirements));
    } catch (e) {
      emit(RequirementError(e.toString()));
    }
  }

  Future<void> _onCreate(CreateRequirement event, Emitter<RequirementState> emit) async {
    emit(RequirementLoading());
    try {
      final data = await _api.post('/requirements', {
        'projectId': event.projectId.toString(),
        'title': event.title,
        'description': event.description,
      }, auth: true);
      final req = Requirement.fromJson(data as Map<String, dynamic>);
      emit(RequirementCreated(req));
    } catch (e) {
      emit(RequirementError(e.toString()));
    }
  }

  Future<void> _onUpdateStatus(UpdateRequirementStatus event, Emitter<RequirementState> emit) async {
    emit(RequirementLoading());
    try {
      final data = await _api.put('/requirements/${event.id}', {'status': event.status}, auth: true);
      final req = Requirement.fromJson(data as Map<String, dynamic>);
      emit(RequirementUpdated(req));
    } catch (e) {
      emit(RequirementError(e.toString()));
    }
  }
}
