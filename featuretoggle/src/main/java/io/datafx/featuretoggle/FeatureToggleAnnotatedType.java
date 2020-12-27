package io.datafx.featuretoggle;


import io.datafx.flow.view.resource.ViewControllerAnnotatedType;
import io.datafx.flow.FlowView;

public class FeatureToggleAnnotatedType implements ViewControllerAnnotatedType<FeatureToggle, FeatureProperty<?>> {

    @Override
    public FeatureProperty<?> getResource(FeatureToggle annotation, FlowView<?> view) {
        return FeatureHandler.getInstance().createFeatureProperty(annotation.value());
    }

    @Override
    public Class<FeatureToggle> getSupportedAnnotation() {
        return FeatureToggle.class;
    }
}
