package io.datafx.samples.featuretoggle;

import io.datafx.flow.view.ViewController;
import io.datafx.featuretoggle.DisabledByFeature;
import io.datafx.featuretoggle.HideByFeature;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

@ViewController("/io/datafx/samples/featuretoggle/featureView.fxml")
public class FeatureController {

    @FXML
    @HideByFeature("FEATURE1")
    private Button button1;

    @FXML
    @DisabledByFeature("FEATURE2")
    private Button button2;

}
