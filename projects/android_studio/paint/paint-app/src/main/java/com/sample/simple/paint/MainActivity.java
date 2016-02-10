package com.sample.simple.paint;

import java.util.ArrayList;

import com.sample.simple.paint.events.BrushTipSelectedEvent;
import com.sample.simple.paint.events.ClearImageEvent;
import com.sample.simple.paint.events.EraserTipSelectedEvent;
import com.sample.simple.paint.events.EventManager;
import com.sample.simple.paint.events.SetBrushColorEvent;
import com.sample.simple.paint.events.SetBrushSizeEvent;
import com.sample.simple.paint.events.UndoEvent;
import com.sample.simple.paint.model.BrushPointsUpdatedListener;
import com.sample.simple.paint.model.PaintApp;
import com.sample.simple.paint.model.PaintServer;
import com.sample.simple.paint.model.PaintSurfaceConfiguration;
import com.sample.simple.paint.model.SurfaceInteractionMode;
import com.simple.sample.paint.view.PaintCanvasView;
import android.view.ViewGroup;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.ThreadMode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class MainActivity extends Activity {

	PaintApp app;
	PaintServer server;
	PaintSurfaceConfiguration configuration;
	PaintCanvasView paintCanvas;
	
	int DEFAULT_BRUSH_SIZE_DP = 3;
	int DEFAULT_BRUSH_COLOR = Color.BLUE;
	
	boolean _initialStart = true;
	View _brushColorButton;
	
	ImageButton _paintBrushSelectButton;
	FrameLayout _selectedBrushTipContainer;
	View _selectedBrushSizeView; // Set this to the actual pixel size of the selected brush tip
	
	ImageButton _eraserSelectButton;
	TextView _eraserSizeIndicator;
	
	ImageButton _undoButton;
	ImageButton _clearSurfaceButton;
	
	ListView _colorBarView;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		setupPaintApp();
		setupUI();
	}

	
	private void setupPaintApp()
	{
		app = new PaintApp();
		app.createNewImage("default");
		app.setCurrentImage("default");
		
		server = new PaintServer();
		server.setImageSettings(app.getCurrentImageSettings());
		app.setImageChangedListener(server);
		
		
		server.setBrushPointUpdatedListener(new BrushPointsUpdatedListener() {
			
			@Override
			public void onImageUpdated() {
				paintCanvas.postInvalidate();
			}
			
			@Override
			public void onImageUpdated(int top, int left, int bottom, int right) {
				paintCanvas.updateViewBounds(top, left, bottom, right);
				paintCanvas.postInvalidate();
			}
		});
		
		
		
		
	}
	
	private void setDefaultBrushSize()
	{
		if (_initialStart)
		{
			int sizePix = convertDPtoPX(DEFAULT_BRUSH_SIZE_DP);
			EventManager.get().postEvent(SetBrushSizeEvent.get(sizePix));
			EventManager.get().postEvent(SetBrushColorEvent.get(DEFAULT_BRUSH_COLOR));
		}
		
	}
	
	// -~<(0)>~- -~<(0)>~- -~<(0)>~- -~<(0)>~- -~<(0)>~- -~<(0)>~- -~<(0)>~- -~<(0)>~- 
	//							UI Configuration Methods
	// -~<(0)>~- -~<(0)>~- -~<(0)>~- -~<(0)>~- -~<(0)>~- -~<(0)>~- -~<(0)>~- -~<(0)>~-
	
	private void setupUI()
	{
		EventManager.get().register(this);
		
		
		_brushColorButton = findViewById(R.id.vw_selected_color);
		// TODO: configure this to show color select dialog
		
		_paintBrushSelectButton = (ImageButton)findViewById(R.id.imb_select_brush);
		_selectedBrushTipContainer = (FrameLayout)findViewById(R.id.frm_brush_tip_select_container);
		_selectedBrushSizeView = findViewById(R.id.vw_brush_size);
		
		_eraserSelectButton = (ImageButton)findViewById(R.id.imb_select_eraser);
		_eraserSizeIndicator = (TextView)findViewById(R.id.txt_eraser_size_indicator);
		
		_undoButton = (ImageButton)findViewById(R.id.imb_undo);
		_clearSurfaceButton = (ImageButton)findViewById(R.id.imb_clear_picture);
		
		_colorBarView = (ListView)findViewById(R.id.lst_color_selector_bar);
		_colorBarView = (ListView)findViewById(R.id.lst_color_selector_bar);
		
		paintCanvas = (PaintCanvasView)findViewById(R.id.pcv_paint_canvas);
		paintCanvas.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
						server.beginStroke(app.getPaintConfiguration());
						server.addSurfaceTouches(app.getPaintConfiguration().getSurfaceTouches(event));
						return true;
					case MotionEvent.ACTION_UP:
						server.addSurfaceTouches(app.getPaintConfiguration().getSurfaceTouches(event));
						server.endStroke();
						return true;
					case MotionEvent.ACTION_MOVE:
						server.addSurfaceTouches(app.getPaintConfiguration().getSurfaceTouches(event));
						return true;
				}
				
				return false;
			}
		});
		paintCanvas.setPaintServer(server);
		// Setup the buttons
		
		_paintBrushSelectButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setSelected(_paintBrushSelectButton, true);
				setSelected(_eraserSelectButton, false);

				EventManager.get().postEvent(BrushTipSelectedEvent.get());
			}
		});
		
		
		_selectedBrushTipContainer.setFocusableInTouchMode(true);
		_selectedBrushTipContainer.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showBrushTipSelectDialog();
				
			}
		});
		
		
		_eraserSelectButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setSelected(_paintBrushSelectButton, false);
				setSelected(_eraserSelectButton, true);

				EventManager.get().postEvent(EraserTipSelectedEvent.get());
			}
		});
		
		// TODO: configure eraser tip select button, _eraserSizeIndicator
		
		
		_undoButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EventManager.get().postEvent(UndoEvent.get());
				
			}
		});
		
		_clearSurfaceButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EventManager.get().postEvent(ClearImageEvent.get());
				
			}
		});
		
		setupColorBar();
		
	}
	
	private void setupColorBar()
	{
		final ArrayList<Integer> colorList = new ArrayList<Integer>();
		for (String colorString:getResources().getStringArray(R.array.color_bar_colors))
		{
			colorList.add(Color.parseColor(colorString));
		}
		final ArrayAdapter<Integer> colorAdapter = new ArrayAdapter<Integer>(this, 0, colorList)
				{
					@Override
					public View getView(int position, View cachedView, ViewGroup listItemParentView)
					{
						if (cachedView == null)
						{
							View vw = new View(MainActivity.this);
							vw.setBackgroundColor(colorList.get(position));
							vw.setLayoutParams(new AbsListView.LayoutParams(getResources().getDimensionPixelSize(R.dimen.color_list_item_length), getResources().getDimensionPixelSize(R.dimen.color_list_item_length)));
							return vw;
						}
						return cachedView;
					}
				};
		
		_colorBarView.setAdapter(colorAdapter);
		_colorBarView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int color = colorList.get(position);
				EventManager.get().postEvent(SetBrushColorEvent.get(color));
				updateSelectedColorView(color);
			}
		});
	}
	
	private void updateSelectedColorView(int color)
	{
		LayerDrawable background = (LayerDrawable)_brushColorButton.getBackground();
		GradientDrawable top = (GradientDrawable)background.getDrawable(1);
		top.setColor(color);
	}
	
	private void showBrushTipSelectDialog()
	{
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.brush_tip_select_dialog);
		WindowManager.LayoutParams wparams = dialog.getWindow().getAttributes();
		wparams.width = getResources().getDimensionPixelSize(R.dimen.brush_tip_select_dialog_window_width);
		wparams.gravity = Gravity.CENTER;
		
		ListView brushView = (ListView)dialog.findViewById(R.id.lst_brush_tips);
		
		final ArrayList<Integer> tipPixelSizeList = new ArrayList<Integer>();
		for (int size:getResources().getIntArray(R.array.brush_sizes))
		{
			tipPixelSizeList.add(convertDPtoPX(Integer.valueOf(size)));
		}
		
		ArrayAdapter<Integer> tipAdapter = new ArrayAdapter<Integer>(this, 0, tipPixelSizeList)
				{
					@SuppressLint("NewApi")
					@Override
					public View getView(int position, View cachedView, ViewGroup listItemParentView)
					{
						if (cachedView == null)
						{
							int brushSizeDP = tipPixelSizeList.get(position);
							FrameLayout frame = new FrameLayout(MainActivity.this);
							frame.setBackgroundColor(getResources().getColor(R.color.unselected_tip_color));
							
							FrameLayout.LayoutParams childParams = new FrameLayout.LayoutParams(brushSizeDP, brushSizeDP);
							childParams.gravity = Gravity.CENTER;
							View tipView = new View(MainActivity.this);
							ShapeDrawable brushTip = new ShapeDrawable(new OvalShape());
							Paint p = brushTip.getPaint();
							p.setColor(Color.BLACK);
							if (Build.VERSION.SDK_INT < 16)
								tipView.setBackgroundDrawable(brushTip);
							else
								tipView.setBackground(brushTip);
							tipView.setLayoutParams(childParams);
							frame.addView(tipView);
							
							frame.setLayoutParams(new AbsListView.LayoutParams(getResources().getDimensionPixelSize(R.dimen.color_list_item_length), getResources().getDimensionPixelSize(R.dimen.color_list_item_length)));
							return frame;
						}
						return cachedView;
					}
				};
		brushView.setAdapter(tipAdapter);
		brushView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int size = tipPixelSizeList.get(position);
				EventManager.get().postEvent(SetBrushSizeEvent.get(size));
				dialog.dismiss();
			}
		});
		
		dialog.show();
	}
	
	
	
	@SuppressLint("NewApi")
	private void setSelected(ImageButton button, boolean isSelected)
	{
		if (isSelected)
		{
			button.setBackgroundResource(R.drawable.selected_tip);
		}
		else
		{
			if (Build.VERSION.SDK_INT < 16)
				button.setBackgroundDrawable(null);
			else
				button.setBackground(null);
			button.setBackgroundColor(Color.TRANSPARENT);
		}
	}
	
	
	
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

	@Override
	protected void onStart() {
		super.onStart();
		
		
	}

	@Override
	protected void onResume() {
		
		super.onResume();
		server.start();
		setDefaultBrushSize();
	}

	@Override
	protected void onPause() {
		server.stop();
		super.onPause();
	}

	@Override
	protected void onStop() {
		
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public int convertDPtoPX(int dp)
	{
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager m = (WindowManager)this.getSystemService(Context.WINDOW_SERVICE);
		m.getDefaultDisplay().getMetrics(dm);
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
	}
	
	// .oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo.
	//							Event Handlers
	// .oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo..oO0()0Oo.
	
	//@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SetBrushSizeEvent sizeEvent)
	{
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)_selectedBrushSizeView.getLayoutParams();
		params.height = sizeEvent.getBrushTipSize();
		params.width = sizeEvent.getBrushTipSize();
		_selectedBrushSizeView.requestLayout();
		
	}
	
	public void onEventMainThread(SetBrushColorEvent colorEvent)
	{
		updateSelectedColorView(colorEvent.getBrushColor());
		
	}
	
}
