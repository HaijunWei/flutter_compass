import 'package:flutter/services.dart';

class Compass {
  static const EventChannel _compassChannel = EventChannel('haijunwei/compass');
  static final Stream<double> _stream = _compassChannel
      .receiveBroadcastStream()
      .map((dynamic data) => data as double? ?? 0);

  static Stream<double> get events => _stream;
}
