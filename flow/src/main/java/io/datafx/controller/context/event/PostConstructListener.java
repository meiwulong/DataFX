package io.datafx.controller.context.event;

import io.datafx.controller.context.ViewContext;

@FunctionalInterface
public interface PostConstructListener {

    void postConstruct(ViewContext context);
}
