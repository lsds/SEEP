package com.example.android_seep;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;
import uk.ac.imperial.lsds.seep.Main;

public class MainActivity extends ActionBarActivity {

	Button btn_startMaster;
	Button btn_startWorkers;
	Button btn_deploy;
	Button btn_start;
	Button btn_stop;
	TextView t1,t2,t3,t4,t5;
	ActionBarActivity self;
	Main instance;
    private Handler mHandler = new Handler();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment())
			.commit();
		}

		addListenerOnButtons();
		self = this;
		addTextViews();
	}

	public void addListenerOnButtons(){

		btn_startMaster = (Button) findViewById(R.id.button1);
		btn_startWorkers = (Button) findViewById(R.id.button2);
		btn_deploy = (Button) findViewById(R.id.button3);
		btn_start = (Button) findViewById(R.id.button4);
		btn_stop = (Button) findViewById(R.id.button5);
		final String master = "Master";
		final String classname = "query.base.Base";

		btn_startMaster.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				
				String[] args = {master, classname};
				try {
					instance = new Main();
					instance.executeMaster(args);
					t1.setText("Starting MonitorMaster on localhost:5555");
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		btn_startWorkers.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){

				Main instance1 = new Main();
				String worker = "Worker";
				String port = "2001";
				String[] args = {worker, port};
				instance1.executeSec(args);

				Main instance2 = new Main();
				port = "2002";
				args[1] = port;
				instance2.executeSec(args);

				Main instance3 = new Main();
				port = "2003";
				args[1] = port;
				instance3.executeSec(args);

				t2.setText("Waiting for incoming requests on port: 2001, 2002, 2003");
			}
		});


		btn_deploy.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				instance.deploy(classname);			
				t3.setText("Deploying...");
				mHandler.postDelayed(new Runnable() {
		            public void run() {
		            	instance.deploy2();
		            	t3.setText("Deploying done");
		            }
		        }, 2000);
			}
		});
		
		btn_start.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				instance.start();
				t4.setText("Starting system");
			}
		});
		
		btn_stop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0){
				finish(); 
				//t3.setText("Deploying done");
			}
		});
	}

	public void addTextViews(){

		t1 = (TextView) findViewById(R.id.textView1);
		t2 = (TextView) findViewById(R.id.textView2);
		t3 = (TextView) findViewById(R.id.textView3);
		t4 = (TextView) findViewById(R.id.textView4);
		t5 = (TextView) findViewById(R.id.textView5);

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

}
