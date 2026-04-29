class User {
  final int id;
  final String email;
  final String role;
  final String name;
  final String? phoneNumber;
  final bool emailVerified;

  User({
    required this.id,
    required this.email,
    required this.role,
    required this.name,
    this.phoneNumber,
    this.emailVerified = false,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] as int,
      email: json['email'] as String,
      role: json['role'] as String,
      name: json['name'] as String? ?? json['email'] as String,
      phoneNumber: json['phoneNumber'] as String?,
      emailVerified: json['emailVerified'] as bool? ?? false,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'email': email,
        'role': role,
        'name': name,
        'phoneNumber': phoneNumber,
        'emailVerified': emailVerified,
      };

  bool get isFounder => role == 'FOUNDER';
  bool get isDeveloper => role == 'DEVELOPER';
  bool get isViewer => role == 'VIEWER';
  bool get isAdmin => role == 'UCTO_ADMIN';
}
