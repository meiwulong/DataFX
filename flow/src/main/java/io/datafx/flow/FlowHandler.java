/**
 * Copyright (c) 2011, 2013, Jonathan Giles, Johan Vos, Hendrik Ebbers
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

import io.datafx.core.ExceptionHandler;
import io.datafx.flow.action.FlowAction;
import io.datafx.flow.action.FlowLinkAction;
import io.datafx.flow.event.*;
import io.datafx.flow.wrapper.FlowViewWrapper;
import io.datafx.util.ActionUtil;
import io.datafx.util.Veto;
import io.datafx.util.VetoException;
import io.datafx.util.VetoHandler;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.util.UUID;

// FIXME: 2/10/2018 Needs documentation
public class FlowHandler {
	private Flow flow;
	private final ObservableList<FlowView<?>> controllerHistory;
	private final ObservableMap<Class<?>, FlowView<?>> viewMap;
	private ReadOnlyObjectWrapper<FlowView<?>> currentView;
	private ReadOnlyObjectWrapper<FlowViewWrapper<?>> viewWrapper;
	private SimpleObjectProperty<BeforeFlowActionHandler> beforeFlowActionHandler;
	private SimpleObjectProperty<AfterFlowActionHandler> afterFlowActionHandler;
	private SimpleObjectProperty<VetoableBeforeFlowActionHandler> vetoableBeforeFlowActionHandler;
	private SimpleObjectProperty<VetoHandler> vetoHandler;
	private ExceptionHandler exceptionHandler;



	public FlowHandler(Flow flow, ExceptionHandler exceptionHandler) {
		this.flow = flow;
		this.exceptionHandler = exceptionHandler;
		this.viewMap = FXCollections.observableHashMap();
		controllerHistory = FXCollections.observableArrayList();
		currentView = new ReadOnlyObjectWrapper<>();
		viewWrapper = new ReadOnlyObjectWrapper<>();
	}

	protected  <T extends Parent> void startInStage(Stage stage, FlowViewWrapper<T> viewWrapper) throws FlowException {
		stage.setScene(new Scene(start(viewWrapper)));
		currentView.addListener((e) -> {
			stage.titleProperty().unbind();
			stage.titleProperty().bind(currentView.get().titleProperty());
		});

		stage.titleProperty().unbind();
		stage.titleProperty().bind(currentView.get().titleProperty());

		stage.show();
	}


	public void destroy() {
		//TODO
	}

	public <T extends Node> T start(FlowViewWrapper<T> wrapper) throws FlowException {
		viewWrapper.set(wrapper);
		try {
			switchView(flow.getStartControllerClazz(), false);
		} catch (FxmlLoadException e) {
			throw new FlowException(e);
		}
		return wrapper.getWrap();
	}


	public void handle(String actionId) throws VetoException, FlowException {
		FlowAction action = null;
		if (getCurrentView() != null) {
			action = getCurrentView().getActionById(actionId);
		}
		if (action == null) {
			action = flow.getGlobalActionById(actionId);
		}
		if (action == null) {
			throw new FlowException("Can't find an action with id " + actionId);
		}
		handle(action, actionId);
	}

	public ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * Set the exception handler for this flow.
	 * @param exceptionHandler the exception handler
	 */
	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	public Flow getFlow() {
		return flow;
	}

	public FlowView<?> getCurrentView() {
		return currentView.get();
	}

	public ReadOnlyObjectWrapper<FlowView<?>> currentViewProperty() {
		return currentView;
	}

	public FlowViewWrapper<?> getViewWrapper() {
		return viewWrapper.get();
	}

	public ReadOnlyObjectWrapper<FlowViewWrapper<?>> viewWrapperProperty() {
		return viewWrapper;
	}

	/**
	 * Returns the controller class of the current visible
	 * @return the view controller class
	 */
	public Class<?> getCurrentControllerClazz() {
		return getCurrentView().getController().getClass();
	}

	public void handle(FlowAction action, String actionId) throws FlowException, VetoException {

		if (beforeFlowActionHandler != null && beforeFlowActionHandler.getValue() != null) {
			beforeFlowActionHandler.getValue().handle(new BeforeFlowActionEvent(actionId, action));
		}

		if (vetoableBeforeFlowActionHandler != null && vetoableBeforeFlowActionHandler.getValue() != null) {
			try {
				vetoableBeforeFlowActionHandler.getValue().handle(new BeforeFlowActionEvent(actionId, action));
			} catch (Veto veto) {
				if (vetoHandler != null && vetoHandler.getValue() != null) {
					vetoHandler.get().onVeto(veto);
				}
				throw new VetoException(veto);
			}
		}

		action.handle(this, actionId);
		if (afterFlowActionHandler != null && afterFlowActionHandler.getValue() != null) {
			afterFlowActionHandler.getValue().handle(new AfterFlowActionEvent(actionId, action));
		}
	}

	@SuppressWarnings("unchecked")
	public <U> FlowView<U> switchView(Class<?> controller, boolean destroy) throws FlowException, FxmlLoadException {
		FlowView<?> newView = viewMap.get(controller);
		if(newView == null){
			newView = createFlowView(controller);
		}
		return (FlowView<U>) switchView(newView, destroy);
	}

	public FlowView<?> createFlowView(Class<?> controller) {
		FlowView<?> newView = null;
		try {
			newView = new FlowView<>(flow, controller);
			viewMap.put(newView.getControllerClazz(), newView);
			System.out.println("controller init " + controller);
		} catch (FxmlLoadException e) {
			e.printStackTrace();
		}
		return newView;
	}

	public <U> FlowView<U> switchView(FlowView<U> newView, boolean destroy) throws FlowException {
		FlowView<?> oldView = getCurrentView();

		currentView.set(newView);
		viewWrapper.get().switchView(newView, destroy);
		newView.show();
		if (oldView != null) {
			oldView.hide();
			if(destroy){
				try {
					viewMap.remove(oldView.getControllerClazz());
					controllerHistory.remove(oldView);
					oldView.destroy();
					flow.unregister(oldView);
				} catch (Exception e) {
					throw new FlowException("Last ViewContext can't be destroyed!", e);
				}
			}else{
				controllerHistory.add(0, oldView);
			}
		}
		return newView;
	}

	/** 导航上一视图（默认销毁现有视图） */
	public void navigateBack() throws  FlowException {
		navigateBack(true);
	}

	/** 导航上一视图 */
	public void navigateBack(boolean destroyed) throws FlowException {
		switchView(controllerHistory.remove(0), destroyed);
	}

	public ObservableList<FlowView<?>> getControllerHistory() {
		return FXCollections.unmodifiableObservableList(controllerHistory);
	}

	/**
	 * Navigate to the view that is defined by the given controller class
	 * @param controllerClass the controller class of the view
	 * @throws VetoException
	 * @throws FlowException
	 */
	public void navigateTo(Class<?> controllerClass) throws VetoException, FlowException {
		handle(new FlowLinkAction<>(controllerClass),
				"navigateAction-" + UUID.randomUUID().toString());
	}

	public void attachAction(Node node, Runnable action) {
		ActionUtil.defineNodeAction(node, action);
	}

	public void attachAction(MenuItem menuItem, Runnable action) {
		ActionUtil.defineItemAction(menuItem, action);
	}

	public void attachEventHandler(Node node, String actionId) {
		ActionUtil.defineNodeAction(node, () -> handleActionWithExceptionHandler(actionId));
	}

	public void attachBackEventHandler(MenuItem menuItem) {
		ActionUtil.defineItemAction(menuItem, this::handleBackActionWithExceptionHandler);
	}

	public void attachBackEventHandler(Node node) {
		ActionUtil.defineNodeAction(node, this::handleBackActionWithExceptionHandler);
	}

	public void attachEventHandler(MenuItem menuItem, String actionId) {
		ActionUtil.defineItemAction(menuItem, () -> handleActionWithExceptionHandler(actionId));
	}

	private void handleActionWithExceptionHandler(String id) {
		try {
			handle(id);
		} catch (VetoException | FlowException e) {
			getExceptionHandler().setException(e);
		}
	}

	private void handleBackActionWithExceptionHandler() {
		try {
			navigateBack();
		} catch (FlowException e) {
			getExceptionHandler().setException(e);
		}
	}

}