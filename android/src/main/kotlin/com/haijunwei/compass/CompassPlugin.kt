package com.haijunwei.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.StreamHandler


/** CompassPlugin */
class CompassPlugin: FlutterPlugin, StreamHandler {
  private var channel: EventChannel? = null
  private var context: Context? = null
  private var orientationSensor: Sensor? = null
  private var sensorManager: SensorManager? = null
  private var sensorEventListener: SensorEventListener? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    initListener()
    channel = EventChannel(flutterPluginBinding.binaryMessenger, "haijunwei/compass")
    channel?.setStreamHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    unregisterListener()
    channel?.setStreamHandler(null)
  }

  private fun initListener() {
    sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    orientationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION);
  }

  private fun unregisterListener() {
    sensorManager?.unregisterListener(sensorEventListener, orientationSensor)
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    sensorEventListener = events?.let { createSensorEventListener(it) }
    sensorManager?.registerListener(sensorEventListener, orientationSensor, SensorManager.SENSOR_DELAY_GAME)
  }

  override fun onCancel(arguments: Any?) {
    unregisterListener()
  }

  private fun createSensorEventListener(events: EventChannel.EventSink): SensorEventListener {
    return object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent) {
        val direction = event.values[0]
        notifyCompassChangeListeners(direction)
      }

      override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
      }

      private fun notifyCompassChangeListeners(direction: Float) {
        events.success(direction)
      }
    }
  }
}
