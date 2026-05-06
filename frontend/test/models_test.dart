import 'package:flutter_test/flutter_test.dart';
import 'package:ucto_frontend/models/user.dart';
import 'package:ucto_frontend/models/usage_status.dart';
import 'package:ucto_frontend/models/subscription_plan.dart';

void main() {
  group('User model', () {
    test('fromJson creates User with all fields', () {
      final json = {
        'id': 1,
        'email': 'test@test.com',
        'role': 'FOUNDER',
        'name': 'Test User',
        'phoneNumber': '+919876543210',
        'emailVerified': true,
      };

      final user = User.fromJson(json);
      expect(user.id, 1);
      expect(user.email, 'test@test.com');
      expect(user.role, 'FOUNDER');
      expect(user.name, 'Test User');
      expect(user.phoneNumber, '+919876543210');
      expect(user.emailVerified, true);
    });

    test('fromJson uses email as fallback for name', () {
      final json = {
        'id': 2,
        'email': 'noname@test.com',
        'role': 'DEVELOPER',
        'name': null,
      };

      final user = User.fromJson(json);
      expect(user.name, 'noname@test.com');
    });

    test('fromJson defaults emailVerified to false', () {
      final json = {
        'id': 3,
        'email': 'test@test.com',
        'role': 'DEVELOPER',
        'name': 'Dev',
      };

      final user = User.fromJson(json);
      expect(user.emailVerified, false);
    });

    test('toJson returns correct map', () {
      final user = User(id: 1, email: 'test@test.com', role: 'FOUNDER', name: 'Test');
      final json = user.toJson();

      expect(json['id'], 1);
      expect(json['email'], 'test@test.com');
      expect(json['role'], 'FOUNDER');
      expect(json['name'], 'Test');
    });

    test('role helper methods return correct values', () {
      final founder = User(id: 1, email: 'f@t.com', role: 'FOUNDER', name: 'F');
      final dev = User(id: 2, email: 'd@t.com', role: 'DEVELOPER', name: 'D');
      final viewer = User(id: 3, email: 'v@t.com', role: 'VIEWER', name: 'V');
      final admin = User(id: 4, email: 'a@t.com', role: 'UCTO_ADMIN', name: 'A');

      expect(founder.isFounder, true);
      expect(dev.isDeveloper, true);
      expect(viewer.isViewer, true);
      expect(admin.isAdmin, true);

      expect(founder.isDeveloper, false);
      expect(dev.isFounder, false);
    });
  });

  group('UsageStatus model', () {
    test('fromJson creates UsageStatus with all fields', () {
      final json = {
        'tier': 'STARTUP',
        'maxProjects': 5,
        'projectsUsed': 2,
        'projectsRemaining': 3,
        'maxAgentRuns': 50,
        'runsUsed': 10,
        'runsRemaining': 40,
        'hasAudit': true,
        'hasCompliance': false,
        'hasPrioritySupport': true,
        'needsUpgrade': false,
        'canRun': true,
      };

      final usage = UsageStatus.fromJson(json);
      expect(usage.tier, 'STARTUP');
      expect(usage.maxProjects, 5);
      expect(usage.projectsUsed, 2);
      expect(usage.projectsRemaining, 3);
      expect(usage.maxAgentRuns, 50);
      expect(usage.runsUsed, 10);
      expect(usage.runsRemaining, 40);
      expect(usage.hasAudit, true);
      expect(usage.hasCompliance, false);
      expect(usage.hasPrioritySupport, true);
      expect(usage.needsUpgrade, false);
      expect(usage.canRun, true);
    });

    test('fromJson defaults to FREE tier when tier is missing', () {
      final json = <String, dynamic>{};
      final usage = UsageStatus.fromJson(json);
      expect(usage.tier, 'FREE');
      expect(usage.maxProjects, 1);
      expect(usage.maxAgentRuns, 5);
    });
  });

  group('SubscriptionPlan model', () {
    test('fromJson creates SubscriptionPlan with all fields', () {
      final json = {
        'tier': 'GROWTH',
        'price': 7999,
        'maxProjects': 20,
        'maxAgentRuns': 500,
        'hasAudit': true,
        'hasCompliance': true,
        'hasPrioritySupport': true,
      };

      final plan = SubscriptionPlan.fromJson(json);
      expect(plan.tier, 'GROWTH');
      expect(plan.price, 7999);
      expect(plan.maxProjects, 20);
      expect(plan.maxAgentRuns, 500);
      expect(plan.hasAudit, true);
      expect(plan.hasCompliance, true);
      expect(plan.hasPrioritySupport, true);
      // formattedPrice for 7999 paise: 79~100=79, 7999%100=99 → "₹79,99"
      expect(plan.formattedPrice, startsWith('₹'));
      expect(plan.formattedPrice.contains('79'), isTrue);
    });

    test('formattedPrice shows "Free" for price 0', () {
      final json = {
        'tier': 'FREE',
        'price': 0,
        'maxProjects': 1,
        'maxAgentRuns': 5,
        'hasAudit': false,
        'hasCompliance': false,
        'hasPrioritySupport': false,
      };

      final plan = SubscriptionPlan.fromJson(json);
      expect(plan.formattedPrice, 'Free');
    });

    test('formattedPriceUSD derives USD from INR', () {
      final json = {
        'tier': 'STARTUP',
        'price': 2999, // ~$35
        'maxProjects': 5,
        'maxAgentRuns': 50,
        'hasAudit': false,
        'hasCompliance': false,
        'hasPrioritySupport': false,
      };

      final plan = SubscriptionPlan.fromJson(json);
      expect(plan.formattedPriceUSD, startsWith('\$'));
    });
  });
}
