

/**
 * WebServer Class
 * 
 * Implements a multi-threaded web server
 * supporting non-persistent connections.
 * 
 * @author 	Majid Ghaderi
 * @version	2021
 *
 */

import java.util.logging.*;


public class WebServer extends Thread {
	
	// global logger object, configures in the driver class
	private static final Logger logger = Logger.getLogger("WebServer");
	
	
    /**
     * Constructor to initialize the web server
     * 
     * @param port 	The server port at which the web server listens > 1024
     * 
     */
	public WebServer(int port);

	
    /**
	 * Main web server method.
	 * The web server remains in listening mode 
	 * and accepts connection requests from clients 
	 * until the shutdown method is called.
	 *
     */
	public void run();
	

    /**
     * Signals the web server to shutdown.
	 *
     */
	public void shutdown();
	
}
