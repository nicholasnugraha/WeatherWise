import 'package:flutter_test/flutter_test.dart';
import 'package:weatherwise_flutter/main.dart';

void main() {
  testWidgets('App renders without crashing', (WidgetTester tester) async {
    await tester.pumpWidget(const WeatherWiseApp());
    expect(find.textContaining('WeatherWise Flutter'), findsOneWidget);
  });
}
