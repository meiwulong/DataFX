package io.datafx.flow.wrapper;

import io.datafx.flow.wrapper.AnimatedFlowViewWrapper;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public enum AnimationDefine {
	FADE((c) -> {
		return Arrays.asList(
				new KeyFrame(Duration.ZERO, new KeyValue(c.getPlaceholder().opacityProperty(), 1.0D, Interpolator.EASE_BOTH)),
				new KeyFrame(c.getDuration(), new KeyValue(c.getPlaceholder().opacityProperty(), 0.0D, Interpolator.EASE_BOTH)));
	}),
	ZOOM_IN((c) -> {
		return Arrays.asList(
				new KeyFrame(Duration.ZERO, new KeyValue(c.getPlaceholder().scaleXProperty(), 1, Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().scaleYProperty(), 1, Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().opacityProperty(), 1.0D, Interpolator.EASE_BOTH)),
				new KeyFrame(c.getDuration(), new KeyValue(c.getPlaceholder().scaleXProperty(), 4, Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().scaleYProperty(), 4, Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().opacityProperty(), 0, Interpolator.EASE_BOTH)));
	}),
	ZOOM_OUT((c) -> {
		return Arrays.asList(
				new KeyFrame(Duration.ZERO, new KeyValue(c.getPlaceholder().scaleXProperty(), 1, Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().scaleYProperty(), 1, Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().opacityProperty(), 1.0D, Interpolator.EASE_BOTH)),
				new KeyFrame(c.getDuration(), new KeyValue(c.getPlaceholder().scaleXProperty(), 0, Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().scaleYProperty(), 0, Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().opacityProperty(), 0, Interpolator.EASE_BOTH)));
	}),
	SWIPE_LEFT((c) -> {
		return Arrays.asList(
				new KeyFrame(Duration.ZERO, new KeyValue(c.getWrap().translateXProperty(), c.getWrap().getWidth(), Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().translateXProperty(), -c.getWrap().getWidth(), Interpolator.EASE_BOTH)),
				new KeyFrame(c.getDuration(), new KeyValue(c.getWrap().translateXProperty(), 0, Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().translateXProperty(), -c.getWrap().getWidth(), Interpolator.EASE_BOTH)));
	}),
	SWIPE_RIGHT((c) -> {
		return Arrays.asList(
				new KeyFrame(Duration.ZERO, new KeyValue(c.getWrap().translateXProperty(), -c.getWrap().getWidth(), Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().translateXProperty(), c.getWrap().getWidth(), Interpolator.EASE_BOTH)),
				new KeyFrame(c.getDuration(), new KeyValue(c.getWrap().translateXProperty(), 0, Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().translateXProperty(), c.getWrap().getWidth(), Interpolator.EASE_BOTH)));
	}),

	SWIPE_UP((c) -> {
		return Arrays.asList(
				new KeyFrame(Duration.ZERO, new KeyValue(c.getWrap().translateYProperty(), c.getWrap().getHeight(), Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().translateYProperty(), -c.getWrap().getHeight(), Interpolator.EASE_BOTH)),
				new KeyFrame(c.getDuration(), new KeyValue(c.getWrap().translateYProperty(), 0, Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().translateYProperty(), -c.getWrap().getHeight(), Interpolator.EASE_BOTH)));
	}),
	SWIPE_DOWN((c) -> {
		return Arrays.asList(
				new KeyFrame(Duration.ZERO, new KeyValue(c.getWrap().translateYProperty(), -c.getWrap().getHeight(), Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().translateYProperty(), c.getWrap().getHeight(), Interpolator.EASE_BOTH)),
				new KeyFrame(c.getDuration(), new KeyValue(c.getWrap().translateYProperty(), 0, Interpolator.EASE_BOTH), new KeyValue(c.getPlaceholder().translateYProperty(), c.getWrap().getHeight(), Interpolator.EASE_BOTH)));
	}),
	;

	private Function<AnimatedFlowViewWrapper, List<KeyFrame>> animationProducer;

	AnimationDefine(Function<AnimatedFlowViewWrapper, List<KeyFrame>> animationProducer) {
		this.animationProducer = animationProducer;
	}

	public Function<AnimatedFlowViewWrapper, List<KeyFrame>> getAnimationProducer() {
		return this.animationProducer;
	}
}
