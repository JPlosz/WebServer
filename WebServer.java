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

import java.io.*;
import java.net.*;
import java.util.logging.*;
import java.util.concurrent.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.nio.file.Files;

public class WebServer extends Thread {

	// global logger object, configures in the driver class
	private static final Logger logger = Logger.getLogger("WebServer");
	private ServerSocket serverSocket;
	private final ExecutorService pool;
	private boolean shuttingDown = false;

	/**
	 * Constructor to initialize the web server
	 *
	 * @param port 	The server port at which the web server listens > 1024
	 *
	 */
	public WebServer(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception e) {
			System.out.println("Server not initialized properly");
			serverSocket = null;
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		pool = Executors.newCachedThreadPool();
	}


	/**
	 * Main web server method.
	 * The web server remains in listening mode
	 * and accepts connection requests from clients
	 * until the shutdown method is called.
	 *
	 */
	public void run() {
			while (!shuttingDown) {
				try {
					//System.out.println("Waiting for connection");
					serverSocket.setSoTimeout(2000);
					pool.execute(new Worker(serverSocket.accept()));
				} catch (Exception e) {
				}
			}

			try {
				serverSocket.close();
			} catch (Exception e) {}
	}


	/**
	 * Signals the web server to shutdown.
	 *
	 */
	public void shutdown() {
		shuttingDown = true;
		try {
			serverSocket.close();
			pool.shutdown();    // stop accepting new threads
			pool.awaitTermination(5, TimeUnit.SECONDS); // wait max 5 seconds for threads to finish
			pool.shutdownNow(); // returns number of still running threads
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private class Worker implements Runnable {
		Socket socket;

		public Worker (Socket socket) {
			this.socket = socket;
		}

		public void run() {
			InetAddress addr = socket.getInetAddress();
			String ip = addr.getHostAddress();
			System.out.println(ip);


			FileInputStream fromFile = null;
			String filePath = "";

			try {
				System.out.println("Starting Thread");
				InputStream request = socket.getInputStream();

				boolean fileFound = false;
				boolean getFound = false;
				boolean httpFound = false;
				boolean hostFound = false;
				boolean header_complete = false;

				String line = "";
				int buffSize = 1024;
				byte[] buff = new byte[buffSize];
				int readBytes;

				// Begin reading the request
				while ((readBytes = request.read(buff)) != -1) {
					for (int i = 0; i < readBytes; i++) {
						char c = (char) buff[i];
						if (c != '\n') {
							line = line + c;    // add each char to a string until a newline is reached
						} else {
							// once a newline character is reached print the header line and check
							// for required information
							System.out.println(line);

							// request line must conatin "Get [filePath] [http version]"
							// try opening the file provided by the relative filepath after the GET keyword
							if (line.contains("GET")) {
								System.out.println("GET status found");
								getFound = true;

								String[] getRequest = line.split(" ");
								filePath = getRequest[1].substring(1);

								// check for valid http version
								httpFound = (getRequest[2].contains("HTTP/1.")) ? true : false;
								if (httpFound) System.out.println("HTTP version found");

								// check for an indicated host
							} else if (line.contains("Host")) {
								hostFound = true;
								System.out.println("Host name found");
							}

							// the header and body will be separated by an empty line containing only '\r\n'
							if (line.equals("\r")) {
								header_complete = true;
								System.out.println("Empty String. Header must be complete\n");
							}

							line = ""; // empty the string after being processed
						}
					}
				}

				String pattern = "EEE, dd MMM yyyy hh:mm:ss zzz";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				String date = simpleDateFormat.format(new Date());

				String serverName = "Missed-Connections";
				String lastModified = "";
				Long contentLength = 0L;
				String contentType = "";

				try {
					System.out.println("Opening the file: " + filePath);
					fromFile = new FileInputStream(filePath);
					File file = new File(filePath);
					lastModified = simpleDateFormat.format(new Date(file.lastModified()));
					contentLength = file.length();
					contentType = Files.probeContentType(file.toPath());
					fileFound = true;
				} catch (FileNotFoundException e) {
					//System.out.println("404 File Not Found");
					fileFound = false;
				}

				String resp = "";

				if (!getFound || !httpFound || !hostFound || !header_complete) {
					// send a 400 invalid request response
					System.out.println("400 Bad Request - Problem with the format/semantics of the request");
					// construct response
					resp = "HTTP/1.1 400 Bad Request\r\n";
					resp += "Date: " + date + "\r\n";
					resp += "Server: " + serverName + "\r\n";
					resp += "Connection: close\r\n";
					resp += "\r\n";
				} else if (!fileFound) {
					resp = "HTTP/1.1 404 Not Found\r\n";
					resp += "Date: " + date + "\r\n";
					resp += "Server: " + serverName + "\r\n";
					resp += "Connection: close\r\n";
					resp += "\r\n";
				} else {
					// send a 200 OK response along with the object requested
					System.out.println("200 OK response\n");
					resp = "HTTP/1.1 200 OK\r\n";
					resp += "Date: " + date + "\r\n";
					resp += "Server: " + serverName + "\r\n";
					resp += "Last-Modified: " + lastModified + "\r\n";
					resp += "Content-Length: " + contentLength + "\r\n";
					resp += "Content-Type: " + contentType + "\r\n";
					resp += "Connection: close\r\n";
					resp += "\r\n";
				}

				OutputStream response = socket.getOutputStream();
				// send the header response
				response.write(resp.getBytes("US-ASCII"));
				response.flush();

				System.out.print(resp);

				buffSize = 32 * 1024;
				buff = new byte[buffSize];

				if (fileFound) {
					// read from file and write to socket until EOF is reached
					while ((readBytes = fromFile.read(buff)) != -1) {
						System.out.println("W " + readBytes);
						response.write(buff, 0, readBytes);
						response.flush();
					}
					fromFile.close();
				}

				request.close();
				response.close();

			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
