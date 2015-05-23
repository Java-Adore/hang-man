package ver1_5;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * v5.0
 * 
 * This is the fifth version of hangman in a command line like application.
 * This one uses a Hangman Server and a Hangman Client
 * This version use multi-threading
 */
public class HangmanServer {
	
	private ServerSocket serverSocket;
	private Socket client;
	int numConnections = 0;

	public static void main(String[] args) throws Exception {

		int port = 9999;

		HangmanServer server = new HangmanServer();
		try {
			server.start(port);
			System.out.println("1");

		} catch (FileNotFoundException e) {
			System.out.println("The file is not found, shutting down...");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	
	/**
	 * Starts the server on the specified port
	 * 
	 * @param port
	 * @throws IOException
	 */
	public void start(int port) throws IOException {
		// serverSocket = new ServerSocket(port);
		// serverSocket.setSoTimeout(1000000);
		// Try to open a server socket on the given port
		// Note that we can't choose a port less than 1024 if we are not
		// privileged users (root)

		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(1000000);
		} catch (IOException e) {
			System.out.println(e);
		}

		System.out.println("Server is started and is waiting for connections.");
		System.out
				.println("With multi-threading, multiple connections are allowed.");
		System.out.println("Any client can send -1 to stop the server.");

		// Whenever a connection is received, start a new thread to process the
		// connection
		// and wait for the next connection.

		while (true) {
			try {
				client = serverSocket.accept();
				numConnections++;
				Server2Connection oneconnection = new Server2Connection(client,
						numConnections, this);
				new Thread(oneconnection).start();
			} catch (IOException e) {
				System.out.println(e);
			}
		}

	}

	/**
	 * Wait for a connection to come up
	 * 
	 * @throws IOException
	 */
//	public void waitOnConnection() throws IOException {
//
//		client = serverSocket.accept();
//		outputToClient = new PrintWriter(client.getOutputStream(), true);
//		inputFromClient = new BufferedReader(new InputStreamReader(
//				client.getInputStream()));
//	}

	/**
	 * Shows every aspect of the Hangman game
	 */
	

	
	public void stopServer() {
		System.out.println("Server cleaning up.");
		System.exit(0);
	}
}

class Server2Connection implements Runnable {
	
	private boolean[] usedLetters = new boolean[26];
	private int possibleFailedGuesses = 10;
	private int failedGuesses = 0;
	private String guessMe;
	String fileLocation = "C:\\dictionary.txt";
	private ArrayList wordsToChooseFrom = new ArrayList();
	
	BufferedReader inputFromClient;
	PrintWriter outputToClient;
	Socket clientSocket;
	int id;
	HangmanServer server;

	public Server2Connection(Socket clientSocket, int id, HangmanServer server) {
		this.clientSocket = clientSocket;
		this.id = id;
		this.server = server;
		System.out.println("Connection " + id + " established with: "
				+ clientSocket);
		try {
			inputFromClient = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			outputToClient = new PrintWriter(clientSocket.getOutputStream(),
					true);
			chooseWord(fileLocation);
//			showGreetings();
//			showHangman();
//			askAndValidateInput();
//			

		}catch (IOException e) {
			System.out.println(e);
		} 
	}

