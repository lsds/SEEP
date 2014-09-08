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

public class RequestHandler extends AbstractHandler {

	public RequestHandler() {
		super();
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, 
						HttpServletResponse response) throws IOException, ServletException {


        if (target.equals("/submitquery/")) {
            String queryPath = request.getParameter("path");
            String baseClass = request.getParameter("baseclass");

            if (queryPath == null) {
                // No valid http request. Just return with error 404
                return;
            }

        }
        else if (target.equals("/setoutputdir/")) {
            String path = request.getParameter("path");

            if (path == null) {
                // No valid http request. Just return with error 404
                return;
            }

        }
        else if (target.equals("/deploy/")) {

        }
        else if (target.equals("/start/")) {

        }
        else if (target.equals("/exit/")) {

        }
        else if (target.equals("/stop/")) {

        } 
        else if (target.equals("/workersinfo")) {
            
        } else {
            // No valid http request. Just return with error 404
            return;
        }


        System.out.println("Incoming request");

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);
        System.out.println(request.getParameter("username"));
        System.out.println(request.getParameter("password"));

        response.getWriter().println("<h1>Hello</h1>");
    }

}