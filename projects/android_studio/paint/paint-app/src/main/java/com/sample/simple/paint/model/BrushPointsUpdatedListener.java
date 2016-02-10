package com.sample.simple.paint.model;

public interface BrushPointsUpdatedListener {
	public void onImageUpdated(int top, int left, int bottom, int right);
	public void onImageUpdated();
}