	public void run() {
		try {
			showGreetings();
			showHangman();
			askAndValidateInput();
			
		} catch (IOException e) {
			System.out.println(e);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}
	
	/**
	 * Shows the client some greetings
	 */
	public void showGreetings() {
		outputToClient.println("Welcome!");
	}
	
	public void showHangman() {
		// Show the current score
		showScore();
		// Show the word
		showShownWord(getShownWord());
		drawHangman();
	}
	
	/**
	 * Returns the word that is shown to the user, hiding the characters not yet
	 * discovered with underscores.
	 * 
	 * @return
	 */
	public String getShownWord() {
		StringBuilder wordToGuess = new StringBuilder();
		// numberOfSuccessGessedLetters = 0;
		// Loop through the word that has to be guessed
		for (int i = 0; i < guessMe.length(); i++) {
			// Get the current character in the loop
			char currentChar = guessMe.charAt(i);

			// Check if the current character is already guessed
			if (usedLetters[((int) currentChar - 65)]) {
				// If it is guessed already, show it
				wordToGuess.append(guessMe.charAt(i));
			} else {
				// If it is not guessed already, show an underscore
				wordToGuess.append("_");
			}
		}

		return wordToGuess.toString();
	}

	/**
	 * Shows the current score
	 */
	public void showScore() {
		outputToClient.println("[Score] Tries: " + failedGuesses + "/"
				+ possibleFailedGuesses);
	}

	public void drawHangman() {
		float numberOfSuccessGessedLetters = 0;
		for (int i = 0; i < guessMe.length(); i++) {
			// Get the current character in the loop
			char currentChar = guessMe.charAt(i);

			// Check if the current character is already guessed
			if (usedLetters[((int) currentChar - 65)]) {
				// to know the number of correct guest letters
				numberOfSuccessGessedLetters += 1;
			}
		}

		outputToClient.println("[Paint] "
				+ ((numberOfSuccessGessedLetters / guessMe.length()) * 100));
		System.out
				.println((numberOfSuccessGessedLetters / guessMe.length()) * 100);
	}

	/**
	 * Shows the shown word up until now
	 * 
	 * @param shownWord
	 */
	public void showShownWord(String shownWord) {
		outputToClient.println("[ShownWord] " + shownWord);
	}

	/**
	 * Returns the letters that are already used
	 * 
	 * @return String
	 */
	public String getAlreadyUsedLetters() {
		StringBuilder alreadyUsedLetters = new StringBuilder();
		for (int i = 0; i < usedLetters.length; i++) {
			if (usedLetters[i]) {
				alreadyUsedLetters.append((char) (i + 65));
			} else {
				alreadyUsedLetters.append(".");
			}
		}
		return alreadyUsedLetters.toString();
	}

	/**
	 * Asks for the user to guess a character
	 * 
	 * @throws IOException
	 */
	public void askAndValidateInput() throws IOException, InterruptedException {

		// Signal the client to send a letter
		// outputToClient.println("[AskLetter]");

		String inputString;
		while ((inputString = inputFromClient.readLine()) != null) {
			inputString = inputString.toUpperCase();
			System.out.println("Client guessed: " + inputString);

			// Get the character the user has put in
			char character = inputString.charAt(0);

			// Check if the character is in the word by using the
			// String.contains and Character.toString functions
			if (guessMe.contains(Character.toString(character))) {
				// The reason we use cast the character to int is
				// to get the ASCII value which is always between
				// 65 (A) and 90 (Z). This way we can fill the array
				// by using this value minus 65 as the array indices
				usedLetters[((int) character - 65)] = true;
				outputToClient
						.println("[Message] The character is in the guessed word, good job!.");
				if (gameHasEnded()) {
					showHangman();
					wonGame();
				} else {
					showHangman();
					askAndValidateInput();
				}

			} else {
				usedLetters[((int) character - 65)] = true;
				outputToClient
						.println("[Message] The character is not in the guessed word, try again.");
				failedGuesses++;
				if (gameHasEnded()) {
					showHangman();
					lostGame();
				} else {
					showHangman();
					askAndValidateInput();
				}
			}
		}

	}

	/**
	 * Check if the game has ended
	 * 
	 * @return
	 */
	public boolean gameHasEnded() {
		// Check if the failed guesses equal the possible amount you may guess
		if (failedGuesses >= possibleFailedGuesses) {
			return true;
		}

		// Check if the shown word equals the word to be guessed
		if (guessMe.equals(getShownWord())) {
			return true;
		}

		return false;
	}
	
	/**
	 * Chooses the random word from the file specified
	 * 
	 * @param fileLocation
	 * @throws FileNotFoundException
	 */
	public void chooseWord(String fileLocation) throws FileNotFoundException {
		Scanner fileScanner = new Scanner(new FileReader(fileLocation));
		while (fileScanner.hasNext()) {
			wordsToChooseFrom.add(fileScanner.next().toUpperCase());
		}
		fileScanner.close();

		if (wordsToChooseFrom.isEmpty()) {
			System.out.println("The file is empty, shutting down...");
			System.exit(0);
		}

		Random random = new Random();
		int getRandomIndex = random.nextInt(wordsToChooseFrom.size());
		guessMe = (String) wordsToChooseFrom.get(getRandomIndex);
	}


	/**
	 * Show the won game text and the score
	 */
	public void wonGame() {
		outputToClient
				.println("[Ended] You've guessed the word! Good job mate.");
	}

	/**
	 * Show the lost game text
	 */
	public void lostGame() {
		outputToClient
				.println("[Ended] You've not guessed the word, loser! The word was "
						+ guessMe + "!");
	}

}
