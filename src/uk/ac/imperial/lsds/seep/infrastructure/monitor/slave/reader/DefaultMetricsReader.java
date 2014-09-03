/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.reader;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Debug;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.example.android_seep_master.MainActivity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;

/**
 * Default concrete implementation of a metric reader for slaves. There might
 * be other readers that provide more detailed or application specific metrics.
 * A single slave node can read values from multiple readers.
 * 
 * @author mrouaux
 */
public class DefaultMetricsReader implements MetricsReader {

	private final static MetricRegistry metricRegistry 
	= SharedMetricRegistries.getOrCreate("uk.ac.imperial.lsds.seep.infrastructure.monitor");

	static {
		metricRegistry.register(MetricName.QUEUE_LENGTH.getName(), new Counter());
		metricRegistry.register(MetricName.OPERATOR_LATENCY.getName(), new Timer());
	}

	private static final String MEMORY_HEAP_SIZE_KEY = "heap.used";
	private static final String MEMORY_HEAP_UTIL_KEY = "heap.usage";

	// Initially, we will only support CPU utilisation on slave nodes and grow
	// from there. Typically, other metrics we might be interested on are heap
	// side/utilisation, processing delay and input queue length for an operator. 
	private MetricName[] readableNames = new MetricName[]{
			MetricName.CPU_UTILIZATION,
			//   MetricName.HEAP_SIZE,
			//   MetricName.HEAP_UTILIZATION,
			//   MetricName.QUEUE_LENGTH,
			//   MetricName.OPERATOR_LATENCY,
	      	MetricName.MEM_UTILIZATION,
	      	MetricName.BATTERY_LIFE,
	      	MetricName.WIFI_STRENGTH,
	};


	private MemoryUsageGaugeSet memoryMetricSet;

	/**
	 * Default constructor
	 */
	public DefaultMetricsReader() {
		// Get management beans to obtain JVM and OS runtime details


		// this.memoryMetricSet = new MemoryUsageGaugeSet();

	}

	/**
	 * @return List of metric names returned by this reader.
	 */
	@Override
	public List<MetricName> readableNames() {
		return Arrays.asList(readableNames);
	}

	/**
	 * Read a metric value for a given name.
	 * @param name Name of the metric to read.
	 * @return Metric value.
	 */
	@Override
	public MetricValue readValue(MetricName name) {
		MetricValue value = null;

		switch (name) {
		case CPU_UTILIZATION:
			value = readCpuUtilization();
			break;

		case HEAP_SIZE:
			value = readHeapSize();
			break;

		case HEAP_UTILIZATION:
			value = readHeapUtilization();
			break;

		case QUEUE_LENGTH:
			value = readQueueLength();
			break;

		case OPERATOR_LATENCY:
			value = readOperatorLatency();
			break;

		case MEM_UTILIZATION:
			value = readMemUtilization();
			break;
			
		case BATTERY_LIFE:
			value = readBatteryLife();
			break;
			
		case WIFI_STRENGTH:
			value = readWifiStrength();
			break;
		}

		return value;
	}

	/**
	 * Calculate CPU utilisation and return as MetricValue (percentage).
	 */
	
