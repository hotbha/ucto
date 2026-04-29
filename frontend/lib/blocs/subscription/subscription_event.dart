part of 'subscription_bloc.dart';

abstract class SubscriptionEvent {}

class LoadSubscription extends SubscriptionEvent {}

class UpgradeSubscription extends SubscriptionEvent {
  final String tier;
  UpgradeSubscription(this.tier);
}

class StartTrial extends SubscriptionEvent {}
