package com.example.modelloading;

import java.util.ArrayList;

import vuforia.LoadingDialogHandler;
import vuforia.SampleApplicationControl;
import vuforia.SampleApplicationException;
import vuforia.SampleApplicationSession;
import vuforia.ui.SampleAppMenu;
import vuforia.ui.SampleAppMenuGroup;
import vuforia.ui.SampleAppMenuInterface;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;

public class MainActivity extends Activity implements SampleApplicationControl,
		SampleAppMenuInterface {

	private GLSurfaceView mGLView;
	SampleApplicationSession vuforiaAppSession;
	private RelativeLayout mUILayout;
	LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
	private GestureDetector mGestureDetector;
	private DataSet mCurrentDataset;
	private int mStartDatasetsIndex = 0;
	private int mDatasetsNumber = 0;
	private ArrayList<String> mDatasetStrings = new ArrayList<String>();
	private int mCurrentDatasetSelectionIndex = 0;
	private boolean mExtendedTracking = false;
	private static final String LOGTAG = "ImageTargets";
	private boolean mContAutofocus = false;
	private SampleAppMenu mSampleAppMenu;
	private View mFlashOptionView;
	private boolean mFlash = false;
	private boolean mSwitchDatasetAsap = false;
	boolean mIsDroidDevice = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startLoadingAnimation();
		mDatasetStrings.add("StonesAndChips.xml");
		mDatasetStrings.add("Tarmac.xml");
		vuforiaAppSession = new SampleApplicationSession(this);
		vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mGestureDetector = new GestureDetector(this, new GestureListener());
		mGLView = new MyGLSurfaceView(this,vuforiaAppSession,loadingDialogHandler,(TextView)findViewById(R.id.name),(TextView)findViewById(R.id.horseName),(TextView)findViewById(R.id.trainerName),(TextView)findViewById(R.id.currentPosition),(ImageView)findViewById(R.id.imgExercise));
		mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
				"droid");
		
	}

	// Called when the activity will start interacting with the user.
	@Override
	protected void onResume() {
		Log.d(LOGTAG, "onResume");
		super.onResume();

		// This is needed for some Droid devices to force portrait
		if (mIsDroidDevice) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		try {
			vuforiaAppSession.resumeAR();
		} catch (SampleApplicationException e) {
			Log.e(LOGTAG, e.getString());
		}

		// Resume the GL view:
		if (mGLView != null) {
			mGLView.setVisibility(View.VISIBLE);
			mGLView.onResume();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean doInitTrackers() {
		// Indicate if the trackers were initialized correctly
		boolean result = true;

		TrackerManager tManager = TrackerManager.getInstance();
		Tracker tracker;

		// Trying to initialize the image tracker
		tracker = tManager.initTracker(ImageTracker.getClassType());
		if (tracker == null) {
			Log.e("error",
					"Tracker not initialized. Tracker already initialized or the camera is already started");
			result = false;
		} else {
			Log.i("success", "Tracker successfully initialized");
		}
		return result;
	}

	@Override
	public boolean doLoadTrackersData() {
		TrackerManager tManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) tManager
				.getTracker(ImageTracker.getClassType());
		if (imageTracker == null)
			return false;

		if (mCurrentDataset == null)
			mCurrentDataset = imageTracker.createDataSet();

		if (mCurrentDataset == null)
			return false;

		if (!mCurrentDataset.load(
				mDatasetStrings.get(mCurrentDatasetSelectionIndex),
				DataSet.STORAGE_TYPE.STORAGE_APPRESOURCE))
			return false;

		if (!imageTracker.activateDataSet(mCurrentDataset))
			return false;

		int numTrackables = mCurrentDataset.getNumTrackables();
		for (int count = 0; count < numTrackables; count++) {
			Trackable trackable = mCurrentDataset.getTrackable(count);
			if (isExtendedTrackingActive()) {
				trackable.startExtendedTracking();
			}

			String name = "Current Dataset : " + trackable.getName();
			trackable.setUserData(name);
			Log.d("data", "UserData:Set the following user data "
					+ (String) trackable.getUserData());
		}

		return true;
	}

	@Override
	public boolean doStartTrackers() {
		// Indicate if the trackers were started correctly
		boolean result = true;

		Tracker imageTracker = TrackerManager.getInstance().getTracker(
				ImageTracker.getClassType());
		if (imageTracker != null)
			imageTracker.start();

		return result;
	}

	@Override
	public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;
        
        Tracker imageTracker = TrackerManager.getInstance().getTracker(
            ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.stop();
        
        return result;
	}

	@Override
	public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) tManager
            .getTracker(ImageTracker.getClassType());
        if (imageTracker == null)
            return false;
        
        if (mCurrentDataset != null && mCurrentDataset.isActive())
        {
            if (imageTracker.getActiveDataSet().equals(mCurrentDataset)
                && !imageTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            } else if (!imageTracker.destroyDataSet(mCurrentDataset))
            {
                result = false;
            }
            
            mCurrentDataset = null;
        }
        
        return result;
	}

	@Override
	public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ImageTracker.getClassType());
        
        return result;
	}

	@Override
	public void onInitARDone(SampleApplicationException e) {
		// TODO Auto-generated method stub
		if (e == null) {
			// Now add the GL surface view. It is important
			// that the OpenGL ES surface view gets added
			// BEFORE the camera is started and video
			// background is configured.
			RelativeLayout contrainer = (RelativeLayout) this.findViewById(R.id.arLayout);
			DisplayMetrics metrics = new DisplayMetrics();
		    this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		    int mScreenHeight =  (metrics.heightPixels/2) + (metrics.heightPixels/2 - this.findViewById(R.id.imgExercise).getHeight())/2;
			contrainer.addView(mGLView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

			// Sets the UILayout to be drawn in front of the camera
			mUILayout.bringToFront();

			// Sets the layout background to transparent
			//mUILayout.setBackgroundColor(Color.TRANSPARENT);

			try {
				vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
			} catch (SampleApplicationException e1) {
				Log.e(LOGTAG, e1.getString());
			}
			boolean result = CameraDevice.getInstance().setFocusMode(
					CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
			if (result)
				mContAutofocus = true;
			else
				Log.e(LOGTAG, "Unable to enable continuous autofocus");

			mSampleAppMenu = new SampleAppMenu(this, this, "Image Targets",
					mGLView, mUILayout, null);
			setSampleAppMenuSettings();

		} else {
			Log.e(LOGTAG, e.getString());
			finish();
		}
	}

	@Override
	public void onQCARUpdate(State state) {
		// TODO Auto-generated method stub

	}

	private void startLoadingAnimation() {
		LayoutInflater inflater = LayoutInflater.from(this);
		mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
				null, false);

		mUILayout.setVisibility(View.VISIBLE);
		mUILayout.setBackgroundColor(Color.LTGRAY);

		// Gets a reference to the loading dialog
		loadingDialogHandler.mLoadingDialogContainer = mUILayout
				.findViewById(R.id.loading_indicator);

		// Shows the loading indicator at start
		loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
		// Adds the inflated layout to the view
		addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

	}

	// Process Single Tap event to trigger autofocus
	private class GestureListener extends
			GestureDetector.SimpleOnGestureListener {
		// Used to set autofocus one second after a manual focus is triggered
		private final Handler autofocusHandler = new Handler();

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// Generates a Handler to trigger autofocus
			// after 1 second
			autofocusHandler.postDelayed(new Runnable() {
				public void run() {
					boolean result = CameraDevice.getInstance().setFocusMode(
							CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

					if (!result)
						Log.e("SingleTapUp", "Unable to trigger focus");
				}
			}, 1000L);

			return true;
		}
	}

	boolean isExtendedTrackingActive() {
		return mExtendedTracking;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Process the Gestures
		if (mSampleAppMenu != null && mSampleAppMenu.processEvent(event))
			return true;

		return mGestureDetector.onTouchEvent(event);
	}

	final public static int CMD_BACK = -1;
	final public static int CMD_EXTENDED_TRACKING = 1;
	final public static int CMD_AUTOFOCUS = 2;
	final public static int CMD_FLASH = 3;
	final public static int CMD_CAMERA_FRONT = 4;
	final public static int CMD_CAMERA_REAR = 5;
	final public static int CMD_DATASET_START_INDEX = 6;

	// This method sets the menu's settings
	private void setSampleAppMenuSettings() {
		SampleAppMenuGroup group;

		group = mSampleAppMenu.addGroup("", false);

		group.addTextItem(getString(R.string.menu_back), -1);

		group = mSampleAppMenu.addGroup("", true);
		group.addSelectionItem(getString(R.string.menu_extended_tracking),
				CMD_EXTENDED_TRACKING, false);
		group.addSelectionItem(getString(R.string.menu_contAutofocus),
				CMD_AUTOFOCUS, mContAutofocus);
		mFlashOptionView = group.addSelectionItem(
				getString(R.string.menu_flash), CMD_FLASH, false);

		CameraInfo ci = new CameraInfo();
		boolean deviceHasFrontCamera = false;
		boolean deviceHasBackCamera = false;
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, ci);
			if (ci.facing == CameraInfo.CAMERA_FACING_FRONT)
				deviceHasFrontCamera = true;
			else if (ci.facing == CameraInfo.CAMERA_FACING_BACK)
				deviceHasBackCamera = true;
		}

		if (deviceHasBackCamera && deviceHasFrontCamera) {
			group = mSampleAppMenu.addGroup(getString(R.string.menu_camera),
					true);
			group.addRadioItem(getString(R.string.menu_camera_front),
					CMD_CAMERA_FRONT, false);
			group.addRadioItem(getString(R.string.menu_camera_back),
					CMD_CAMERA_REAR, true);
		}

		group = mSampleAppMenu
				.addGroup(getString(R.string.menu_datasets), true);
		mStartDatasetsIndex = CMD_DATASET_START_INDEX;
		mDatasetsNumber = mDatasetStrings.size();

		group.addRadioItem("Stones & Chips", mStartDatasetsIndex, true);
		group.addRadioItem("Tarmac", mStartDatasetsIndex + 1, false);

		mSampleAppMenu.attachMenu();
	}

	@Override
	public boolean menuProcess(int command) {
		boolean result = true;

		switch (command) {
		case CMD_BACK:
			finish();
			break;

		case CMD_FLASH:
			result = CameraDevice.getInstance().setFlashTorchMode(!mFlash);

			if (result) {
				mFlash = !mFlash;
			} else {
				showToast(getString(mFlash ? R.string.menu_flash_error_off
						: R.string.menu_flash_error_on));
				Log.e(LOGTAG, getString(mFlash ? R.string.menu_flash_error_off
						: R.string.menu_flash_error_on));
			}
			break;

		case CMD_AUTOFOCUS:

			if (mContAutofocus) {
				result = CameraDevice.getInstance().setFocusMode(
						CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);

				if (result) {
					mContAutofocus = false;
				} else {
					showToast(getString(R.string.menu_contAutofocus_error_off));
					Log.e(LOGTAG,
							getString(R.string.menu_contAutofocus_error_off));
				}
			} else {
				result = CameraDevice.getInstance().setFocusMode(
						CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

				if (result) {
					mContAutofocus = true;
				} else {
					showToast(getString(R.string.menu_contAutofocus_error_on));
					Log.e(LOGTAG,
							getString(R.string.menu_contAutofocus_error_on));
				}
			}

			break;

		case CMD_CAMERA_FRONT:
		case CMD_CAMERA_REAR:

			// Turn off the flash
			if (mFlashOptionView != null && mFlash) {
				// OnCheckedChangeListener is called upon changing the checked
				// state
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
					((Switch) mFlashOptionView).setChecked(false);
				} else {
					((CheckBox) mFlashOptionView).setChecked(false);
				}
			}

			doStopTrackers();
			CameraDevice.getInstance().stop();
			CameraDevice.getInstance().deinit();
			try {
				vuforiaAppSession
						.startAR(command == CMD_CAMERA_FRONT ? CameraDevice.CAMERA.CAMERA_FRONT
								: CameraDevice.CAMERA.CAMERA_BACK);
			} catch (SampleApplicationException e) {
				showToast(e.getString());
				Log.e(LOGTAG, e.getString());
				result = false;
			}
			doStartTrackers();
			break;

		case CMD_EXTENDED_TRACKING:
			for (int tIdx = 0; tIdx < mCurrentDataset.getNumTrackables(); tIdx++) {
				Trackable trackable = mCurrentDataset.getTrackable(tIdx);

				if (!mExtendedTracking) {
					if (!trackable.startExtendedTracking()) {
						Log.e(LOGTAG,
								"Failed to start extended tracking target");
						result = false;
					} else {
						Log.d(LOGTAG,
								"Successfully started extended tracking target");
					}
				} else {
					if (!trackable.stopExtendedTracking()) {
						Log.e(LOGTAG, "Failed to stop extended tracking target");
						result = false;
					} else {
						Log.d(LOGTAG,
								"Successfully started extended tracking target");
					}
				}
			}

			if (result)
				mExtendedTracking = !mExtendedTracking;

			break;

		default:
			if (command >= mStartDatasetsIndex
					&& command < mStartDatasetsIndex + mDatasetsNumber) {
				mSwitchDatasetAsap = true;
				mCurrentDatasetSelectionIndex = command - mStartDatasetsIndex;
			}
			break;
		}

		return result;
	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	 // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();
        
        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        
        System.gc();
    }

}
