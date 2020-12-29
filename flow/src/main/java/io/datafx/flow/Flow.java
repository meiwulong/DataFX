/**
 * Copyright (c) 2011, 2014, Jonathan Giles, Johan Vos, Hendrik Ebbers
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of DataFX, the website javafxdata.org, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.datafx.flow;

import io.datafx.core.DataFXUtils;
import io.datafx.core.ExceptionHandler;
import io.datafx.flow.action.*;
import io.datafx.flow.view.ViewController;
import io.datafx.flow.wrapper.DefaultFlowViewWrapper;
import io.datafx.flow.wrapper.FlowViewWrapper;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This class defines a flow. A flow is a map of different views that are linked.
 * A flow can define actions for each view or global actions for the complete flow.
 * The class provides a fluent API to create a flow with views and actions.
 *
 * @author Hendrik Ebbers
 */
public class Flow {

	private Class<?> startControllerClazz;
	private Map<Class<?>, Map<String, FlowAction>> viewFlowMap;
	private Map<String, FlowAction> globalFlowMap;
	private ViewConfiguration viewConfiguration;
	private FlowHandler handler;

	private Map<String, Object> dataMap = new HashMap<>();

	/**
	 * Creates a new Flow with the given controller for the start view and a
	 * view configuration for all views.
	 * The start view must be a view controller as specified in the DataFX-Controller API.
	 * See {@link ViewController} for more information
	 *
	 * @param startControllerClazz Controller class of the start view
	 * @param viewConfiguration        Configuration for all views of the flow
	 * @see ViewController
	 */
	public Flow(Class<?> startControllerClazz, ViewConfiguration viewConfiguration) {
		this.startControllerClazz = startControllerClazz;
		globalFlowMap = new HashMap<>();
		viewFlowMap = new HashMap<>();
		this.viewConfiguration = viewConfiguration;
		this.handler = createHandler();
		register(handler);
		register(new FlowActionHandler(handler));
	}

	/**
	 * Creates a new Flow with the given controller for the start view.
	 * The startViewControllerClass must be a view controller as specified
	 * in the DataFX-Controller API, which means it must be a class
	 * annotated with {@link ViewController}.
	 * See {@link ViewController} for more information.
	 * Using this constructor will create a new {@link ViewConfiguration}.
	 *
	 * @param startControllerClazz Controller class of the start view
	 * @see ViewController
	 */
	public Flow(Class<?> startControllerClazz) {
		this(startControllerClazz, new ViewConfiguration());
	}

	/**
	 * Returns the view configuration for all views of the flow
	 *
	 * @return the view configuration
	 */
	public ViewConfiguration getViewConfiguration() {
		return viewConfiguration;
	}

	/**
	 * Creates a handler that can be used to run the flow. The Flow class provides only the definition of a flow.
	 */
	protected FlowHandler createHandler() {
		return new FlowHandler(this, ExceptionHandler.getDefaultInstance());
	}

	/**
	 * Adds a global action to the flow. The action is registered by the given unique ID and can be called at runtime
	 * by using the id. A global action can be called from each view and from outside of the flow.
	 *
	 * @param actionId unique action id
	 * @param action   the action
	 * @return returns this flow (for the fluent API)
	 */
	public Flow withGlobalAction(String actionId, FlowAction action) {
		addGlobalAction(actionId, action);
		return this;
	}

	/**
	 * Adds a task action as a global action to the flow. Internally a {@link FlowTaskAction} will be created and
	 * added to the flow. As you can read in the documentation of {@link FlowTaskAction} a instance of the given
	 * {@code actionClass} will be created and executed whenever the action will be called.
	 *
	 * @param actionId    unique action id
	 * @param actionClass class that defines the runnable that will be called whenever the action is called
	 * @return returns this flow (for the fluent API)
	 * @see FlowTaskAction
	 */
	public Flow withGlobalTaskAction(String actionId, Class<? extends Runnable> actionClass) {
		addGlobalAction(actionId, new FlowTaskAction(actionClass));
		return this;
	}

	/**
	 * Adds a task action as a global action to the flow. Internally a {@link FlowTaskAction} will be created and
	 * added to the flow.
	 *
	 * @param actionId unique action id
	 * @param action   a runnable that will be called whenever the action is called
	 * @return returns this flow (for the fluent API)
	 * @see FlowTaskAction
	 */
	public Flow withGlobalTaskAction(String actionId, Runnable action) {
		addGlobalAction(actionId, new FlowTaskAction(action));
		return this;
	}

