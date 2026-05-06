/// AppConfig reads build-time environment variables passed via --dart-define.
///
/// These are set during `flutter build` or `flutter run`:
///   --dart-define=SERVER_HOST=192.168.1.100
///   --dart-define=SERVER_PORT=8080
///
/// Defaults to a LAN IP placeholder so localhost is never the default
/// in your development or production builds.
class AppConfig {
  AppConfig._();

  /// The hostname or IP where the backend API is reachable.
  /// Override at build time with --dart-define=SERVER_HOST=...
  static String get apiHost =>
      const String.fromEnvironment('SERVER_HOST', defaultValue: '0.0.0.0');

  /// The port the backend API listens on.
  /// Override at build time with --dart-define=SERVER_PORT=...
  static String get apiPort =>
      const String.fromEnvironment('SERVER_PORT', defaultValue: '8080');

  /// Full base URL for the API (without trailing slash).
  static String get baseUrl => 'http://$apiHost:$apiPort/api';
}
