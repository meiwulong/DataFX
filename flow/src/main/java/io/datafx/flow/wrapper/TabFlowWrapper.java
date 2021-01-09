package io.datafx.flow.wrapper;

import io.datafx.flow.FlowView;
import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 * wrapper flow view to {@link Tab}
 *
 * @author first
 * @date 2020-12-26 15:26
 */
public class TabFlowWrapper implements FlowViewWrapper<Tab> {

	private FlowViewWrapper<? extends Node> innerWrapper;
	private Tab tab;
	public TabFlowWrapper(){
		this(new DefaultFlowViewWrapper());
	}

	public TabFlowWrapper(FlowViewWrapper<? extends Node> innerWrapper){
		this.innerWrapper = innerWrapper;

		this.tab = new Tab();

//		getCurrentViewMetadata().addListener((e) -> {
//			tab.textProperty().unbind();
//			tab.graphicProperty().unbind();
//			tab.textProperty().bind(getCurrentViewMetadata().get().titleProperty());
//			tab.graphicProperty().bind(getCurrentViewMetadata().get().graphicsProperty());
//		});
//
//		tab.setOnClosed(e -> {
//			try {
//				destroy();
//			} catch (Exception exception) {
//				exceptionHandler.setException(exception);
//			}
//		});
		tab.setContent(innerWrapper.getWrap());
	}

	@Override
	public <U> void switchView(FlowView<U> view, boolean remove) {
		innerWrapper.switchView(view, remove);
	}

	@Override
	public Tab getWrap() {
		return tab;
	}
}
