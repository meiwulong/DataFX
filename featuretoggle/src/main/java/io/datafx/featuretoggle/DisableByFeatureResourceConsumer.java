package io.datafx.featuretoggle;

import io.datafx.flow.view.resource.ControllerResourceConsumer;
import io.datafx.flow.FlowView;
import javafx.scene.Node;

public class DisableByFeatureResourceConsumer implements ControllerResourceConsumer<DisabledByFeature, Node> {

    @Override
    public void consumeResource(DisabledByFeature annotation, Node resource, FlowView<?> view) {
        FeatureHandler.getInstance().disableByFeature(resource, annotation.value());
    }

    @Override
    public Class<DisabledByFeature> getSupportedAnnotation() {
        return DisabledByFeature.class;
    }
}
