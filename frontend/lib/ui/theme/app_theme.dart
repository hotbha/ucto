import 'package:flutter/material.dart';

class AppTheme {
  AppTheme._();

  static const Color _bg = Color(0xFF0F1115);
  static const Color _surface = Color(0xFF171A21);
  static const Color _surfaceAlt = Color(0xFF1F2430);
  static const Color _card = Color(0xFF202633);
  static const Color _primary = Color(0xFF7C5CFC);
  static const Color _secondary = Color(0xFF22C55E);
  static const Color _accent = Color(0xFF06B6D4);
  static const Color _warning = Color(0xFFF59E0B);
  static const Color _error = Color(0xFFEF4444);
  static const Color _text = Color(0xFFF8FAFC);
  static const Color _muted = Color(0xFF94A3B8);
  static const Color _border = Color(0xFF2B3240);

  static ThemeData get darkTheme {
    final base = ThemeData.dark(useMaterial3: true);

    const colorScheme = ColorScheme.dark(
      primary: _primary,
      secondary: _secondary,
      tertiary: _accent,
      error: _error,
      surface: _surface,
      onPrimary: Colors.white,
      onSecondary: Colors.white,
      onTertiary: Colors.white,
      onError: Colors.white,
      onSurface: _text,
    );

    return base.copyWith(
      colorScheme: colorScheme,
      scaffoldBackgroundColor: _bg,
      canvasColor: _bg,
      dividerColor: _border,
      disabledColor: _muted,
      appBarTheme: const AppBarTheme(
        backgroundColor: _bg,
        foregroundColor: _text,
        elevation: 0,
        centerTitle: false,
        surfaceTintColor: Colors.transparent,
      ),
      cardTheme: CardThemeData(
        color: _card,
        elevation: 0,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: const BorderSide(color: _border),
        ),
      ),
      dialogTheme: DialogThemeData(
        backgroundColor: _surface,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
        ),
      ),
      snackBarTheme: SnackBarThemeData(
        backgroundColor: _surfaceAlt,
        contentTextStyle: const TextStyle(color: _text),
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(14),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: _surfaceAlt,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 16,
          vertical: 14,
        ),
        hintStyle: const TextStyle(color: _muted),
        labelStyle: const TextStyle(color: _muted),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: _border),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: _border),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: _primary, width: 1.4),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: _error),
        ),
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: _error, width: 1.4),
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: _primary,
          foregroundColor: Colors.white,
          minimumSize: const Size.fromHeight(52),
          elevation: 0,
          textStyle: const TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.w700,
          ),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
          ),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: _text,
          minimumSize: const Size.fromHeight(52),
          side: const BorderSide(color: _border),
          textStyle: const TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.w600,
          ),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
          ),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: _primary,
          textStyle: const TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      chipTheme: base.chipTheme.copyWith(
        backgroundColor: _surfaceAlt,
        selectedColor: _primary.withValues(alpha: 0.18),
        disabledColor: _surfaceAlt,
        side: const BorderSide(color: _border),
        labelStyle: const TextStyle(color: _text),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(999),
        ),
      ),
      listTileTheme: const ListTileThemeData(
        tileColor: Colors.transparent,
        iconColor: _muted,
        textColor: _text,
      ),
      progressIndicatorTheme: const ProgressIndicatorThemeData(
        color: _primary,
        linearTrackColor: _surfaceAlt,
        circularTrackColor: _surfaceAlt,
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: _primary,
        foregroundColor: Colors.white,
      ),
      iconTheme: const IconThemeData(color: _text),
      textTheme: const TextTheme(
        headlineLarge: TextStyle(
          color: _text,
          fontSize: 32,
          fontWeight: FontWeight.w800,
          letterSpacing: -0.8,
        ),
        headlineMedium: TextStyle(
          color: _text,
          fontSize: 26,
          fontWeight: FontWeight.w700,
          letterSpacing: -0.5,
        ),
        headlineSmall: TextStyle(
          color: _text,
          fontSize: 22,
          fontWeight: FontWeight.w700,
        ),
        titleLarge: TextStyle(
          color: _text,
          fontSize: 18,
          fontWeight: FontWeight.w700,
        ),
        titleMedium: TextStyle(
          color: _text,
          fontSize: 16,
          fontWeight: FontWeight.w600,
        ),
        bodyLarge: TextStyle(
          color: _text,
          fontSize: 16,
          fontWeight: FontWeight.w400,
        ),
        bodyMedium: TextStyle(
          color: _text,
          fontSize: 14,
          fontWeight: FontWeight.w400,
        ),
        bodySmall: TextStyle(
          color: _muted,
          fontSize: 12,
          fontWeight: FontWeight.w400,
        ),
        labelLarge: TextStyle(
          color: _text,
          fontSize: 14,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }

  static const Color background = _bg;
  static const Color surface = _surface;
  static const Color surfaceAlt = _surfaceAlt;
  static const Color card = _card;
  static const Color primary = _primary;
  static const Color secondary = _secondary;
  static const Color accent = _accent;
  static const Color warning = _warning;
  static const Color error = _error;
  static const Color text = _text;
  static const Color muted = _muted;
  static const Color border = _border;
}