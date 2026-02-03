import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init() {
        initKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }

    struct ContentView: View {
        var body: some View {
            ComposeView().ignoresSafeArea(.keyboard)
        }
    }

    struct ComposeView: UIViewControllerRepresentable {
        func makeUIViewController(context: Context) -> UIViewController {
            return MainViewControllerKt.MainViewController()
        }

        func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
    }
}