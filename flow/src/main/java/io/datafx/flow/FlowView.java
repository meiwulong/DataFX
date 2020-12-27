/**
 * Copyright (c) 2011, 2013, Jonathan Giles, Johan Vos, Hendrik Ebbers
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of DataFX, the website javafxdata.org, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
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

import io.datafx.flow.view.*;
import io.datafx.flow.view.event.PostConstructListener;
import io.datafx.flow.action.FlowAction;
import io.datafx.util.NullNode;
import io.datafx.core.DataFXUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * This class defines one view in the flow. Normally this class will only be used by the Flow API and should not be used by developers
 * @param <T> Controller class of the Flow
 *
 * @author Hendrik Ebbers
 */
public class FlowView<T> {

	private Flow flow;

	/** controller clazz */
	private Class<T> controllerClazz;

	private StringProperty titleProperty;

	private ObjectProperty<Node> graphicProperty;

	private Map<String, FlowAction> flowMap = new HashMap<>();

	private ViewConfiguration configuration = new ViewConfiguration();

	/** controller instance */
	private T controller;

	/** control */
	private Node viewNode;



    /**
     * Creates a ViewMetadata based on a context
     * @param viewContext The context of the view
     */
    public FlowView(Flow flow, Class<T> controllerClazz) throws FxmlLoadException {
    	this(flow, controllerClazz, null);
    }

	public FlowView(Flow flow, Class<T> controllerClazz, String fxmlName) throws FxmlLoadException {
	    try {
//	    	this.register(this);
//		    this.register(new FlowActionHandler(flow.getHandler()));
	    	this.flow = flow;
	    	this.controllerClazz = controllerClazz;
		    // 1. Create an instance of the Controller
		    controller = controllerClazz.getDeclaredConstructor().newInstance();
		    ViewController controllerAnnotation = controllerClazz.getAnnotation(ViewController.class);
		    if (controllerAnnotation != null && !controllerAnnotation.title().isEmpty()) {
			    setTitle(controllerAnnotation.title());
		    }
		    if (controllerAnnotation != null && !controllerAnnotation.iconPath().isEmpty()) {
			    setGraphic(new ImageView(controllerClazz.getResource(controllerAnnotation.iconPath()).toExternalForm()));
		    }


		    // 2. Create the view and make sure the @FXML annotations are injected
		    if (controllerAnnotation != null && !controllerAnnotation.root().equals(NullNode.class)) {
			    viewNode = controllerAnnotation.root().getDeclaredConstructor().newInstance();
			    injectNodeIDs(viewNode);
		    } else {
			    FXMLLoader loader = createLoader(controller, fxmlName);
			    viewNode = loader.load();
		    }
		    injectFXMLNodes(controller, viewNode);


		    // 4. Resolve the @Inject points in the Controller and call
		    // @PostConstruct
		    FlowViewResolver.injectResources(this);

		    // 5. Call listeners
		    ServiceLoader<PostConstructListener> postConstructLoader = ServiceLoader.load(PostConstructListener.class);
		    for (PostConstructListener listener : postConstructLoader) {
			    listener.postConstruct(this);
		    }

		    // 6. call PostConstruct methods
		    for (final Method method : DataFXUtils.getInheritedDeclaredMethods(controller.getClass())) {
			    if (method.isAnnotationPresent(PostConstruct.class)) {
				    DataFXUtils.callPrivileged(method, controller);
			    }
		    }

		    flow.addActionsToView(this);
	    } catch (Exception e) {
		    throw new FxmlLoadException("Can't create flow view for controller: " + controllerClazz, e);
	    }

    }

	private FXMLLoader createLoader(final Object controller, String fxmlName) throws FxmlLoadException {
		Class<?> controllerClass = controller.getClass();
		String foundFxmlName = getFxmlName(controllerClass);
		if (fxmlName != null) {
			foundFxmlName = fxmlName;
		}
		if (foundFxmlName == null) {
			throw new FxmlLoadException("No FXML File specified!");
		}

		URL fxmlUrl = controllerClass.getResource(foundFxmlName);

		if (fxmlUrl == null) {
			throw new FxmlLoadException("Can't find FXML file for controller " + controller.getClass());
		}

		FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
		fxmlLoader.setBuilderFactory(configuration.getBuilderFactory());
		fxmlLoader.setCharset(configuration.getCharset());
		fxmlLoader.setResources(configuration.getResources());
		fxmlLoader.setController(controller);
		fxmlLoader.setControllerFactory(c -> controller);
		return fxmlLoader;
	}

	private String getFxmlName(Class<?> controllerClass) {
		String foundFxmlName = null;

		if (controllerClass.getSimpleName().endsWith("Controller")) {
			String nameByController = controllerClass.getSimpleName().substring(0, controllerClass.getSimpleName().length() - "Controller".length()) + ".fxml";
			if (DataFXUtils.canAccess(controllerClass, nameByController)) {
				foundFxmlName = nameByController;
			}
		}

		ViewController controllerAnnotation = controllerClass.getAnnotation(ViewController.class);
		if (controllerAnnotation != null && !controllerAnnotation.value().isEmpty()) {
			foundFxmlName = controllerAnnotation.value();
		}
		return foundFxmlName;
	}

