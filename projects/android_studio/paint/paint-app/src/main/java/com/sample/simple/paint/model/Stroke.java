package com.sample.simple.paint.model;

import java.util.ArrayList;

import com.simple.sample.paint.model.surface.BrushPoint;

public interface Stroke {
	public void addBrushPoints(ArrayList<BrushPoint> points, String layer);
	public void undo();
}
