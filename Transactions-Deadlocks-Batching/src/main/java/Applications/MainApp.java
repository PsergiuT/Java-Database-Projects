package Applications;

import Controller.AppController;
import Repo.RepoApp;
import Service.ServiceApp;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainApp extends Application {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void start(Stage stage) throws Exception {
        logger.info("Starting app, Setting up services");
        ServiceApp serviceApp = new ServiceApp(new RepoApp());

        logger.info("Setting up FXML Scene");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/app.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("App");
        stage.setScene(scene);

        AppController controller = fxmlLoader.getController();
        controller.setService(serviceApp);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
