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

import org.eclipse.jetty.server.Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {

	final private static int PORT = 8686;
	final private static Logger LOG = LoggerFactory.getLogger(Interactive.class);
	final private static Server SERVER = new Server(PORT);

	public WebServer() {

	}

	//Initialises the webserver and waits for incoming requests;
	public void initWebServer() {
		LOG.info("Starting webserver.");
		try {
			SERVER.setHandler(new RequestHandler());
			SERVER.start();
			LOG.info("Webserver started successfully");
			SERVER.join();
		} catch (Exception e) {
			LOG.warn("Webserver failed to start");
		}
	}
}