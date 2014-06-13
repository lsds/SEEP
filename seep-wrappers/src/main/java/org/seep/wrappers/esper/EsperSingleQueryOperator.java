package org.seep.wrappers.esper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.StatefulOperator;
import uk.ac.imperial.lsds.seep.state.State;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;


public class EsperSingleQueryOperator implements StatefulOperator {

	final static Logger log = LoggerFactory.getLogger(EsperSingleQueryOperator.class);

	// This map contains a list of key,class mappings, which will be
	// registered to the esper engine
	final Map<String, Object> types = new LinkedHashMap<String, Object>();

	API currentAPI = null;

	/*
	 * URL of the ESPER engine instance
	 */
	String esperEngineURL = "";

	/*
	 * The ESPER engine instance used by this processor, will be fetched based
	 * on the given URL
	 */
	EPServiceProvider epService = null;

	/*
	 * The actual ESPER query as String
	 */
	String esperQuery = "";

	/*
	 * The query as a statement, built from the query string
	 */
	EPStatement statement = null;

	protected String defaultKey = "Data";

	@Override
	public void setUp() {
		/*
		 * Init data structures
		 */
		Configuration configuration = new Configuration();
		configuration.getEngineDefaults().getThreading()
				.setInternalTimerEnabled(false);

		// The data types for the data items
		//
		if (types != null) {
			log.info("Registering data items as '{}' in esper queries...", defaultKey);
			for (String key : types.keySet()) {
				Class<?> clazz = (Class<?>) types.get(key);
				log.info("  * registering type '{}' for key '{}'",
						clazz.getName(), key);
			}
			configuration.addEventType(defaultKey, types);
			log.info("{} attributes registered.", types.size());
		}
		
		/*
		 * Get the ESPER engine instance
		 */
		epService = EPServiceProviderManager.getProvider(esperEngineURL,
				configuration);

		log.info("Creating ESPER query...");
		
		/*
		 * Build the ESPER statement
		 */
		statement = epService.getEPAdministrator().createEPL(this.esperQuery);

		/*
		 * Set a listener called when statement matches
		 */
		statement.addListener(new UpdateListener() {
			@Override
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
				if (newEvents == null) {
					// we don't care about events leaving the window (old
					// events)
					return;
				}
				for (EventBean theEvent : newEvents) {
					sendOutput(theEvent);
				}
			}
		});
	}
	
	protected void sendOutput(EventBean out) {
		if (this.currentAPI == null) {
			log.debug("No api available, output will be discarded.");
			return;
		}
		
		log.debug("Query returned a new result event: {}", out);

		DataTuple output = DataTuple.getNoopDataTuple();
		
		List<Object> objects = new ArrayList<>();
		
		for (String key : out.getEventType().getPropertyNames()) {
			Object value = out.get(key);
			if (value == null)
				continue;
			objects.add(value.toString());
		}
		
		output.setValues(objects);

		log.debug("Sending output...");
		this.currentAPI.send(output);
	}


	@Override
	public void processData(DataTuple input, API api) {

		// store current api so that we can send result items later
		this.currentAPI = api;

		Map<String, Object> item = new LinkedHashMap<String, Object>();

		// only previously defined types are available to esper.
		//
		for (String key : types.keySet()) 
			item.put(key, input.getValue(key));
		
		String key = this.defaultKey;
		if (item.containsKey("@source"))
			if (item.get("@source")!= null)
				key = item.get("@source").toString();
		
		log.debug("Sending item {} with name '{}' to esper engine", item,
				key);
		epService.getEPRuntime().sendEvent(item, key);		
	}

	@Override
	public void processData(List<DataTuple> dataList, API api) {
		for (DataTuple tuple : dataList)
			processData(tuple, api);
	}

	@Override
	public void setState(State state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public String getTypes() {
		StringBuffer s = new StringBuffer();
		Iterator<String> it = types.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			s.append(key);
			s.append(":");
			Class<?> clazz = (Class<?>) types.get(key);
			s.append(clazz.getName());
			if (it.hasNext()) {
				s.append(",");
			}
		}
		return s.toString();
	}

	public void setTypes(String[] types) {
		this.types.clear();

		for (String def : types) {
			int idx = def.indexOf(":");
			if (idx > 0) {
				String key = def.substring(0, idx);
				String type = def.substring(idx + 1);

				Class<?> clazz = classForName(type);
				if (clazz != null) {
					log.debug("Defining type class '{}' for key '{}'", key,
							clazz);
					this.types.put(key, clazz);
				} else {
					log.error("Failed to locate class for type '{}'!", type);
				}
			}
		}
	}

	protected static Class<?> classForName(String name) {
		//
		// the default packages to look for classes...
		//
		String[] pkgs = new String[] { "", "java.lang" };

		for (String pkg : pkgs) {
			String className = name;
			if (!pkg.isEmpty())
				className = pkg + "." + name;

			try {
				Class<?> clazz = Class.forName(className);
				if (clazz != null)
					return clazz;
			} catch (Exception e) {
			}
		}

		return null;
	}

}