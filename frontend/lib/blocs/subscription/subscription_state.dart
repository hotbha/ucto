part of 'subscription_bloc.dart';

abstract class SubscriptionState {}

class SubscriptionInitial extends SubscriptionState {}

class SubscriptionLoading extends SubscriptionState {}

class SubscriptionLoaded extends SubscriptionState {
  final UsageStatus usage;
  final List<SubscriptionPlan> plans;
  SubscriptionLoaded(this.usage, this.plans);
}

class SubscriptionUpgraded extends SubscriptionState {
  final String message;
  SubscriptionUpgraded(this.message);
}

class SubscriptionError extends SubscriptionState {
  final String message;
  SubscriptionError(this.message);
}
