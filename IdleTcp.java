/**
 * IdleTcp class
 * CPSC 441
 *
 * To open multiple TCP connections
 * to a given server at a given port.
 * Can be used to check a server's 
 * multi-threading functionality
 *
 *
 * @author: Majid Ghaderi
 */

import java.io.*;
import java.net.*;
import java.util.*;


public class IdleTcp {

	public static void main(String[] args) {
		// parse command line args
		parseCommandLine(args);
		
        // parse command line args
        HashMap<String, String> params = parseCommandLine(args);
        
        // set parameters
        int serverPort = Integer.parseInt( params.getOrDefault("-p", "2025") ); // server port number > 1024
        String serverName = params.getOrDefault("-s", "localhost"); // server name
        int numConns = Integer.parseInt( params.getOrDefault("-c", "10") ); // number of TCP connections
		
		try {
			// array of client sockets
            Socket[] conns = new Socket[numConns];
            
			// open all sockets
			System.out.println("opening the connections...");
            for (int i = 0; i < numConns; i++) {
                conns[i] = new Socket(serverName, serverPort);
				System.out.printf("socket[%d] status: %s on port %d\n", i, conns[i].isConnected() ? "connected" : "not connected", conns[i].getLocalPort());
			}
            
			System.out.printf("%d connections to %s:%d\n", numConns, serverName, serverPort);
			System.out.println("type \"quit\" to stop");
			System.out.println("......................");

			// wait for quit command
			waitForQuit();
            
			// close the sockets
            for (int i = 0; i < numConns; i++) {
				System.out.printf("socket[%d] status: %s\n", i, conns[i].isOutputShutdown() ? "connected" : "not connected");
				conns[i].close();
			}
            
			System.out.println("connections terminated");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

	// helper method to parse commandline arguments
	private static HashMap<String, String> parseCommandLine(String[] args) {
		HashMap<String, String> params = new HashMap<String, String>();

		int i = 0;
		while ((i + 1) < args.length) {
			params.put(args[i], args[i+1]);
			i += 2;
		}
		return params;
	}
	

    // helper method to wait until user types "quit"
    private static void waitForQuit() {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        
        try {
            while (!console.readLine().equals("quit"));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
}
