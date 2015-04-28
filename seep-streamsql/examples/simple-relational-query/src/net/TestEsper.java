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
		 * Set a listener; called when statement matches
		 */
		statement.addListener(new UpdateListener() {
			
			long count = 0L;
			long previous = 0L;
			long delta, STEP = 1000000L;
			long t, _t = 0L;
			double dt, rate;
			
			@Override
			public void update(EventBean [] newEvents, EventBean [] oldEvents) {
				
				if (newEvents != null) {
					
					for (EventBean e : newEvents) {
						
						// System.out.println(e);
						
						/*
						 * Question:
						 * 
						 * Can we get the tuple size here, e.g. by iterating over
						 * all attributes and check if they are instances of Long
						 * (+8 bytes), or Integer (+4 bytes) and so on?
						 * 
						 */
						
						count ++;
						
						if (count % STEP == 0) {
							
							/* System.out.println(String.format("[DBG] [Listener] count %10d _t %20d previous %20d", 
							 * count, _t, previous));
							 */
							t = System.nanoTime();
							if (_t > 0 && previous > 0) {
								dt = ((double) (t - _t)) / 1000000000.; /* In seconds */
								delta = (count - previous);
								rate = ((double) delta) / dt;
								System.out.println(String.format("%10d tuples %10.3f sec %10.3f tuples/s", delta, dt, rate));
							}
							_t = t;
							previous = count;
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