	class readCpuUtilizationClass implements Callable<Double> {
		@Override
		public Double call() {
			try {
						RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
						String load = reader.readLine();

						String[] toks = load.split(" ");

						long idle1 = Long.parseLong(toks[4]);
						long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
								+ Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

						try {
							Thread.sleep(360);
						} catch (Exception e) {}

						reader.seek(0);
						load = reader.readLine();
						reader.close();

						toks = load.split(" ");

						long idle2 = Long.parseLong(toks[4]);
						long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
								+ Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

						return (double) ((float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1)));

					} catch (IOException ex) {
						ex.printStackTrace();
					}
				return 0.0;
				}
	}
	
	
	private MetricValue readCpuUtilization() {
		
		MetricValue result = MetricValue.percent(0.0);
		final ExecutorService service;
        final Future<Double>  task;
        
        service = Executors.newFixedThreadPool(1);        
        task    = service.submit(new readCpuUtilizationClass());
        try 
        {
            final Double percent;

            // waits the 10 seconds for the Callable.call to finish.
            percent = task.get();
            result = MetricValue.percent(percent);
        }
        catch(final InterruptedException ex)
        {
            ex.printStackTrace();
        }
        catch(final ExecutionException ex)
        {
            ex.printStackTrace();
        }

        service.shutdownNow();
		
        return result;
	}

	/**
	 * Obtain heap used size and return as MetricValue (bytes).
	 */
	private MetricValue readHeapSize() {
		Map<String, Metric> metrics = memoryMetricSet.getMetrics();
		MetricValue value = null;

		if (metrics.containsKey(MEMORY_HEAP_SIZE_KEY)) {
			value = MetricValue.bytes(
					((Long) ((Gauge) metrics.get(MEMORY_HEAP_SIZE_KEY)).getValue()).intValue());
		}


		return value;
	}

	class readMemUtilizationClass implements Callable<Double> {
		@Override
		public Double call() {
			String str1 = "/proc/meminfo";
		    String str2="";
		    String[] arrayOfString;
		    long initial_memory = 0, free_memory = 0;
		    try {
		        FileReader localFileReader = new FileReader(str1);
		        BufferedReader localBufferedReader = new BufferedReader(
		            localFileReader, 8192);
		        for (int i = 0; i < 2; i++) {
		            str2 =str2+" "+ localBufferedReader.readLine();// meminfo  //THIS WILL READ meminfo AND GET BOTH TOT MEMORY AND FREE MEMORY eg-: Totalmemory 12345 KB //FREEMEMRY: 1234 KB  
		        }
		        arrayOfString = str2.split("\\s+");
		        
		        // total Memory
		        initial_memory = Integer.valueOf(arrayOfString[2]).intValue();
		        free_memory = Integer.valueOf(arrayOfString[5]).intValue();

		        localBufferedReader.close();
		    } catch (IOException e) {
		    }
		    return  (double)(initial_memory-free_memory)/(double)(initial_memory);
		}
	}
	
	private MetricValue readMemUtilization() {
		    

		MetricValue result = MetricValue.percent(0.0);
		final ExecutorService service;
        final Future<Double>  task;
        
        service = Executors.newFixedThreadPool(1);        
        task    = service.submit(new readMemUtilizationClass());
        try 
        {
            final Double percent;

            // waits the 10 seconds for the Callable.call to finish.
            percent = task.get();
            result = MetricValue.percent(percent);
        }
        catch(final InterruptedException ex)
        {
            ex.printStackTrace();
        }
        catch(final ExecutionException ex)
        {
            ex.printStackTrace();
        }

        service.shutdownNow();
		
        return result;
	}

	private MetricValue readBatteryLife(){
		Intent batteryIntent = MainActivity.getAppContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = batteryIntent.getIntExtra("level", -1);
		double scale = batteryIntent.getIntExtra("scale", -1);
		double level = -1;
		if (rawlevel >= 0 && scale > 0) {
				level = rawlevel / scale;
		}
		return MetricValue.percent(level);
	}
	
	private MetricValue readWifiStrength(){
		WifiInfo wifiInfo = MainActivity.mainWifi.getConnectionInfo();
		int strength=0;
		if(wifiInfo.getBSSID()!=null){
			strength = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 10);
		}
		return MetricValue.percent((double)strength/10);
	}
	/**
	 * Obtain heap utilisation and return as MetricValue (percentage).
	 */
	private MetricValue readHeapUtilization() {
		Map<String, Metric> metrics = memoryMetricSet.getMetrics();
		MetricValue value = null;

		if (metrics.containsKey(MEMORY_HEAP_UTIL_KEY)) {
			value = MetricValue.percent(
					(Double) ((Gauge) metrics.get(MEMORY_HEAP_UTIL_KEY)).getValue());
		}

		return value;
	}

	/**
	 * Obtain operator latency and return as MetricValue (milliseconds)
	 */
	private MetricValue readOperatorLatency() {
		MetricValue value = null;

		Timer operatorLatency = metricRegistry
				.timer(MetricName.OPERATOR_LATENCY.getName());

		if (operatorLatency != null) {
			value = MetricValue.millis(Double.valueOf(
					operatorLatency.getSnapshot().getMean() / 1000000).intValue());
		}

		return value;
	}

	/**
	 * Obtain operator queue length and return as MetricValue (tuples)
	 */
	private MetricValue readQueueLength() {
		MetricValue value = null;

		Counter queueLength = metricRegistry
				.counter(MetricName.QUEUE_LENGTH.getName());

		if (queueLength != null) {
			value = MetricValue.tuples(Long.valueOf(
					queueLength.getCount()).intValue());
		}

		return value;
	}
}
