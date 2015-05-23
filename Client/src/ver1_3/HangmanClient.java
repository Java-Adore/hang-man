package ver1_3;

/**
 * v3.0
 * 
 * This is the third version of hangman which will use Swing components.  
 */

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;

public class HangmanClient extends JFrame implements ActionListener {
	/**
	 * This is auto generated, it needs it for some reason
	 */
	private static final long serialVersionUID = -508381262137931628L;

	private int port;
	private String serverName;
	private Socket connection;

	private JLabel scoreLabel = new JLabel();
	private JLabel wonLostLabel = new JLabel();
	private JLabel wordToGuessLabel = new JLabel();
	private JButton letterButtons[];

	private JPanel mainPanel = new JPanel();
	private JPanel letterButtonsPanel = new JPanel();
	private JPanel hangmanPanel = new JPanel();
	private JPanel msgPanel = new JPanel();
	private JLabel msgLabel = new JLabel();

	private BufferedWriter outputToServer;
	private boolean usedLetters[] = new boolean[26];
	private final JPanel person = new JPanel();
	private final JLabel headLabel;
	private JLabel rightHandLabel;
	private JLabel leftHandLabel;
	private final JLabel bodyLabel;
	private final JLabel rightLegLabel;
	private final JLabel leftLegLabel;
	
	/**
	 * Main start function
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		String serverName = "localhost";
		//int port = Integer.parseInt(args[1]);
		int port =9999;
		
		
		try {
			//InetAddress host = InetAddress.getLocalHost();

			 HangmanClient client = new HangmanClient(serverName, port);
//			System.out.println("======================" + host.toString());
//			HangmanClient client = new HangmanClient("localhost", 9999);

			// trying to establish connection to the server
			client.connect();

			// sets the output globally in this class
			client.setOutputToServer();

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
	public HangmanClient(String serverName, int port) {
		this.serverName = serverName;
		this.port = port;

		headLabel = new JLabel("o");
		rightHandLabel = new JLabel("/");
		leftHandLabel = new JLabel("\\");
		bodyLabel = new JLabel("|");
		rightLegLabel = new JLabel("\\");
		leftLegLabel = new JLabel("/");
		
		// Frame settings
		// Exit program on close
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Set size to 500x500 obviously
		setSize(400, 300);
		// This makes the frame show up in the center of the screen
		setLocationRelativeTo(null);
		// Sets a title
		setTitle("Hangman v1.3!");
		letterButtons = new JButton[26];
		StringBuffer buffer;
		// create all 26 buttons
		for (int i = 0; i < 26; i++) {
			buffer = new StringBuffer();
			buffer.append((char) (i + 65));
			letterButtons[i] = new JButton(buffer.toString());
			letterButtons[i].addActionListener(this);
			letterButtonsPanel.add(letterButtons[i]);

			usedLetters[i] = false;
		}
						mainPanel.setLayout(null);
				
						// Gives the hangman panel a flow layout, so that everything flows
						// nicely
						FlowLayout fl_hangmanPanel = new FlowLayout();
						hangmanPanel.setBounds(0, 0, 392, 48);
						hangmanPanel.setLayout(fl_hangmanPanel);
						
								// Add all the components needed
								hangmanPanel.add(wonLostLabel);
								hangmanPanel.add(scoreLabel);
								hangmanPanel.add(wordToGuessLabel);
								
										mainPanel.add(hangmanPanel);
				letterButtonsPanel.setBounds(0, 142, 392, 130);
		
				letterButtonsPanel.setLayout(new FlowLayout());
				mainPanel.add(letterButtonsPanel);

		getContentPane().add(mainPanel);
		
		
		msgPanel.setBounds(0, 118, 392, 25);
		mainPanel.add(msgPanel);
		msgLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		msgLabel.setForeground(new Color(153, 0, 0));
		
		
		msgPanel.add(msgLabel);
		person.setBounds(0, 48, 392, 70);
		
		mainPanel.add(person);
		person.setLayout(null);
		headLabel.setForeground(new Color(255, 0, 0));
		headLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		headLabel.setBounds(192, 12, 15, 16);
		
		person.add(headLabel);
		
		rightHandLabel.setForeground(new Color(255, 0, 0));
		rightHandLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		rightHandLabel.setBounds(202, 11, 12, 15);
		person.add(rightHandLabel);
		
		leftHandLabel.setForeground(new Color(255, 0, 0));
		leftHandLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		leftHandLabel.setBounds(182, 11, 7, 15);
		person.add(leftHandLabel);
		bodyLabel.setForeground(new Color(255, 0, 0));
		bodyLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		bodyLabel.setBounds(192, 26, 8, 16);
		
		person.add(bodyLabel);
		rightLegLabel.setForeground(new Color(255, 0, 0));
		rightLegLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		rightLegLabel.setBounds(198, 40, 8, 16);
		
		person.add(rightLegLabel);
		leftLegLabel.setForeground(new Color(255, 0, 0));
		leftLegLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		leftLegLabel.setBounds(186, 40, 8, 16);
		
		person.add(leftLegLabel);
		
		JLabel[] hangmanParts = {headLabel,rightHandLabel,leftHandLabel,bodyLabel,rightLegLabel,leftLegLabel};
		setManInvisible(hangmanParts);

		// Disable all the input
		disableInput();

		// Display the frame
		setVisible(true);
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
	 * Reads response from the server, if server asks what character, enables
	 * the input
	 * 
	 * @throws IOException
	 */
	public void readResponse() throws IOException {
		String responseFromServer;
		BufferedReader outputFromServer = new BufferedReader(
				new InputStreamReader(connection.getInputStream()));

		while ((responseFromServer = outputFromServer.readLine()) != null) {
						
			if (responseFromServer.contains("[Message]")) {
//				JOptionPane.showMessageDialog(null,
//						responseFromServer.substring(10));
				msgLabel.setText(responseFromServer.substring(10));
				
			}
			if (responseFromServer.contains("[Score]")) {
				scoreLabel.setText(responseFromServer.substring(7));
			}
			if (responseFromServer.contains("[Paint]")) {
				showHangman(Float.parseFloat((responseFromServer.substring(8))));
				enableInput();
				break;
			}
			if (responseFromServer.contains("[ShownWord]")) {
				wordToGuessLabel.setText(responseFromServer.substring(11));
			}
			if (responseFromServer.contains("[Ended]")) {
				wonLostLabel.setText(responseFromServer.substring(8));
				msgLabel.setText("");
				break;
			}
//			if (responseFromServer.contains("[AskLetter]")) {
//				enableInput();
//				break;
//			}
		}
	}

