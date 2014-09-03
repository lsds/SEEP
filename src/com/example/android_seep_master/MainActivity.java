package com.example.android_seep_master;


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
import android.app.AlertDialog;

public class MainActivity extends Activity {
	private static final String    TAG                 = "Android-Seep-FR::Activity";
	Logger LOG = LoggerFactory.getLogger(Base.class);

	ToggleButton btn_local;
	ToggleButton btn_startWorkers;
	ToggleButton btn_deploy;
	ToggleButton btn_start;

	Button btn_stop;

	
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
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		self = this;
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    this.context = getApplicationContext();
		addListenerOnButtons();

		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java";
		mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";
		mImageView = (ImageView) findViewById(R.id.imageView);
		textresult = (TextView) findViewById(R.id.textResult);

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
		
		instance = new Main();
		instance.executeMaster();
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
		textresult = (TextView) findViewById(R.id.textResult);
		btn_local = (ToggleButton) findViewById(R.id.button1);
		btn_startWorkers = (ToggleButton) findViewById(R.id.button2);
		btn_deploy = (ToggleButton) findViewById(R.id.button3);
		btn_start = (ToggleButton) findViewById(R.id.button4);
		btn_stop = (Button) findViewById(R.id.button5);

		final String classname = "com.example.query.Base";

		btn_local.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				btn_local.setChecked(true);
				btn_startWorkers.setClickable(false);
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
				}, 500);
				mHandler2.postDelayed(new Runnable() {
					public void run() {
						Main instance3 = new Main();
						String worker = "Worker";
						String port = "2003";
						String[] args = {worker, port};
						args[1] = port;
						instance3.executeSec(args);
					}
				}, 1000);
				mHandler2.postDelayed(new Runnable() {
					public void run() {
						Main instance4 = new Main();
						String worker = "Worker";
						String port = "2004";
						String[] args = {worker, port};
						args[1] = port;
						instance4.executeSec(args);
						Toast.makeText(getApplicationContext(), "Done! Please deploy!", 
								   Toast.LENGTH_LONG).show();
					}
				}, 1500);
			}
			
		});
		
		btn_startWorkers.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				
				btn_startWorkers.setChecked(true);
				btn_local.setClickable(false);
				Main instance1 = new Main();
				String worker = "Worker";
				String port = "2001";
				String[] args = {worker, port};
				instance1.executeSec(args);
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
				// set title
				alertDialogBuilder.setTitle("Please join the workers")
				.setMessage("Click when workers have joined")
				      .setCancelable(false)
				      .setNeutralButton("Done",
				         new DialogInterface.OnClickListener() {
				         public void onClick(DialogInterface dialog, int whichButton){
				        	 Main instance2 = new Main();
								String worker = "Worker";
								String port = "2002";
								String[] args = {worker, port};
								args[1] = port;
								instance2.executeSec(args);
								Toast.makeText(getApplicationContext(), "Done! Please deploy!", 
										   Toast.LENGTH_LONG).show();
				         }
				         })
				      .show();

			}
			
		});

		btn_deploy.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				btn_deploy.setChecked(true);
				instance.deploy(classname, 40000, 50000);	
				LOG.info("===============1=============");
				mHandler2.postDelayed(new Runnable() {
					public void run() {
						instance.deploy0(40000, 50000);
						LOG.info("===============2=============");
					}
				}, 500);
				mHandler2.postDelayed(new Runnable() {
					public void run() {
						instance.deploy1();
						LOG.info("===============3=============");
					}
				}, 1000);
				mHandler2.postDelayed(new Runnable() {
					public void run() {
						instance.deploy2();
						LOG.info("===============4=============");
					}
				}, 3000);
				mHandler2.postDelayed(new Runnable() {
					public void run() {
						instance.deploy3();
						LOG.info("===============5=============");
						Toast.makeText(getApplicationContext(), "Done! Please start!", 
								   Toast.LENGTH_LONG).show();
					}
				}, 5000);
			}
		});

		btn_start.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				btn_start.setChecked(true);
				isSystemRunning = true;
				instance.start();
			}
		});


		btn_stop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				isSystemRunning = false;
				instance.stop();
				btn_startWorkers.setChecked(false);
				btn_deploy.setChecked(false);
				btn_start.setChecked(false);
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
