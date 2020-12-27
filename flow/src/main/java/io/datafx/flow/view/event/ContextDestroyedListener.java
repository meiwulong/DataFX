package io.datafx.flow.view.event;

import io.datafx.flow.FlowView;

@FunctionalInterface
public interface ContextDestroyedListener<T> {

    void contextDestroyed(FlowView<T> context);
}
