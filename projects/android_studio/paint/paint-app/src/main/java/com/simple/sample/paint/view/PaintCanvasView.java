package com.simple.sample.paint.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.sample.simple.paint.model.PaintServer;
import com.simple.sample.paint.model.surface.BrushPoint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Join;
import android.util.AttributeSet;
import android.view.View;

public class PaintCanvasView extends View
{
	Context _context;
	Paint _paint;
	Path _bezier;
	PaintServer _server;
	
	int top = 0;
	int left = 0;
	
	int bottom = 0;
	int right = 0;
	
	boolean _useSmoothingP = false;
	
	boolean invalidateRect = false;
	
	
	public PaintCanvasView(Context con)
	{
		super(con);
		configure();
	}
	
	
	public PaintCanvasView(Context con, AttributeSet attrib)
	{
		super(con, attrib);
		configure();
	}
	
	private void configure()
	{
		_paint = new Paint();
		_paint.setStyle(Paint.Style.STROKE);
		_paint.setStrokeCap(Paint.Cap.ROUND);
		_paint.setStrokeJoin(Join.ROUND);
		_bezier = new Path();
	}
	
	public void setPaintServer(PaintServer server)
	{
		_server = server;
	}
	
	/**
	 * Update the range of BrushPoints to draw.  Not the same as invalidate(int, int, int, int) since
	 * points on the canvas outside of the BrushPoints still need to be draw due to the brush width
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public void updateViewBounds(int left, int top, int right, int bottom)
	{
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		invalidateRect = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		invalidateRect = false;
		ArrayList<BrushPoint> points = new ArrayList<BrushPoint>();
		
		if (_server != null)
		{
//			if (invalidateRect)
//			{
//				points = _server.getPoints(top, left, bottom, right);
//				
//			}
//			else
			{
				points = _server.getPoints();
			}
			
			if (points.size() == 0)
				return;
			
			PriorityQueue<BrushPoint> strokeQueue = new PriorityQueue<BrushPoint>(points.size(), new Comparator<BrushPoint>(){

				@Override
				public int compare(BrushPoint lhs, BrushPoint rhs) {
					
					if (lhs.getOrderIndex() < rhs.getOrderIndex())
						return -1;
					else if (lhs.getOrderIndex() == rhs.getOrderIndex())
						return 0;
					else
						return 1;
				}
				
			});
			
			for (BrushPoint pt:points)
			{
				strokeQueue.add(pt);
			}
			
			// Create splines
			BrushPoint prev, start = null, middle = null, end = null, next;
			
			if (_useSmoothingP)
			{
				int consecutiveCount = 0;
				
				outer: while (true)
				{
					switch (consecutiveCount)
					{
						case 0:
							_bezier.reset();
							start =  strokeQueue.poll();
							if (start == null)
								break outer;
							else
								_bezier.moveTo(start.x(), start.y());
							consecutiveCount++;
							break;
						case 1:
							middle = strokeQueue.poll();
							if (middle == null || ((middle.getOrderIndex() - start.getOrderIndex()) != 1))
							{
								drawCircle(canvas, start, true);
								if (middle == null)
									break outer;
								else
								{
									start = middle;
									_bezier.reset();
									_bezier.moveTo(start.x(), start.y());
								}
									
							}
							else
							{
								consecutiveCount++;
							}
							break;
						case 2:
							end = strokeQueue.poll();
							if (end == null || ((end.getOrderIndex() - middle.getOrderIndex()) != 1))
							{
								joinLine(canvas, start, middle, true);
								if (end == null)
									break outer;
								else
								{
									start = end;
									
									_bezier.moveTo(start.x(), start.y());
									
									consecutiveCount = 1;
								}
							}
							else
							{
								_paint.setColor(end.color());
								_paint.setStyle(Paint.Style.STROKE);
								_paint.setStrokeWidth(end.diameter());
								_bezier.quadTo(middle.x(), middle.y(), end.x(), end.y());
								canvas.drawPath(_bezier, _paint);
								consecutiveCount = 0;
							}
							break;
					}
				}
				
			}
			else
			{
				prev = strokeQueue.poll();
				while ((next = strokeQueue.poll())!=null)
				{
					_paint.setColor(next.color());
					_paint.setStrokeWidth(next.diameter());
					if (Math.abs(next.getOrderIndex() - prev.getOrderIndex()) == 1)
					{
						// join these points by a line
						
						joinLine(canvas, prev, next, false);
						
						
						//canvas.drawLine(prev.x(), prev.y(), next.x(), next.y(), _paint);
					}
					else
					{
						// This is to handle disconnected points.  Should do this better
						// to avoid repetition
						drawCircle(canvas, next, true);
					}
					prev = next;
				}
				
				
			}
			
		}
	}
	
	private void drawCircle(Canvas canvas, BrushPoint point, boolean updatePaint)
	{
		int diameter = point.diameter();
		if (updatePaint)
		{
			_paint.setStyle(Paint.Style.FILL);
			_paint.setColor(point.color());
			_paint.setStrokeWidth(0);
		}
		canvas.drawCircle(point.x(), point.y(), Math.max(1, diameter/2), _paint);
	}
	
	private void joinLine(Canvas canvas, BrushPoint start, BrushPoint end, boolean updatePaint)
	{
		if (updatePaint)
		{
			_paint.setColor(end.color());
			_paint.setStrokeWidth(end.diameter());
		}
		canvas.drawLine(start.x(), start.y(), end.x(), end.y(), _paint);
	}
}
