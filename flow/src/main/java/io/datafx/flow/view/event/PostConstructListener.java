package io.datafx.flow.view.event;

import io.datafx.flow.FlowView;

@FunctionalInterface
public interface PostConstructListener {

    void postConstruct(FlowView<?> context);
}
