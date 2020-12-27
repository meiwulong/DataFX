package io.datafx.featuretoggle;

import io.datafx.flow.view.resource.ControllerResourceConsumer;
import io.datafx.flow.FlowView;
import javafx.scene.Node;

public class HideByFeatureResourceConsumer implements ControllerResourceConsumer<HideByFeature, Node> {

    @Override
    public void consumeResource(HideByFeature annotation, Node resource, FlowView<?> view) {
        FeatureHandler.getInstance().hideByFeature(resource, annotation.value());
    }

    @Override
    public Class<HideByFeature> getSupportedAnnotation() {
        return HideByFeature.class;
    }
}
