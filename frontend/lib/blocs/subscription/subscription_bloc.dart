import 'package:flutter_bloc/flutter_bloc.dart';
import '../../models/subscription_plan.dart';
import '../../models/usage_status.dart';
import '../../services/api_service.dart';

part 'subscription_event.dart';
part 'subscription_state.dart';

class SubscriptionBloc extends Bloc<SubscriptionEvent, SubscriptionState> {
  final ApiService _api;

  SubscriptionBloc(this._api) : super(SubscriptionInitial()) {
    on<LoadSubscription>(_onLoad);
    on<UpgradeSubscription>(_onUpgrade);
    on<StartTrial>(_onStartTrial);
  }

  Future<void> _onLoad(LoadSubscription event, Emitter<SubscriptionState> emit) async {
    emit(SubscriptionLoading());
    try {
      final usageData = await _api.get('/subscriptions/my', auth: true);
      final usage = UsageStatus.fromJson(usageData);
      emit(SubscriptionLoaded(usage, [])); // Plans loaded separately if needed
    } catch (e) {
      emit(SubscriptionError(e.toString()));
    }
  }

  Future<void> _onUpgrade(UpgradeSubscription event, Emitter<SubscriptionState> emit) async {
    emit(SubscriptionLoading());
    try {
      final data = await _api.post('/subscriptions/upgrade', {'tier': event.tier}, auth: true);
      emit(SubscriptionUpgraded(data['message']));
    } catch (e) {
      emit(SubscriptionError(e.toString()));
    }
  }

  Future<void> _onStartTrial(StartTrial event, Emitter<SubscriptionState> emit) async {
    emit(SubscriptionLoading());
    try {
      final data = await _api.post('/subscriptions/start-trial', {}, auth: true);
      emit(SubscriptionUpgraded(data['message']));
    } catch (e) {
      emit(SubscriptionError(e.toString()));
    }
  }
}
