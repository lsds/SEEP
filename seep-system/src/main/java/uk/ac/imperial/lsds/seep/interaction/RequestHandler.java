/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Constantinos Vryonides - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.interaction;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import uk.ac.imperial.lsds.seep.infrastructure.master.MasterController;
import uk.ac.imperial.lsds.seep.api.QueryPlan;


public class RequestHandler extends AbstractHandler {

    final private MasterController mc = MasterController.getInstance();

	public RequestHandler() {
		super();
        mc.init();
	}

    // Handles http requests
	public void handle(String target, Request baseRequest, HttpServletRequest request, 
						HttpServletResponse response) throws IOException, ServletException {

        if (target.equals("/submitquery/")) {
            String queryPath = request.getParameter("path");
            String baseClass = request.getParameter("baseclass");

            if (queryPath == null || baseClass == null) {
                // No valid http request. Just return with error 400
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Bad request");
                return;
            }

            // Compose query
            QueryPlan qp = mc.executeComposeFromQuery(queryPath,baseClass);

            if (qp == null) {
                // Cannot create query. Return with error code 500
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Failed to create query");
                return;
            }

            // Submit query to the master
            mc.submitQuery(qp);

            // Success
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("Query submitted successfully");
        }
        else if (target.equals("/setoutputdir/")) {
            
            response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            response.getWriter().println("Not implemented");

            /*
            String path = request.getParameter("path");

            if (path == null) {
                // No valid http request. Just return with error 400
                return;
            }*/

        }
        else if (target.equals("/deploy/")) {
            boolean succ = mc.deployQueryToNodes();

            if (succ) {
                // Success
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("Query deployed successfully");
            } 
            else {
                // Failure
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Could not deploy query to nodes");
            }
        }
        else if (target.equals("/start/")) {
            boolean succ = mc.startSystem();
            if (succ) {
                // Success
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("Query started successfully");
            } 
            else {
                // Failure
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Could not deploy query to nodes");
            }
        }
        else if (target.equals("/exit/")) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("Exiting..");
            new Thread(new Runnable() {

                public void run() {
                    try {
                        Thread.sleep(1000);
                        System.exit(0); 
                    }
                    catch (Exception e) {
                        System.exit(0);
                    }

                }


            }).start();
        }
        else if (target.equals("/stop/")) {
            response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            response.getWriter().println("Not implemented");  
        } 
        else if (target.equals("/workersinfo")) {
            response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            response.getWriter().println("Not implemented");       
        } else {
            // No valid http request. Just return with error 404
            return;
        }

        baseRequest.setHandled(true);

        /*
        System.out.println("Incoming request");

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);
        System.out.println(request.getParameter("username"));
        System.out.println(request.getParameter("password"));

        response.getWriter().println("<h1>Hello</h1>");*/
    }

}