	/**
	 * Adds a link action as a global action to the flow. Internally a {@link FlowLink} will be created and added
	 * to the flow.
	 *
	 * @param actionId        unique action id
	 * @param controllerClass the controller of the view that should be shown whenever the action will be called
	 * @return returns this flow (for the fluent API)
	 * @see FlowLink
	 */
	public Flow withGlobalLink(String actionId, Class<?> controllerClass) {
		addGlobalAction(actionId, new FlowLink<>(controllerClass));
		return this;
	}

	/**
	 * Adds a global back action to the flow that navigates back to the last view of a flow. Internally a
	 * {@link FlowBackAction} will be created and added to the flow.
	 *
	 * @param actionId unique action id
	 * @return returns this flow (for the fluent API)
	 * @see FlowBackAction
	 */
	public Flow withGlobalBackAction(String actionId) {
		addGlobalAction(actionId, new FlowBackAction());
		return this;
	}

	/**
	 * Adds a action to the view of the given view controller. The action is registered by the given unique ID and can
	 * be called at runtime by using the id. the action can only be called when the view of the given controller is the
	 * active view of the flow
	 *
	 * @param controllerClass controller class for the view of the action
	 * @param actionId        unique action id
	 * @param action          the action
	 * @return returns this flow (for the fluent API)
	 */
	public Flow withAction(Class<?> controllerClass, String actionId, FlowAction action) {
		addActionToView(controllerClass, actionId, action);
		return this;
	}

	/**
	 * Adds a navigation action to the flow that will navigate from one view to another. The two controller classes that
	 * must be passed as parameters defines the origin view and the destination. Internally a {@link FlowLink} will be
	 * created and added to the flow.
	 *
	 * @param fromControllerClass the controller class of the view that is the origin of the navigation
	 * @param actionId            unique action id
	 * @param toControllerClass   the controller class of the view that is the destination of the navigation
	 * @return returns this flow (for the fluent API)
	 * @see FlowLink
	 */
	public Flow withLink(Class<?> fromControllerClass, String actionId, Class<?> toControllerClass) {
		addActionToView(fromControllerClass, actionId, new FlowLink<>(toControllerClass));
		return this;
	}

	/**
	 * Adds a task action to the defined view of the flow. Internally a {@link FlowTaskAction} will be created and
	 * added to the flow. As you can read in the documentation of {@link FlowTaskAction} a instance of the given
	 * {@code actionClass} will be created and executed whenever the action will be called.
	 *
	 * @param controllerClass the controller class of the view to that the action will be registered
	 * @param actionId        unique action id
	 * @param actionClass     class that defines the runnable that will be called whenever the action is called
	 * @return returns this flow (for the fluent API)
	 * @see FlowTaskAction
	 */
	public Flow withTaskAction(Class<?> controllerClass, String actionId, Class<? extends Runnable> actionClass) {
		addActionToView(controllerClass, actionId, new FlowTaskAction(actionClass));
		return this;
	}

	/**
	 * Adds a task action to the defined view of the flow. Internally a {@link FlowTaskAction} will be created and
	 * added to the flow.
	 *
	 * @param controllerClass the controller class of the view to that the action will be registered
	 * @param actionId        unique action id
	 * @param action          a runnable that will be called whenever the action is called
	 * @return returns this flow (for the fluent API)
	 * @see FlowTaskAction
	 */
	public Flow withTaskAction(Class<?> controllerClass, String actionId, Runnable action) {
		addActionToView(controllerClass, actionId, new FlowTaskAction(action));
		return this;
	}

	/**
	 * Adds a back action to the defined view of the flow that navigates back to the last view of a flow. Internally a
	 * {@link FlowBackAction} will be created and added to the flow.
	 *
	 * @param controllerClass the controller class of the view to that the action will be registered
	 * @param actionId        unique action id
	 * @return returns this flow (for the fluent API)
	 * @see FlowBackAction
	 */
	public Flow withBackAction(Class<?> controllerClass, String actionId) {
		addActionToView(controllerClass, actionId, new FlowBackAction());
		return this;
	}

	/**
	 * Adds an action to the defined view of the flow.
	 *
	 * @param controllerClass the controller class of the view to that the action will be registered
	 * @param actionId        unique action id
	 * @param action          the action that will be added
	 * @return returns this flow (for the fluent API)
	 * @see FlowAction
	 */
	public Flow addActionToView(Class<?> controllerClass, String actionId, FlowAction action) {
		viewFlowMap.computeIfAbsent(controllerClass, k -> new HashMap<>());
		viewFlowMap.get(controllerClass).put(actionId, action);
		return this;
	}

