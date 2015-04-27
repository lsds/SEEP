package net;

import java.util.LinkedHashMap;
import java.util.Map;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class TestEsper {

	private Map<String, Object> esperTypeBinding = new LinkedHashMap<String, Object>();

	private String esperEngineURL = "";

	private EPServiceProvider esperService = null;
	
	private Configuration configuration = new Configuration();

	public void addEventType(String eventType, Map<String, Object> bindingForEventType) {
		this.esperTypeBinding.putAll(bindingForEventType);
		configuration.addEventType(eventType, bindingForEventType);
	}


	public void initEngine() {
		// do not use internal timer
		configuration.getEngineDefaults().getThreading().setInternalTimerEnabled(false);
	}

	public void setupEngine() {
		esperService = EPServiceProviderManager.getProvider(esperEngineURL, configuration);
	}
		
	public void addQuery(String query) {
		
		/*
		 * Build the ESPER statement
		 */
		EPStatement statement = esperService.getEPAdministrator().createEPL(query);

		/*
		 * Set a listener called when statement matches
		 */
		statement.addListener(new UpdateListener() {
			long count = 0L;
			long previous = 0L; 
			long t, _t = 0L;
			double dt, rate, MB;
			double _1MB = 1048576.0;
			long Bytes = 0;
			
			@Override
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
				if (newEvents != null) {
					for (EventBean e : newEvents) { 
						// System.out.println(e);
						count ++;
						//System.out.println(count);
						Bytes += 64;
						if (count % 1000 == 0) {
							t = System.currentTimeMillis();
							if (_t > 0 && previous > 0) {
								dt = ((double) (t - _t)) / 1000.;
								MB = ((double) (Bytes - previous)) / _1MB;
								rate = MB / dt;
								System.out.println(String.format("%10.3f MB %10.3f sec %10.3f MB/s %10.3f Gbps", 
								MB, dt, rate, ((rate * 8.)/1000.)));
							}
							_t = t;
							previous = Bytes;
						}
					}
				}
			}
		});
	}
	
	public void sendEvent(Map<String, Object> item, String key) {
		esperService.getEPRuntime().sendEvent(item, key);		
	}

	
	
}
