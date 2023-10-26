package com.haijunwei.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.StreamHandler


/** CompassPlugin */
class CompassPlugin: FlutterPlugin, StreamHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : EventChannel
  private var context: Context? = null
  private var gravitySensor: Sensor? = null
  private var magneticFieldSensor: Sensor? = null
  private var sensorManager: SensorManager? = null
  private var lastAccuracySensorStatus: Int? = null
  private val rotationMatrix = FloatArray(9)
  private var gravityValues = FloatArray(3)
  private var magneticValues = FloatArray(3)
  private var sensorEventListener: SensorEventListener? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    initListener()
    val channel = EventChannel(flutterPluginBinding.binaryMessenger, "haijunwei/compass")
    channel.setStreamHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setStreamHandler(null)
  }

  private fun initListener() {
    sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    magneticFieldSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    sensorEventListener = events?.let { createSensorEventListener(it) }
    sensorManager?.registerListener(sensorEventListener, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    sensorManager?.registerListener(sensorEventListener, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL)
  }

  override fun onCancel(arguments: Any?) {
    sensorManager?.unregisterListener(sensorEventListener, gravitySensor)
    sensorManager?.unregisterListener(sensorEventListener, magneticFieldSensor)
  }

  private fun createSensorEventListener(events: EventChannel.EventSink): SensorEventListener {
    return object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent) {
        if (lastAccuracySensorStatus === SensorManager.SENSOR_STATUS_UNRELIABLE) {
//          Log.d(TAG, "Compass sensor is unreliable, device calibration is needed.")
          // Update the heading, even if the sensor is unreliable.
          // This makes it possible to use a different indicator for the unreliable case,
          // instead of just changing the RenderMode to NORMAL.
        }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
          gravityValues = event.values
          updateOrientation()
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
          magneticValues = event.values
          updateOrientation()
        }
      }

      override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (lastAccuracySensorStatus !== accuracy) {
          lastAccuracySensorStatus = accuracy
        }
      }

      private fun updateOrientation() {
        SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, magneticValues)
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)
        var value = Math.toDegrees(orientation[0].toDouble()).toFloat()
        if (value < 0) value += 360.0.toFloat()
        notifyCompassChangeListeners(value)
      }

      private fun notifyCompassChangeListeners(heading: Float) {
        events.success(heading)
      }
    }
  }
}
