package io.datafx.tutorial;

import io.datafx.flow.view.ViewController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import io.datafx.flow.action.LinkAction;

import javax.annotation.PostConstruct;

/**
 * This is a view controller for one of the steps in the wizard. The "back" and "finish" buttons of the action-bar that
 * is shown on each view of the wizard are defined in the AbstractWizardController class. So this class only needs to
 * define the "next" button. By using the @LinkAction annotation this button will link on the next step of the
 * wizard. This annotation was already described in tutorial 2.
 *
 * When looking at the @ViewController annotation of the class you can find a new feature. Next to the fxml files that
 * defines the view of the wizard step a "title" is added. This defines the title of the view. Because the wizard is
 * added to a Stage by using the Flow.startInStage() method, the window title of the Stage is automatically bound to
 * the title of the flow. So whenever the view in the flow changes the title of the application window will change to
 * the defined title of the view. As you will learn in future tutorial you can easily change the title of a view in code.
 * In addition to the title other metadata like a icon can be defined for a view or flow.
 */
@ViewController(value="wizard1.fxml", title = "Wizard: Step 1")
public class Wizard1Controller extends AbstractWizardController {

    @FXML
    @LinkAction(Wizard2Controller.class)
    private Button nextButton;

	@PostConstruct
	private void init(){
		System.out.println("Wizard1Controller PostConstruct: " + this + ", " + nextButton.hashCode());
	}
}
