import UIKit

@main
final class BopaviApp: UIResponder, UIApplicationDelegate {
    var window: UIWindow?
    func application(_ application:UIApplication,didFinishLaunchingWithOptions launchOptions:[UIApplication.LaunchOptionsKey:Any]? = nil)->Bool {
        let w=UIWindow(frame:UIScreen.main.bounds)
        w.rootViewController=GameController()
        w.makeKeyAndVisible()
        window=w
        return true
    }
}
