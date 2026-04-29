part of 'requirement_bloc.dart';

abstract class RequirementState {}

class RequirementInitial extends RequirementState {}

class RequirementLoading extends RequirementState {}

class RequirementsLoaded extends RequirementState {
  final List<Requirement> requirements;
  RequirementsLoaded(this.requirements);
}

class RequirementCreated extends RequirementState {
  final Requirement requirement;
  RequirementCreated(this.requirement);
}

class RequirementError extends RequirementState {
  final String message;
  RequirementError(this.message);
}
