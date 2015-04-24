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
			@Override
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
				if (newEvents != null) {
					for (EventBean e : newEvents) 
						System.out.println(e);
				}
			}
		});
	}
	
	public void sendEvent(Map<String, Object> item, String key) {
		esperService.getEPRuntime().sendEvent(item, key);		
	}

	
	
}
