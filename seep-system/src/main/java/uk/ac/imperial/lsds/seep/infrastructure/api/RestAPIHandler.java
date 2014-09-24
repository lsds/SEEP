package uk.ac.imperial.lsds.seep.infrastructure.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.MultiMap;

import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RestAPIHandler extends AbstractHandler {
	
	public static final ObjectMapper mapper = new ObjectMapper();
	
	
	public static Map<String, String> getReqParameter(String query) {
		String[] params = query.split("&");  
	    Map<String, String> map = new HashMap<>();  
	    for (String param : params) {  
	        String name = param.split("=")[0];  
	        String value = param.split("=")[1];  
	        map.put(name, value);  
	    }  
	    return map;  
	}
	
	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		response.setContentType("application/json;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		
		if (baseRequest.getMethod().equals("GET")) {
			if (NodeManager.restAPIRegistry.containsKey(target)) {
				MultiMap<String> reqParameters = new MultiMap<>();
				baseRequest.getUri().decodeQueryTo(reqParameters);
				mapper.writeValue(response.getWriter(),NodeManager.restAPIRegistry.get(target).getAnswer(reqParameters));
			}
			else {
				// default case: answer with a list of available keys
				baseRequest.setHandled(true);
				mapper.writeValue(response.getWriter(),NodeManager.restAPIRegistry.keySet());
			}
		}
	}

}
