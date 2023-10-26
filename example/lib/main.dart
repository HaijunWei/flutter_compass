import 'package:flutter/material.dart';
import 'dart:async';

import 'package:compass_plus/compass_plus.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  StreamSubscription<double>? _eventsSub;
  double _directionAngle = 0;

  @override
  void initState() {
    super.initState();
    _eventsSub = Compass.events.listen((event) {
      _directionAngle = event;
      setState(() {});
    });
  }

  @override
  void dispose() {
    _eventsSub?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Angle: $_directionAngle\n'),
        ),
      ),
    );
  }
}
