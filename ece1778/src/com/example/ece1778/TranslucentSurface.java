package com.example.ece1778;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.android.ece1778.R;
import com.qualcomm.QCARSamples.ImageTargets.ImageTargets;
import com.qualcomm.QCARSamples.ImageTargets.ImageTargetsSplashScreen;

public class TranslucentSurface extends Activity {
	String TAG = "info";

	private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
	private final float TRACKBALL_SCALE_FACTOR = 36.0f;
	private CubeRenderer mRenderer;
	private float mPreviousX;
	private float mPreviousY;
	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;
	private String filename;
	private Camera mCamera;
	private CameraPreview mPreview;
	String root = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	VertexPackage mVertexPackage;

	private static boolean MAM = false;

	public String getFile(int plus) {
		File dir = new File(Environment.getExternalStorageDirectory() + "/3D/");
		File[] files = dir.listFiles();
		System.out.println(filename);
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(filename)) {
				if (i + plus >= files.length || i + plus <= 0)
					return files[0].getName();
				else
					return files[i + plus].getName();
			}
		}
		return filename;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.layout.menu, menu);
		return true;
	}

	
	private short tutorial_index = 0;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_next:
			tutorial_index++;
			Toast.makeText(this, "Next is Selected", Toast.LENGTH_SHORT).show();
			if (tutorial_index>mVertexPackage.list_vertex_cor_tri.size()-1)
			{
				tutorial_index = (short)(mVertexPackage.list_vertex_cor_tri.size() -1);
				Toast.makeText(this, "All components have been displayed", Toast.LENGTH_SHORT);
			}
			// new LoadTask(getFile(1), this).execute();
			mGLSurfaceView.addObject();
			return true;

		case R.id.menu_previous:
			Toast.makeText(this, "Previous is Selected", Toast.LENGTH_SHORT)
					.show();
			// new LoadTask(getFile(-1), this).execute();
			return true;

		case R.id.menu_search:
			Toast.makeText(this, "Search is Selected", Toast.LENGTH_SHORT)
					.show();
			return true;

		case R.id.menu_ar:
			Toast.makeText(this, "AR is Selected", Toast.LENGTH_SHORT)
					.show();
			Intent i = new Intent(this, ImageTargets.class);
			i.putExtra("VertexPackage", this.mVertexPackage);
			startActivity(i);
			return true;

		case R.id.menu_delete:
			Toast.makeText(this, "Delete is Selected", Toast.LENGTH_SHORT)
					.show();
			return true;

		case R.id.menu_preferences:
			Toast.makeText(this, "Preferences is Selected", Toast.LENGTH_SHORT)
					.show();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.main_content);
		// Create our Preview view and set it as the content of our
		// Activity
		
		mVertexPackage = (VertexPackage) getIntent()
				.getSerializableExtra("Package");
		
		MAM = getIntent().getBooleanExtra("MAM", false);
		filename = getIntent().getStringExtra("filename");
		System.out.println("2 " + filename);
		mGLSurfaceView = new TouchSurfaceView(this);

		boolean do_camera = false;
		if (do_camera) {
			mCamera = getCameraInstance();
			mPreview = new CameraPreview(this, mCamera);
			FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
			// System.out.println("here");
			if (mPreview == null)
				Log.w(TAG, "null camera");
			// preview.addView(mPreview);
			setContentView(mPreview);
		}
		// We want an 8888 pixel format because that's required for
		// a translucent window.
		// And we want a depth buffer.
		// mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		// Tell the cube renderer that we want to render a translucent version
		// of the cube:
		// mGLSurfaceView.setRenderer(new CubeRenderer(true));
		// Use a surface format with an Alpha channel:
		mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		// mGLSurfaceView.setZOrderOnTop(true);
		// preview.addView(mGLSurfaceView);
		// setContentView(mGLSurfaceView);
		addContentView(mGLSurfaceView, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());
		mGLSurfaceView.requestFocus();
		mGLSurfaceView.setFocusableInTouchMode(true);

	}

	@SuppressLint("NewApi")
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
			Camera.Parameters parameters = c.getParameters();
			parameters.set("orientation", "portrait");
			c.setDisplayOrientation(90);
			c.setParameters(parameters);
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			File pictureFile = null;

			try {
				// pictureFile = getOutputMediaFile();
				// FileOutputStream fos = new FileOutputStream(pictureFile);
				// fos.write(data);
				// fos.close();
			} catch (NullPointerException e) {
				Log.d(TAG,
						"Error creating media file, check storage permissions: "
								+ e.getMessage());
			}
			mCamera.startPreview();
			mCamera.setPreviewCallback(null);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLSurfaceView.onPause();
	}

	private TouchSurfaceView mGLSurfaceView;

	class TouchSurfaceView extends GLSurfaceView {
		public TouchSurfaceView(Context context) {
			super(context);

			
			mRenderer = new CubeRenderer(true, mVertexPackage);
			mRenderer.tutorial_mode = true;

			setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			setRenderer((Renderer) mRenderer);
			// setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}

		@Override
		public boolean onTrackballEvent(MotionEvent e) {
			mRenderer.mAngleX += e.getX() * TRACKBALL_SCALE_FACTOR;
			mRenderer.mAngleY += e.getY() * TRACKBALL_SCALE_FACTOR;
			requestRender();
			return true;
		}

		@Override
		public boolean onTouchEvent(MotionEvent e) {
			mScaleDetector.onTouchEvent(e);
			float x = e.getX();
			float y = e.getY();
			switch (e.getAction()) {
			case MotionEvent.ACTION_MOVE:
				float dx = x - mPreviousX;
				float dy = y - mPreviousY;
				mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR;
				mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR;

				requestRender();
			}
			mPreviousX = x;
			mPreviousY = y;
			return true;
		}
		
		@Override
		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			canvas.save();
			canvas.scale(mScaleFactor, mScaleFactor);
			// ...
			// Your onDraw() code
			// ...
			canvas.restore();
		}
		
		public void addObject()
		{
			mRenderer.tutorial_mode = true;
			mRenderer.which_component = tutorial_index;
			requestRender();
		}
	}

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();

			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 4.0f));

			// System.out.println(mScaleFactor);
			mRenderer.scale = mScaleFactor;
			return true;
		}
	}

}
