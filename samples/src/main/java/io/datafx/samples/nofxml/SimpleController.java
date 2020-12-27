package io.datafx.samples.nofxml;

import io.datafx.flow.view.ViewController;
import io.datafx.flow.view.ViewNode;
import io.datafx.flow.action.ActionMethod;
import io.datafx.flow.action.ActionTrigger;
import javafx.scene.control.Button;

@ViewController(root = SimpleView.class)
public class SimpleController {

    @ViewNode
    @ActionTrigger("action")
    private Button myButton;

    @ActionMethod("action")
    private void onClick() {
        System.out.println("TADA!");
    }
}
