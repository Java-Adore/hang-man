package ver1_2;


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
 * v2.0
 * 
 * This is the second version of hangman in a command line like application.
 * This one uses a Hangman Server and a Hangman Client  
 */
public class HangmanServer {
	private PrintWriter outputToClient;
	private BufferedReader inputFromClient;
	private ArrayList wordsToChooseFrom = new ArrayList();
	
	private boolean[] usedLetters = new boolean[26];
	private int possibleFailedGuesses = 10;
	private int failedGuesses = 0;
	private String guessMe;
	private ServerSocket serverSocket;
	private Socket client;
	
	public static void main(String[] args) throws Exception {
		int port = 9978;
		String fileLocation = "C:\\Users\\abdallah\\Documents\\Abdalla Ali\\University\\Year 2\\Sem2\\programming\\Assignment\\dictionary.txt";

		HangmanServer server = new HangmanServer();
		try {
			server.chooseWord(fileLocation);
			server.start(port);
			server.waitOnConnection();
			server.showGreetings();
			server.showHangman();
			server.askAndValidateInput();
		} catch(FileNotFoundException e) {
			System.out.println("The file is not found, shutting down...");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Shows the client some greetings
	 */
	public void showGreetings() {
		outputToClient.println("Welcome!");
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
		
		if( wordsToChooseFrom.isEmpty() ) {
			System.out.println("The file is empty, shutting down...");
			System.exit(0);
		}
		
		Random random = new Random(); 
		int getRandomIndex = random.nextInt(wordsToChooseFrom.size());
		guessMe = (String) wordsToChooseFrom.get(getRandomIndex);
	}
	
	/**
	 * Starts the server on the specified port
	 * 
	 * @param port
	 * @throws IOException
	 */
	public void start(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(1000000);
	}
	
	/**
	 * Wait for a connection to come up
	 * 
	 * @throws IOException
	 */
	public void waitOnConnection() throws IOException {
		client = serverSocket.accept();
		outputToClient = new PrintWriter(client.getOutputStream(), true);
		inputFromClient = new BufferedReader(new InputStreamReader(
				client.getInputStream()));
	}
	
	/**
	 * Shows every aspect of the Hangman game
	 */
	public void showHangman() {
		// Show the current score
		showScore();
		
		// Show the word
		showShownWord(getShownWord());

		// Show the already used letters 
		showAlreadyUsedLetters(getAlreadyUsedLetters());
	}
	
	/**
	 * Returns the word that is shown to the user, 
	 * hiding the characters not yet discovered with underscores.
	 * 
	 * @return
	 */
	public String getShownWord() {
		StringBuilder wordToGuess = new StringBuilder();
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
		outputToClient.println("Tries: " + failedGuesses + "/" + possibleFailedGuesses);
	}
	
	/**
	 * Shows the shown word up until now
	 * 
	 * @param shownWord
	 */
	public void showShownWord(String shownWord) {
		outputToClient.println(shownWord);
	}
	
	/**
	 * Shows the used letters 
	 * 
	 * @param alreadyUsedLetters
	 */
	public void showAlreadyUsedLetters(String alreadyUsedLetters) {
		outputToClient.println(alreadyUsedLetters);
	}
	
	/**
	 * Returns the letters that are already used
	 * 
	 * @return String
	 */
	public String getAlreadyUsedLetters() {
		StringBuilder alreadyUsedLetters = new StringBuilder();
		for (int i = 0; i < usedLetters.length; i++) {
			if(usedLetters[i]) {
				alreadyUsedLetters.append((char) (i+65));
			} else {
				alreadyUsedLetters.append(".");
			}
		}
		return alreadyUsedLetters.toString();
	}
	
	/**
	 * Asks for the user to guess a character
	 * @throws IOException 
	 */
	public void askAndValidateInput() throws IOException, InterruptedException {
		// Asks the user to guess
		outputToClient.println("What character do you think is in the word: ");

		String inputString;		
		while ((inputString = inputFromClient.readLine()) != null) {
			inputString = inputString.toUpperCase();
			System.out.println("Client guessed: " + inputString);

			// Validate the input
			if (validateInput(inputString)) {
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
					outputToClient.println("The character is in the guessed word, good job!.");
					if (gameHasEnded()) {
						wonGame();
					} else {
						showHangman();
						askAndValidateInput();
					}
				} else {
					usedLetters[((int) character - 65)] = true;
					outputToClient.println("The character is not in the guessed word, try again.");
					failedGuesses++;
					if (gameHasEnded()) {
						lostGame();
					} else {
						showHangman();
						askAndValidateInput();
					}
				}
			} else {
				askAndValidateInput();
				break;
			}
		}
		
	}
	
	/**
	 * Validates the input from the user 
	 * 
	 * By casting a char to an int, you get the ASCII value.
	 * The ASCII value for A-Z are 65-90, which can be used 
	 * to map characters used and to validate input. 
	 * 
	 * @param inputString
	 * @return boolean
	 */
	public boolean validateInput(String inputString) {
		// The input has to be 1 character long
		if (inputString.length() != 1) {
			outputToClient.println("You did not type 1 character, please try again.");
			return false;
		}

		char character = inputString.charAt(0);
		// The character must be a letter between ASCII code 65 (A) and 90 (Z)
		if ((int) character < 65) {
			outputToClient.println("The character you put in is not between A and Z.");
			return false;
		}
		if ((int) character > 90) {
			outputToClient.println("The character you put in is not between A and Z.");
			return false;
		}

		// Check if the character is already used
		if (usedLetters[((int) character - 65)]) {
			outputToClient.println("You have already guessed this character, these are the one you already tried: ");
			showAlreadyUsedLetters(getAlreadyUsedLetters());
			return false;
		}
		return true;
	}
	
	/**
	 * Check if the game has ended
	 * 
	 * @return
	 */
	public boolean gameHasEnded() {
		// Check if the failed guesses equal the possible amount you may guess
		if( failedGuesses >= possibleFailedGuesses ) {
			return true;
		}

		// Check if the shown word equals the word to be guessed
		if( guessMe.equals(getShownWord()) ) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Show the won game text and the score
	 */
	public void wonGame() {
		outputToClient.println("You've guessed the word! Good job mate.");
		outputToClient.println("It took you " + failedGuesses + " from the " + possibleFailedGuesses + " possible guesses!");
	}
	
	/**
	 * Show the lost game text
	 */
	public void lostGame() {
		outputToClient.println("You've not guessed the word, loser! The word was " + guessMe + "!");
		outputToClient.println("Better luck next time!");
	}
}
