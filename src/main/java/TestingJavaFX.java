import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class TestingJavaFX extends Application {
    @Override
    public void start(Stage stage) {
        // Create a WebView
        WebView webView = new WebView();

        // Simple HTML content with inline CSS
        String html = """
            <html>
            <head>
                <style>
                    body {
                        background-color: #f0f0f0;
                        font-family: Arial, sans-serif;
                        text-align: center;
                        padding: 50px;
                    }
                    h1 {
                        color: #2E8B57;
                    }
                    p {
                        color: #555555;
                        font-size: 18px;
                    }
                    button {
                        padding: 10px 20px;
                        font-size: 16px;
                        background-color: #4CAF50;
                        color: white;
                        border: none;
                        border-radius: 5px;
                        cursor: pointer;
                    }
                    button:hover {
                        background-color: #45a049;
                    }
                </style>
            </head>
            <body>
                <h1>Smart Warehouse Dashboard</h1>
                <p>Inventory Overview</p>
                <button onclick="alert('Button clicked!')">Click Me</button>
            </body>
            </html>
        """;

        // Load HTML into WebView
        webView.getEngine().loadContent(html);

        // Layout
        VBox root = new VBox(webView);

        // Scene
        Scene scene = new Scene(root, 600, 400);

        stage.setTitle("Smart Warehouse");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
