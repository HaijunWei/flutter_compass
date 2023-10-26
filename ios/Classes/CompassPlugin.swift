import Flutter
import UIKit
import CoreLocation

public class CompassPlugin: NSObject, FlutterPlugin, FlutterStreamHandler, CLLocationManagerDelegate {
    
    private let locationManager: CLLocationManager
    private var eventSink: FlutterEventSink?
    
    init(channel: FlutterEventChannel) {
        locationManager = CLLocationManager()
        super.init()
        channel.setStreamHandler(self)
        locationManager.delegate = self
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterEventChannel.init(name: "haijunwei/compass", binaryMessenger: registrar.messenger())
        let instance = CompassPlugin(channel: channel)
        registrar.publish(instance)
    }

    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        self.eventSink = events
        locationManager.startUpdatingHeading()
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        locationManager.stopUpdatingHeading()
        return nil
    }
    
    public func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        eventSink?(newHeading.trueHeading > 0 ? newHeading.trueHeading : newHeading.magneticHeading)
    }
}
