package org.datafx.controller.flow;

import org.datafx.controller.context.ViewContext;

public interface FXMLFlowContainer {

    public void setView(ViewContext context);
    
    public void flowFinished();
}