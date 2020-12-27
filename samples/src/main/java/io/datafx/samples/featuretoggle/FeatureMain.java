package io.datafx.samples.featuretoggle;

import io.datafx.flow.Flow;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FeatureMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Scene myScene = new Scene((Parent) new Flow(FeatureController.class).wrap());
        stage.setScene(myScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
