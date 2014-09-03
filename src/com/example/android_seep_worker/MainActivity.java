package com.example.android_seep_worker;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
//import org.opencv.contrib.FaceRecognizer;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import com.googlecode.javacv.cpp.opencv_imgproc;

import com.example.query.Base;
import com.example.query.PersonRecognizer;
import com.example.query.Tutorial3View;
import com.example.query.labels;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import uk.ac.imperial.lsds.seep.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends Activity {
	private static final String    TAG                 = "Android-Seep-FR::Activity";
	Logger LOG = LoggerFactory.getLogger(Base.class);

	ToggleButton btn_startWorkers;

	Activity self;
	Main instance;
	public static final int        JAVA_DETECTOR       = 0;
	public static final int        NATIVE_DETECTOR     = 1;

	private File                   mCascadeFile;
	public static CascadeClassifier      mJavaDetector;
	private int                    mDetectorType       = JAVA_DETECTOR;
	private String[]               mDetectorName;

	String mPath="";
	ImageView mImageView;
	TextView textresult;
	public static PersonRecognizer fr;
	com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;
	private static Handler mImageViewHandler;
	private static Handler mTextViewHandler;

	static final long MAXIMG = 10;
	ArrayList<Mat> alimgs = new ArrayList<Mat>();
	int[] labels = new int[(int)MAXIMG];
	int countImages=0;
	private Handler mHandler2 = new Handler();
	private static Context context;

	public static String hostIP = "xx.xx.xx.xx";
	public static int level, scale;
	public static WifiManager mainWifi;
	public static boolean isSystemRunning = false;
	
	static Bitmap currentFrame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		self = this;
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    this.context = getApplicationContext();
		addListenerOnButtons();

		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java";
		mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

		mPath=Environment.getExternalStorageDirectory()+"/facerecogOCV/";


		mTextViewHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.obj != null){	
					textresult.setText(msg.obj.toString());
				} 
			}
		};
		
		mImageViewHandler = new Handler(){
			
			@Override
			public void handleMessage(Message msg) {
				
				if(msg.what == 1){
					currentFrame= (Bitmap) msg.obj;
					if(currentFrame!=null)
						mImageView.setImageBitmap(currentFrame);
				}
				
				if(msg.what == 2){
					Bitmap tempBitmap = Bitmap.createBitmap(currentFrame.getWidth(), currentFrame.getHeight(), Bitmap.Config.RGB_565);
					Paint myPaint = new Paint();
					myPaint.setStyle(Paint.Style.STROKE);
					myPaint.setColor(0xFF3399FF);
					myPaint.setStrokeWidth(5);
					
					Canvas tempCanvas = new Canvas(tempBitmap);		
					
					Bundle b=msg.getData();
					 
		            //log the data received
		            float x = (float) b.getInt("x");
		            float y = (float) b.getInt("y");
		            float width = (float) b.getInt("width");
		            float height = (float) b.getInt("height");		 
					
		            if (x+y+width+height > 0){
					RectF rect = new RectF(x,
							y,
							x+width,
							y+height);
					//Draw the image bitmap into the cavas
					tempCanvas.drawBitmap(currentFrame, 0, 0, null);

					//Draw everything else you want into the canvas, in this example a rectangle with rounded edges
					tempCanvas.drawRect(rect, myPaint);

					//Attach the canvas to the ImageView
					mImageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
		            }
				}
				
				super.handleMessage(msg);
			}
		};

		
		BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			}
		};
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, filter);
	}

	public static Context getAppContext(){
		return context;
	}
	
	public static Handler getImageViewHandler(){
		return mImageViewHandler;
	}
	
	public static Handler getTextViewHandler(){
		return mTextViewHandler;
	}	
	
	


	public void addListenerOnButtons(){
		btn_startWorkers = (ToggleButton) findViewById(R.id.button2);

		final String classname = "com.example.query.Base";

		
		btn_startWorkers.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				btn_startWorkers.setChecked(true);
				Main instance1 = new Main();
				String worker = "Worker";
				String port = "2001";
				String[] args = {worker, port};
				instance1.executeSec(args);

				mHandler2.postDelayed(new Runnable() {
					public void run() {
						Main instance2 = new Main();
						String worker = "Worker";
						String port = "2002";
						String[] args = {worker, port};
						args[1] = port;
						instance2.executeSec(args);
					}
				}, 200);
			}
		});

		

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();      
	}

	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
	}




	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");

				fr=new PersonRecognizer(mPath);
				fr.load();


				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
					if (mJavaDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}

			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;

			}
		}
	};
}
