package com.example.android_seep;


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


import com.example.query.Base;
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
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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
import android.widget.TextView;
import android.widget.ToggleButton;
import uk.ac.imperial.lsds.seep.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends Activity {
	private static final String    TAG                 = "Android-Seep-FR::Activity";
	Logger LOG = LoggerFactory.getLogger(Base.class);

	ToggleButton btn_startMaster;
	ToggleButton btn_startWorkers;
	ToggleButton btn_deploy;
	ToggleButton btn_start;
	Button btn_stop;

	Activity self;
	Main instance;
	private static Context context;
	
	TextView textresult;
	public static Handler mHandler;
	private Handler mHandler2 = new Handler();

	public static String hostIP = "xx.xx.xx.xx";
	public static int level, scale;
	public static WifiManager mainWifi;
	public static boolean isSystemRunning = false;
	WakeLock wakeLock;
	
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
		context = getApplicationContext();
		addListenerOnButtons();
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
		        "MyWakelockTag");
		wakeLock.acquire();
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.obj != null){	
					textresult.setText(msg.obj.toString());
				}
			}
		};

	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		AsyncActionToGetIP asyncAction = new AsyncActionToGetIP();
		try {
			hostIP = asyncAction.execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		new AlertDialog.Builder(this)
		//		.setTitle("Notice")
		//		.setMessage("Your IP address is: "+hostIP)
		//		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		//			public void onClick(DialogInterface dialog, int which) { 
		//				// continue with delete
		//			}
		//		})
		//		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		//			public void onClick(DialogInterface dialog, int which) { 
		//				// do nothing
		//			}
		//		})
		//		.setIcon(android.R.drawable.ic_dialog_alert)
		//		.show();

	}

	public static Context getAppContext() {
        return context;
    }
	
	class AsyncActionToGetIP extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... args) { 
			StringBuilder IFCONFIG=new StringBuilder();
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
							IFCONFIG.append(inetAddress.getHostAddress().toString());
						}
					}
				}
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LOG.error("..........."+IFCONFIG.toString());
			return IFCONFIG.toString();
		}
	}



	public float getBatteryLevel() {
	    Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

	    // Error checking that probably isn't needed but I added just in case.
	    if(level == -1 || scale == -1) {
	        return 50.0f;
	    }

	    return ((float)level / (float)scale) * 100.0f; 
	}
	
	public void addListenerOnButtons(){
		btn_startMaster = (ToggleButton) findViewById(R.id.button1);
		btn_startWorkers = (ToggleButton) findViewById(R.id.button2);
		btn_deploy = (ToggleButton) findViewById(R.id.button3);
		btn_start = (ToggleButton) findViewById(R.id.button4);
		btn_stop = (Button) findViewById(R.id.button5);

		final String classname = "com.example.query.Base";

		btn_startMaster.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				btn_startMaster.setChecked(true);
				instance = new Main();
				instance.executeMaster();
			}
		});

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
					}
				}, 1500);
				
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
				wakeLock.release();
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
	}

	public void onDestroy() {
		super.onDestroy();
	}


}
