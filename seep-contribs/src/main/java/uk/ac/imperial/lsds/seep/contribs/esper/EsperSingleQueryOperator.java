package uk.ac.imperial.lsds.seep.contribs.esper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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

	private static final long serialVersionUID = 1L;

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

	private String name = "";
	
	/*
	 * The query as a statement, built from the query string
	 */
	private EPStatement statement = null;
	
	private boolean enableLoggingOfMatches = true;	
	private List<DataTuple> matchCache;
	
	private Queue<DataTuple> initCache;
	private boolean initialised = false;
	
	public EsperSingleQueryOperator(String query, String url, String name) {
		this.esperQuery = query;
		this.esperEngineURL = url;
		this.name = name;
		if (enableLoggingOfMatches) {
			this.matchCache = Collections.synchronizedList(new ArrayList<DataTuple>());
		}
		this.initCache = new LinkedList<DataTuple>();
	}

	public EsperSingleQueryOperator(String query, String url, String streamKey, String name, String[] typeBinding) {
		this(query, url, name);
		this.typesPerStream.put(streamKey, getTypes(typeBinding));
	}

	public EsperSingleQueryOperator(String query, String url, String name, Map<String, String[]> typeBinding) {
		this(query, url, name);
		for (String stream : typeBinding.keySet())
			this.typesPerStream.put(stream, getTypes(typeBinding.get(stream)));
	}

	public void initStatement() {
		
		if (statement != null) {
			statement.removeAllListeners();
			statement.destroy();
		}
		
		log.debug("Creating ESPER query...");
		
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
		
		initialised = true;
		log.debug("Done with init: {}", this.esperQuery);
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
				log.debug("Registering data items as '{}' in esper queries...", stream);
				for (String key : currentTypes.keySet()) {
					Class<?> clazz = (Class<?>) currentTypes.get(key);
					log.debug("  * registering type '{}' for key '{}'",
							clazz.getName(), key);
				}
				configuration.addEventType(stream, currentTypes);
				log.debug("{} attributes registered.", currentTypes.size());
			}
		}
		
		/*
		 * Get the ESPER engine instance
		 */
		epService = EPServiceProviderManager.getProvider(esperEngineURL,
				configuration);

		/*
		 * Initialise the query statement
		 */
		initStatement();
		
		/*
		 * Register rest API handler
		 */
		NodeManager.restAPIRegistry.put("/query", new RestAPIEsperGetQueryDesc(this));
		NodeManager.restAPIRegistry.put("/matches", new RestAPIEsperGetMatches(this));
		NodeManager.restAPIRegistry.put("/query_update", new RestAPIEsperPostQueryUpdate(this));
		
	}
	
	protected void sendOutput(EventBean out) {
		log.debug("Query returned a new result event: {}", out);
		
		DataTuple output = new DataTuple(api.getDataMapper(), new TuplePayload());
		List<Object> objects = new ArrayList<>();
		
		for (String key : out.getEventType().getPropertyNames()) {
			Object value = out.get(key);
			if (value == null)
				continue;
			objects.add(value);
		}
		DataTuple outTuple = output.setValues(objects.toArray());
		outTuple.getPayload().timestamp = System.currentTimeMillis();
		
		log.debug("At {}, sending output {}", outTuple.getPayload().timestamp, outTuple.getPayload().attrValues);
		
		if (this.enableLoggingOfMatches) {
			long cutOffTime = System.currentTimeMillis() - 1000*60*20;
			synchronized (matchCache) {
				
				matchCache.add(outTuple);
				
				// Remove old items
				Iterator<DataTuple> iter = matchCache.iterator();
				boolean run = true;
				while (iter.hasNext() && run) {
					DataTuple t = iter.next();
					if (t.getPayload().timestamp < cutOffTime) {
						iter.remove();
					}
					else {
						run = false;
					}
				}
			}
		}
		
		log.debug("Match cache size: {}", this.matchCache.size());
		
		api.send(outTuple);
	}

	public void sendData(DataTuple input) {
		String stream = input.getString(STREAM_IDENTIFIER);
		
		Map<String, Object> item = new LinkedHashMap<String, Object>();
		
		// only previously defined types are available to esper.
		for (String key : this.typesPerStream.get(stream).keySet()) 
			item.put(key, input.getValue(key));
		
		log.debug("Sending item {} with name '{}' to esper engine", item,
				stream);
		
		epService.getEPRuntime().sendEvent(item, stream);		
	}

	@Override
	public void processData(DataTuple input) {

		log.debug("Received input tuple {}", input.toString());
		log.debug("Map of received input tuple {}", input.getMap().toString());
		
		if (!initialised) {
			this.initCache.add(input);
		}
		else {
			while (!this.initCache.isEmpty()) {
				sendData(this.initCache.poll());
			}
			sendData(input);
		}
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

	public void initWithNewEsperQuery(String query) {
		log.debug("init with new esper query: {}", query);
		initialised = false;
		this.esperQuery = query;
		NodeManager.restAPIRegistry.put("/query", new RestAPIEsperGetQueryDesc(this));
		initStatement();
	}

	public String getName() {
		return name;
	}

	public boolean isEnableLoggingOfMatches() {
		return enableLoggingOfMatches;
	}

	public List<DataTuple> getMatchCache() {
		return this.matchCache;
	}
	
}