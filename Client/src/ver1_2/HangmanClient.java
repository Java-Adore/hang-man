package ver1_2;

/**
 * v2.0
 * 
 * This is the second version of hangman in a command line like application.
 * This one uses server and clients  
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class HangmanClient {
	// This is used to get the user's input
	private Scanner userInput = new Scanner(System.in);
	private int port;
	private InetAddress serverName;
	private Socket connection;
	
	public static void main(String[] args) {
		

		int port =9978;
		
		
		
		try {
			InetAddress host = InetAddress.getLocalHost();
			System.out.println(host.toString());
			HangmanClient client = new HangmanClient(host, port);
			// trying to establish connection to the server
			client.connect();
			// waiting to read response from server
			client.readResponse();
		} catch (UnknownHostException e) {
			System.err.println("Host unknown. Cannot establish connection");
		} catch (IOException e) {
			System.err
					.println("Cannot establish connection. Server may not be up."
							+ e.getMessage());
		}
	}
	
	/**
	 * The HangmanServer is passed in to communicate
	 * 
	 * @param server
	 */
	public HangmanClient(InetAddress serverName, int port) {
		this.serverName = serverName;
		this.port = port;
	}
	
	/**
	 * Connects to the server
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void connect() throws UnknownHostException, IOException {
		System.out.println("Attempting to connect to " + serverName + ":"
				+ port);
		connection = new Socket(serverName, port);
		System.out.println("Connection Established");
	}

	/**
	 * Reads response from the server, if server asks what character, enables the input
	 * 
	 * @throws IOException
	 */
	public void readResponse() throws IOException {
		String responseFromServer;
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		BufferedWriter outputToServer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

		while ((responseFromServer = stdIn.readLine()) != null) {
			System.out.println(responseFromServer);
			if(responseFromServer.equals("What character do you think is in the word: ")) {
			    outputToServer.write(userInput.next());
			    outputToServer.newLine();
			    outputToServer.flush();
		    }
		}
	}
}
