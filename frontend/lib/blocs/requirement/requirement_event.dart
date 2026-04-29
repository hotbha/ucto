part of 'requirement_bloc.dart';

abstract class RequirementEvent {}

class LoadRequirements extends RequirementEvent {
  final int projectId;
  LoadRequirements(this.projectId);
}

class CreateRequirement extends RequirementEvent {
  final int projectId;
  final String title;
  final String description;
  CreateRequirement(this.projectId, this.title, this.description);
}

class UpdateRequirementStatus extends RequirementEvent {
  final int id;
  final String status;
  UpdateRequirementStatus(this.id, this.status);
}
