package ver1_4;

// hangman.java
// version 2.1

// James Terhune  james@terhune.net   jterhune@nbnet.nb.ca
// http://terhune.net/

//history:
//1.0 Jul 1997 	- initial release
//1.1 May 1999 	- now displays word when incorrectly guessed
//2.0 Jul 1999 	- Java 1.1/1.2 compliant
//				- changed button layout to grid layout
//				- expanded comments
//				- adjusted event stuff
//2.1 Feb 2000  - Cleaned up the code a bit
//2.2 Mar 2006  - Adapted with minor amendments to APP
//2.3 Feb 2013  - Remove deprecated AWT features

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class hman2 extends Applet implements ActionListener {

	// this is the used letter array
	private boolean usd[] = new boolean[26];
	private String guessme;
	private int numguesses = 0;
	private boolean finished = false;
	private boolean won = false;
	public static final long serialVersionUID = 778899L;

	private Button a[];

	public void init() {
		int i;
		StringBuffer buffer;

		// setLayout( new GridLayout( 2,13) );

		a = new Button[26];

		// create all 26 buttons
		for (i = 0; i < 26; i++) {
			buffer = new StringBuffer();
			buffer.append((char) (i + 65));
			a[i] = new Button(buffer.toString());
			a[i].addActionListener(this);
			add(a[i]);
		}
		
		try {
			Socket socketConnection = new Socket("localhost", 9999);
			
			
			ObjectOutputStream clientOutputStream = new
				ObjectOutputStream(socketConnection.getOutputStream());
			
			ObjectInputStream clientInputStream = new
				ObjectInputStream(socketConnection.getInputStream());
			
			clientOutputStream.writeObject(new Word());	
					
			guessme = ((Word)clientInputStream.readObject()).getChoosenServerWord().toUpperCase();
			
			clientOutputStream.close();
			clientInputStream.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void paint(Graphics g) {
		// draw gallows and rope
		setBackground(Color.white);
		g.fillRect(10, 250, 150, 20);
		g.fillRect(40, 70, 10, 200);
		g.fillRect(40, 70, 60, 10);
		g.setColor(Color.yellow);
		g.fillRect(95, 70, 5, 25);

		g.setColor(Color.orange);

		if (numguesses >= 1)
			g.drawOval(82, 95, 30, 30);

		g.setColor(Color.green);

		if (numguesses >= 2)
			g.drawLine(97, 125, 97, 150);

		if (numguesses >= 3)
			g.drawLine(97, 150, 117, 183);

		if (numguesses >= 4)
			g.drawLine(97, 150, 77, 183);

		if (numguesses >= 5)
			g.drawLine(97, 125, 117, 135);

		if (numguesses >= 6)
			g.drawLine(97, 125, 77, 135);

		StringBuffer st = new StringBuffer();

		for (int l = 0; l <= 25; l++) {
			if (usd[l])
				st.append((char) (l + 65));
			else
				st.append(".");
		}
		g.setColor(Color.blue);

		Font f = new Font("Courier", Font.ITALIC, 14);

		g.setFont(f);
		g.drawString(st.toString(), 25, 285);

		StringBuffer guessed = new StringBuffer();

		Font ff = new Font("Courier", Font.BOLD, 24);
		g.setColor(Color.black);

		g.setFont(ff);

		for (int mm = 0; mm < guessme.length(); mm++) {
			if (usd[(int) guessme.charAt(mm) - 65])
				guessed.append(guessme.charAt(mm));
			else
				guessed.append(".");
		}

		g.drawString(guessed.toString(), 75, 230);

		if (numguesses >= 6) {
			g.setColor(Color.white);
			g.fillRect(70, 200, 200, 30);
			g.setColor(Color.black);
			g.drawString(guessme.toString(), 75, 230);
			Font fff = new Font("Helvetica", Font.BOLD, 36);
			g.setFont(fff);

			g.setColor(Color.red);
			g.drawString("You lose!", 200, 100);

			finished = true;
		}

		if (won) {
			Font fff = new Font("Helvetica", Font.BOLD, 36);
			g.setFont(fff);

			// Color red=new Color.red
			g.setColor(Color.red);

			g.drawString("You Win!", 200, 100);
			finished = true;
		}

	}

	public void rer(int lett) {

		if (!finished) {
			boolean found = false;
			boolean www = false;

			if (!usd[lett]) {
				for (int mm = 0; mm < guessme.length(); mm++) {
					if (guessme.charAt(mm) == ((char) (lett + 65)))
						found = true;
				}
				if (!found)
					numguesses++;

			}

			usd[lett] = true;

			for (int mm = 0; mm < guessme.length(); mm++) {
				if (!usd[(int) (guessme.charAt(mm)) - 65])
					www = true;
			}

			if (!www)
				won = true;

			repaint();

		}

	}

	public void actionPerformed(ActionEvent ev) {
		for (int i = 0; i < 26; i++) {
			if (ev.getSource() == a[i]) {
				rer(i);
			}
		}

	}
}