	/**
	 * Adds a global action to the flow.
	 *
	 * @param actionId unique action id
	 * @param action   the action that will be added
	 * @return returns this flow (for the fluent API)
	 * @see FlowAction
	 */
	public Flow addGlobalAction(String actionId, FlowAction action) {
		globalFlowMap.put(actionId, action);
		return this;
	}

	/**
	 * Returns the action that is registered by the given unique id
	 *
	 * @param actionId the id
	 * @return the action that is registered by the given unique id
	 */
	public FlowAction getGlobalActionById(String actionId) {
		return globalFlowMap.get(actionId);
	}

	/**
	 * Returns the class of the view controller that is defined as the start view.
	 *
	 * @return the class of the view controller that is defined as the start view
	 */
	public Class<?> getStartControllerClazz() {
		return startControllerClazz;
	}

	/**
	 * This methods adds all registered actions to the given flow view. This method is needed by DataFX and should
	 * normally not called by a developer.
	 *
	 * @param newView the view that shoudl be prepaired
	 * @param <U>     Class of the view controller
	 */
	public <U> void addActionsToView(FlowView<U> newView) {
		Map<String, FlowAction> viewActionMap = viewFlowMap.get(newView.getController().getClass());
		if (viewActionMap != null) {
			for (String actionId : viewActionMap.keySet()) {
				newView.addAction(actionId, viewActionMap.get(actionId));
			}
		}

		for (Method method : DataFXUtils.getInheritedDeclaredMethods(newView.getController().getClass())) {
			ActionMethod actionMethod = method.getAnnotation(ActionMethod.class);
			if (actionMethod != null) {
				newView.addAction(actionMethod.value(), new FlowMethodAction(method));
			}

		}
	}



	/**
	 * Starts the flow directly in a Stage. This method is useful if an application contains of one main flow. Because
	 * this flow can contain several sub-flows this is the preferred way to create a DataFX based application. The title
	 * of the Stage will be bound to the title of the flow metadata and will change whenever the flow title fill change.
	 * This can happen if a view of the flow defines its own title by using the title attribute of the @ViewController
	 * annotation or the ViewMetadata of an view is changed in code.
	 * <p>
	 * By using the method a flow based application can be created by only a few lines of code as shown in this example:
	 * <code>
	 * public class Example extends Application {
	 *
	 * public static void main(String[] args) {
	 * launch(args);
	 * }
	 *
	 * public void start(Stage primaryStage) throws Exception {
	 * new Flow(SimpleController.class).startInStage(primaryStage);
	 * }
	 * }
	 * </code>
	 *
	 * @param stage The stage in that the flow should be displayed.
	 * @throws FlowException If the flow can't be created or started
	 */
	public void startInStage(Stage stage) throws FlowException {
		getHandler().startInStage(stage, new DefaultFlowViewWrapper());
	}

	public <T extends Parent> void startInStage(Stage stage, FlowViewWrapper<T> viewWrapper) throws FlowException {
		getHandler().startInStage(stage, new DefaultFlowViewWrapper());
	}

	/** 封装流容器*/
	public <T extends Node> void wrap(StackPane pane) throws FlowException {
		wrap(new DefaultFlowViewWrapper(pane));
	}

	/** 封装流容器*/
	public <T extends Node> T wrap(FlowViewWrapper<T> flowViewWrapper) throws FlowException {
		return getHandler().start(flowViewWrapper);
	}

	/** 封装流视图到默认封装器中 {@link DefaultFlowContainer} */
	public StackPane wrap() throws FlowException {
		return wrap(new DefaultFlowViewWrapper());
	}

	public FlowHandler getHandler(){
		return handler;
	}


	public <R> R getRegisteredObject(Class<R> clazz) {
		return getRegisteredObject(clazz.toString());
	}
	public <R> R getRegisteredObject(String key) {
		return (R) dataMap.get(key);
	}

	public void register(final Object value) {
		register(value.getClass().toString(), value);
	}

	public void register(String key, final Object value) {
		dataMap.put(key, value);
	}


	public <T extends Node> T getViewWrap(){
		return (T) getHandler().getViewWrapper().getWrap();
	}
}