	/**
	 * @throws IOException
	 */
	public void setOutputToServer() throws IOException {
		outputToServer = new BufferedWriter(new OutputStreamWriter(
				connection.getOutputStream()));
	}

	/**
	 * Handles the input from the buttons
	 */
	public void actionPerformed(ActionEvent ev) {
		for (int i = 0; i < 26; i++) {
			if (ev.getSource() == letterButtons[i]) {
				try {
					// write the button that was clicked to the server
					outputToServer.write((char) (i + 65));
					outputToServer.newLine();
					outputToServer.flush();
					// set the letter to used, so we can't press it again
					usedLetters[i] = true;

					// disable all the inputs
					disableInput();

					// read what the server says
					readResponse();
				} catch (IOException e) {
					System.out
							.println("Something went wrong with writing to the server");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Enables all input
	 */
	public void enableInput() {
		for (int i = 0; i < 26; i++) {
			if (!usedLetters[i]) {
				letterButtons[i].setEnabled(true);
			}
		}
	}

	/**
	 * Disables all input
	 */
	public void disableInput() {
		for (int i = 0; i < 26; i++) {
			letterButtons[i].setEnabled(false);
		}
	}
	
	//to make all parts of hangman body invisible at the first time when application run
	private void setManInvisible(JLabel[] manParts){
		for(int i=0; i<manParts.length;i++){
			manParts[i].setVisible(false);
		}
	}
	
	private void showHangman(float percentage){
		if(percentage==100){
			leftHandLabel.setVisible(true);
			msgLabel.setForeground(new Color(22, 55, 87));
			msgLabel.setText("Congratulations, You win!");
		}
		if(percentage>=80){
			rightHandLabel.setVisible(true);
		}
		if(percentage>=60){
			leftLegLabel.setVisible(true);
		}
		if(percentage>=40){
			rightLegLabel.setVisible(true);
		} 
		if(percentage>=20){
			bodyLabel.setVisible(true);
		} 
		if(percentage>0){
			headLabel.setVisible(true);
		}
	}
}
