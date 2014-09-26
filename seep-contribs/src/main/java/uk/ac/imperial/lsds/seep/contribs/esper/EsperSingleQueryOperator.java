package uk.ac.imperial.lsds.seep.contribs.esper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class EsperSingleQueryOperator implements StatelessOperator {

	private final static Logger log = LoggerFactory.getLogger(EsperSingleQueryOperator.class);

	public final static String STREAM_IDENTIFIER = "stream";
	
	// This map contains a list of key,class mappings, which will be
	// registered to the esper engine
	private Map<String,Map<String, Object>> typesPerStream = new LinkedHashMap<String, Map<String,Object>>();

	/*
	 * URL of the ESPER engine instance
	 */
	private String esperEngineURL = "";

	/*
	 * The ESPER engine instance used by this processor, will be fetched based
	 * on the given URL
	 */
	private EPServiceProvider epService = null;

	/*
	 * The actual ESPER query as String
	 */
	private String esperQuery = "";

	/*
	 * The query as a statement, built from the query string
	 */
	private EPStatement statement = null;
	
	private boolean enableLoggingOfMatches = true;	
	private List<DataTuple> matchCache;
	

	public EsperSingleQueryOperator(String query, String url) {
		this.esperQuery = query;
		this.esperEngineURL = url;
		if (enableLoggingOfMatches) {
			this.matchCache = Collections.synchronizedList(new ArrayList<DataTuple>());
		}
	}

	public EsperSingleQueryOperator(String query, String url, String streamKey, String[] typeBinding) {
		this(query, url);
		this.typesPerStream.put(streamKey, getTypes(typeBinding));
	}

	public EsperSingleQueryOperator(String query, String url, Map<String, String[]> typeBinding) {
		this(query, url);
		for (String stream : typeBinding.keySet())
			this.typesPerStream.put(stream, getTypes(typeBinding.get(stream)));
	}


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
		if (this.typesPerStream != null) {
			for (String stream : this.typesPerStream.keySet()) {
				Map<String, Object> currentTypes = this.typesPerStream.get(stream);
				log.info("Registering data items as '{}' in esper queries...", stream);
				for (String key : currentTypes.keySet()) {
					Class<?> clazz = (Class<?>) currentTypes.get(key);
					log.info("  * registering type '{}' for key '{}'",
							clazz.getName(), key);
				}
				configuration.addEventType(stream, currentTypes);
				log.info("{} attributes registered.", currentTypes.size());
			}
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
		
		/*
		 * Register monitoring handlers
		 */
		NodeManager.restAPIRegistry.put("/node/matches", new RestAPIEsperGetMatches(this));
		NodeManager.restAPIRegistry.put("/node/query", new RestAPIEsperGetQueryDesc(this));
	}
	
	protected void sendOutput(EventBean out) {
		
		log.info("Query returned a new result event: {}", out);

		DataTuple output = new DataTuple(api.getDataMapper(), new TuplePayload());
		List<Object> objects = new ArrayList<>();
		
		for (String key : out.getEventType().getPropertyNames()) {
			Object value = out.get(key);
			if (value == null)
				continue;
			objects.add(value);
		}
		DataTuple t = output.setValues(objects.toArray());
		
		log.info("Sending output {}", t.getPayload().attrValues);
		
		if (this.enableLoggingOfMatches)
			matchCache.add(t);
		
		api.send(t);
	}

	@Override
	public void processData(DataTuple input) {

		log.info("Received input tuple {}", input.toString());
		log.info("Map of received input tuple {}", input.getMap().toString());
		
		String stream = input.getString(STREAM_IDENTIFIER);
		
		Map<String, Object> item = new LinkedHashMap<String, Object>();
		
		// only previously defined types are available to esper.
		for (String key : this.typesPerStream.get(stream).keySet()) 
			item.put(key, input.getValue(key));
		
		log.info("Sending item {} with name '{}' to esper engine", item,
				stream);

		epService.getEPRuntime().sendEvent(item, stream);		
	}

	@Override
	public void processData(List<DataTuple> dataList) {
		for (DataTuple tuple : dataList)
			processData(tuple);
	}

	public Map<String, Object> getTypes(String[] types) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (String def : types) {
			int idx = def.indexOf(":");
			if (idx > 0) {
				String key = def.substring(0, idx);
				String type = def.substring(idx + 1);

				Class<?> clazz = classForName(type);
				if (clazz != null) {
					log.debug("Defining type class '{}' for key '{}'", key,
							clazz);
					result.put(key, clazz);
				} else {
					log.error("Failed to locate class for type '{}'!", type);
				}
			}
		}
		return result;
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

	public Map<String, Map<String, Object>> getTypesPerStream() {
		return typesPerStream;
	}

	public String getEsperEngineURL() {
		return esperEngineURL;
	}

	public String getEsperQuery() {
		return esperQuery;
	}

	public boolean isEnableLoggingOfMatches() {
		return enableLoggingOfMatches;
	}

	public List<DataTuple> getMatchCache() {
		return this.matchCache;
	}
	
}