import 'package:flutter/material.dart';

/// Placeholder for the Settings screen.
/// TODO(milestone-4): implement full settings UI (units, language, notifications, etc.)
class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Pengaturan')),
      body: const Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.settings, size: 64, color: Colors.grey),
            SizedBox(height: 16),
            Text('Pengaturan belum tersedia.', style: TextStyle(fontSize: 16)),
            SizedBox(height: 8),
            Text('Akan ditambahkan di Milestone 4.',
                style: TextStyle(fontSize: 14, color: Colors.grey)),
          ],
        ),
      ),
    );
  }
}