	/**
	 * Because of some restrictions in the FXMLLoader not all fields that are annotated with @FXML will be injected in
	 * a controller when using the FXMLLoader class. This helper methods injects all fields that were not injected by
	 * JavaFX basics.
	 * The following types will not be injected by FXMLLoader:
	 * - private fields in a superclass of the controller class
	 * - fields that defines a node that is part of a sub-fxml. This is a fxml definition that is included in the fxml
	 * file.
	 *
	 * @param controller The controller instance
	 * @param root       The root Node
	 * @param <T>        Type of the controller
	 */
	private void injectFXMLNodes(T controller, Node root) {

		List<Field> fields = DataFXUtils.getInheritedDeclaredFields(controller.getClass());
		for (Field field : fields) {
			if (field.getAnnotation(FXML.class) != null) {
				if (DataFXUtils.getPrivileged(field, controller) == null) {
					if (Node.class.isAssignableFrom(field.getType())) {
						Node toInject = root.lookup("#" + field.getName());
						if (toInject != null) {
							DataFXUtils.setPrivileged(field, controller, toInject);
						}
					}
				}
			}

			if (field.getAnnotation(ViewNode.class) != null) {
				String id = field.getName();
				if (!field.getAnnotation(ViewNode.class).value().isEmpty()) {
					id = field.getAnnotation(ViewNode.class).value();
				}
				if (DataFXUtils.getPrivileged(field, controller) == null) {
					if (Node.class.isAssignableFrom(field.getType())) {
						Node toInject = root.lookup("#" + id);
						if (toInject != null) {
							DataFXUtils.setPrivileged(field, controller, toInject);
						}
					}
				}
			}
		}

	}

	private void injectNodeIDs(Node root) {
		List<Field> fields = DataFXUtils.getInheritedDeclaredFields(root.getClass());
		for (Field field : fields) {
			if (Node.class.isAssignableFrom(field.getType()) && field.getAnnotation(ViewNode.class) != null) {
				String id = field.getName();
				if (!field.getAnnotation(ViewNode.class).value().isEmpty()) {
					id = field.getAnnotation(ViewNode.class).value();
				}

				Node child = DataFXUtils.getPrivileged(field, root);
				child.setId(id);
			}
		}
	}

    /**
     * Adds a flow action to the view
     * @param actionId unique ID of the action
     * @param action  the action
     */
    public void addAction(String actionId, FlowAction action) {
        flowMap.put(actionId, action);
    }

    /**
     * Returns the registered flow action for the given unique id or null if no action is registered for the id
     * @param actionId the unique action id
     * @return the flow action for the given id
     */
    public FlowAction getActionById(String actionId) {
        return flowMap.get(actionId);
    }




	public String getTitle() {
		return titleProperty().get();
	}

	public StringProperty titleProperty() {
		if(titleProperty == null) {
			titleProperty = new SimpleStringProperty();
		}
		return titleProperty;
	}

	public void setTitle(final String title) {
		titleProperty().set(title);
	}

	public ObjectProperty<Node> graphicsProperty() {
		if(graphicProperty == null) {
			graphicProperty = new SimpleObjectProperty<>();
		}
		return graphicProperty;
	}


	public Node getGraphic() {
		return graphicsProperty().get();
	}

	public void setGraphic(final Node graphic) {
		this.graphicsProperty().set(graphic);
	}

	public Class<T> getControllerClazz() {
		return controllerClazz;
	}

	public void setControllerClazz(Class<T> controllerClazz) {
		this.controllerClazz = controllerClazz;
	}

	public T getController() {
		return controller;
	}

	public void setController(T controller) {
		this.controller = controller;
	}

	public Node getViewNode() {
		return viewNode;
	}

	public void setViewNode(Node viewNode) {
		this.viewNode = viewNode;
	}

	public void destroy() throws IllegalArgumentException {
		// TODO: All managed Object should be checked for a pre destroy....
		if (controller != null) {
			for (final Method method : DataFXUtils.getInheritedDeclaredMethods(getController().getClass())) {
				if (method.isAnnotationPresent(PreDestroy.class)) {
					DataFXUtils.callPrivileged(method, getController());
				}
			}
			this.controller = null;
		}
		this.viewNode = null;
		this.flow = null;
		if(this.titleProperty != null){
			this.titleProperty.unbind();
			this.titleProperty = null;
		}
		this.flowMap.clear();
		this.flowMap = null;
		if(this.graphicProperty != null){
			this.graphicProperty.unbind();
			this.graphicProperty = null;
		}
	}



	public ViewConfiguration getConfiguration() {
		return configuration;
	}

	public Flow getFlow() {
		return flow;
	}

	public void show() throws FlowException {
		if (controller != null) {
			try {
				for (final Method method : DataFXUtils.getInheritedDeclaredMethods(getController().getClass())) {
					if (method.isAnnotationPresent(ShowView.class)) {
						DataFXUtils.callPrivileged(method, getController());
					}
				}
			} catch (Exception e) {
				throw new FlowException("Last ViewContext show fail!", e);
			}
		}
	}

	public void hide() throws FlowException {
		if (controller != null) {
			try {
				for (final Method method : DataFXUtils.getInheritedDeclaredMethods(getController().getClass())) {
					if (method.isAnnotationPresent(HideView.class)) {
						DataFXUtils.callPrivileged(method, getController());
					}
				}
			} catch (Exception e) {
				throw new FlowException("Last ViewContext hide fail!", e);
			}
		}
	}
}
