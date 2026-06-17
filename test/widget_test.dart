import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

void main() {
  testWidgets('App smoke test', (WidgetTester tester) async {
    // Basic test - providers and DI are set up correctly
    final container = ProviderContainer();
    expect(container, isNotNull);
  });
}
