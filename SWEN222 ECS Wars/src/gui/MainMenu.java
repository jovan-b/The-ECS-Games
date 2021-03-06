package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import gameWorld.MultiPlayerController;
import gameWorld.SinglePlayerController;
import gameWorld.characters.DavePlayer;
import gameWorld.characters.MarcoPlayer;
import gameWorld.characters.Player;
import gameWorld.characters.PondyPlayer;
import gameWorld.characters.StreaderPlayer;
import network.Server;

/**
 * Displays the main menu for the game.
 * 
 * 
 * @author Jah Seng Lee 300279468
 * @author Sarah Dobie 300315033
 * @author Chris Read 300254724
 * @author Jovan Bogoievski 300305140
 * @author Carl Anderson 300264124
 *
 */
public class MainMenu implements MouseListener, MouseMotionListener {
	
	private static final int IMAGE_SCALE = 8;
	private static final int BUTTON_WIDTH = 300;
	private static final int BUTTON_HEIGHT = 40;
	private static final int BUTTON_TOP_DIFF = BUTTON_HEIGHT*4; // difference between center and top of buttons
	private static final int BUTTON_LEFT_DIFF = (BUTTON_WIDTH/2)+100; // difference between center and left of buttons
	
	private GUICanvas canvas; // the canvas this draws on
	private Image[] sprites; // the sprite images to animate
	private int animState = 0; // the current animation frame
	private int animModifier = 1; // flicks between 1 and -1 to loop animation
	private int animCounter = 0; // counts each frame the player has moved
	private String[] buttonLabels; // the button text
	private int selectedButton = Integer.MAX_VALUE; // the button currently highlighted
	

	/**
	 * Constructor for class MainMenu.
	 * @param canvas The GUICanvas the menu will be drawn on
	 * @param controller The controller running this
	 */
	public MainMenu(GUICanvas canvas) {
		this.canvas = canvas;
		loadImages();
		loadFonts();
		buttonLabels = new String[]{"New Game", "Load Game", "New Server", "Connect", "Quit"};
		
//		setRedrawLoop(true);
	}

	/**
	 * Loads all required fonts.
	 */
	private void loadFonts() {
		try {
		     GraphicsEnvironment ge = 
		         GraphicsEnvironment.getLocalGraphicsEnvironment();
		     ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, MainMenu.class.getResourceAsStream("/pixelmix.ttf")));
		} catch (IOException|FontFormatException e) {
		     System.out.println("Error loading fonts : "+e.getMessage());
		}
	}

	/**
	 * Loads all necessary images.
	 */
	private void loadImages() {
		// Load sprites
		sprites = new Image[3];
		try {
			for (int ani = 0; ani < 3; ani++){
				Image img = ImageIO.read(
						MainMenu.class.getResource("/Players/Dave"+2+ani+".png"));
				sprites[ani] = img.getScaledInstance(img.getWidth(canvas)*IMAGE_SCALE,
						img.getHeight(canvas)*IMAGE_SCALE, Image.SCALE_FAST);
			}
		} catch (IOException e) {
			System.out.println("Error loading player images: " + e.getMessage());
		}
	}
	
	/**
	 * Displays the main menu.
	 * @param g The Graphics object with which to draw the menu
	 */
	public void paint(Graphics g){
		// calculate canvas centre
		int midX = canvas.getWidth()/2;
		int midY = canvas.getHeight()/2;
		// paint background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		// draw little dave
		g.drawImage(getCurrentImage(), midX+100, midY-150, canvas);
		animate();
		// draw the buttons
		drawMenuItems(g);
	}
	
	/**
	 * Draws the title and buttons.
	 * @param g The Graphics object with which to draw
	 */
	private void drawMenuItems(Graphics g){
		// calculate canvas centre
		int midX = canvas.getWidth()/2;
		int midY = canvas.getHeight()/2;
		// change graphics settings
		g.setColor(Color.WHITE);
		Graphics2D g2 = ((Graphics2D)g);
		g2.setStroke(new BasicStroke(5));
		int textSize = BUTTON_HEIGHT-10;
		// draw title
		drawTitle(g, textSize, midX, midY);
		// draw buttons
		drawButtons(g, textSize, midX, midY);
		// return stroke to default
		g2.setStroke(new BasicStroke(1));
	}

	/**
	 * Draws the title 'The ECS Games' at the top of the menu.
	 * @param g The graphics object with which to draw
	 * @param textSize The button text size
	 * @param midX The horizontal centre of the screen
	 * @param midY The vertical centre of the screen
	 */
	private void drawTitle(Graphics g, int textSize, int midX, int midY) {
		int buttonX = midX - BUTTON_LEFT_DIFF;
		int buttonY = midY - BUTTON_TOP_DIFF;
		int gap = 20 + BUTTON_HEIGHT;
		// draw title
		g.setFont(new Font("pixelmix", Font.PLAIN, textSize+10));
		g.drawString("The ECS Games", buttonX, buttonY-gap);
		g.setFont(new Font("pixelmix", Font.PLAIN, textSize));
	}

	/**
	 * Draws the menu buttons.
	 * @param g The graphics object with which to draw
	 * @param textSize The button text size
	 * @param midX The horizontal centre of the screen
	 * @param midY The vertical centre of the screen
	 */
	private void drawButtons(Graphics g, int textSize, int midX, int midY) {
		int buttonX = midX - BUTTON_LEFT_DIFF;
		int buttonY = midY - BUTTON_TOP_DIFF;
		int gap = 20 + BUTTON_HEIGHT;
		// draw buttons
		for(int i=0; i<buttonLabels.length; i++){
			g.drawRect(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
			// highlight the button hovered over
			if(i == this.selectedButton){
				g.setColor(new Color(255, 255, 255, 128));
				g.fillRect(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
			}
			// draw text
			int textWidth = g.getFontMetrics().stringWidth(buttonLabels[i]);
			int labelX = buttonX + BUTTON_WIDTH/2 - textWidth/2;
			int labelY = buttonY + BUTTON_HEIGHT/2 + textSize/2;
			g.setColor(Color.WHITE);
			g.drawString(buttonLabels[i], labelX, labelY);
			// increment y
			buttonY += gap;
		}
	}
	
	/**
	 * Get the next image in the animation.
	 * @return The next image to draw
	 */
	private Image getCurrentImage() {	
		return sprites[animState];
	}
	
	/**
	 * Increments the animation state.
	 */
	private void animate() {
		animCounter++;
		if (animCounter > 10){
			animState += animModifier;
			animCounter = 0;
			if (animState <= 0 || animState >= 2){
				animModifier *= -1;
			}
		}
	}

	@Override
	/**
	 * Checks which button the mouse is over and highlights that button
	 */
	public void mouseMoved(MouseEvent e) {
		// calculate canvas centre
		int midX = canvas.getWidth()/2;
		int midY = canvas.getHeight()/2;
		int x = e.getX();
		int y = e.getY();
		int buttonY = midY - BUTTON_TOP_DIFF;
		int gap = 20 + BUTTON_HEIGHT;
		int buttonX = midX - BUTTON_LEFT_DIFF;
		
		// check if x is within button bounds
		if(buttonX <= x && x < buttonX+BUTTON_WIDTH){
			// check which y it is on
			for(int i=0; i<buttonLabels.length; i++){
				if(buttonY <= y && y < buttonY + BUTTON_HEIGHT){
					// found the selected button
					this.selectedButton = i;
					return;
				}
				buttonY += gap;
			}
		}
		// deselect buttons
		this.selectedButton = Integer.MAX_VALUE;
	}

	@Override
	public void mouseDragged(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	/**
	 * Calculates which button has been clicked on and carries out the corresponding
	 * action
	 */
	public void mouseReleased(MouseEvent e) {
		// calculate positional values
		int midX = canvas.getWidth()/2;
		int midY = canvas.getHeight()/2;
		int x = e.getX();
		int y = e.getY();
		int buttonY = midY - BUTTON_TOP_DIFF;
		int gap = 20 + BUTTON_HEIGHT;
		int buttonX = midX - BUTTON_LEFT_DIFF;
		
		// check if x is within button bounds
		if(buttonX <= x && x < buttonX+BUTTON_WIDTH){
			// check which y it is on
			for(int i=0; i<buttonLabels.length; i++){
				// check if we are on a button
				if(buttonY <= y && y < buttonY + BUTTON_HEIGHT){
					// figure out the button we're on
					switch(i){
					case 0 : newGame(); break;
					case 1 : loadGame(); break;
					case 2 : newServer(); break;
					case 3 : connect(); break;
					case 4 : quit(); break;
					}
				}
				// increment y
				buttonY += gap;
			}
		}
	}

	
	private void newGame() {
		canvas.setMainMenu(false);
		canvas.togglePlayerSelectMenu(false);
	}

	private void loadGame() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "XML Files", "xml");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(canvas);
		
		if(chooser.getSelectedFile() == null || 
				returnVal != JFileChooser.APPROVE_OPTION){	//Player hasn't choosen a file
			return;
		}
		canvas.setMainMenu(false);
		canvas.startGame(new SinglePlayerController(chooser.getSelectedFile()), 0);
		
	}

	/**
	 * Creates a server for game clients to connect to.
	 */
	private void newServer() {
		try{
			String p = JOptionPane.showInputDialog(canvas, "Enter the "
					+ "port for the server");
			String numberOfClients = JOptionPane.showInputDialog(canvas, "Enter the "
					+ "number of clients for the server");
			
			int port = Integer.parseInt(p);
			int clients = Integer.parseInt(numberOfClients);
			
			//Check that the number of clients entered is not under or over clients limit
			if(clients < 2 || clients > 4){
				JOptionPane.showMessageDialog(canvas, "Error: number of"
						+ " clients must be between 2 and 4.");
				return;
			}
			
			Server server = new Server(port, clients, canvas);
			server.start();
		} catch (NumberFormatException ne){
			JOptionPane.showMessageDialog(canvas, "Error: port or "
					+ "number of clients entered is not a number");
		}
	}

	/**
	 * Gets which server the client wants to connect to then starts the game.
	 */
	private void connect() {
		canvas.setMainMenu(false);
		canvas.togglePlayerSelectMenu(true);
	}
	
	/**
	 * Sets up a multiplayer game by getting all players and starting the
	 * controller
	 * @param player
	 */
	public void setUpMultiplayerGame(Player player){
		try{
			//Get the port and ip from the user of the server they're trying to connect
			String ip = JOptionPane.showInputDialog(canvas, "Enter the "
					+ "IP of the server");
			String p = JOptionPane.showInputDialog(canvas, "Enter the "
					+ "port for the server");
			int port = Integer.parseInt(p);
			
			Socket s = new Socket(ip, port);
			
			//Create the socket input stream to wait for the user input
			DataOutputStream output = new DataOutputStream(s.getOutputStream());
			DataInputStream input = new DataInputStream(s.getInputStream());
			
			int playerNum = getPlayerNumber(player);
			
			//Waits for the server to send an amount of players in the game
			int numberOfPlayers = input.readInt();
			int uid = input.readInt();
			output.writeInt(playerNum);
			
			//Get every player in the game and add them to the player list
			int[] playerNumbers = new int[numberOfPlayers];
			ArrayList<Player> players = new ArrayList<Player>();
			for(int i = 0; i < numberOfPlayers; i++){
				playerNumbers[i] = input.readInt();
				players.add(getPlayerFromNumber(playerNumbers[i]));
			}
			
			//Start the game
			canvas.togglePlayerSelectMenu(true);
			canvas.startGame(new MultiPlayerController(s, uid, numberOfPlayers, canvas, players), uid);
		} catch (IOException e){
			JOptionPane.showMessageDialog(canvas, "Error: could not find server");
			canvas.setMainMenu(true);
		} catch (NumberFormatException ne){
			JOptionPane.showMessageDialog(canvas, "Error: port is not a number");
			canvas.setMainMenu(true);
		}
	}

	private void quit() {
		System.exit(0);
	}

	/**
	 * Get a player id to send across the server to tell every client what
	 * your player is
	 * @return
	 */
	public int getPlayerNumber(Player player){
		String name = player.getName();
		switch(name){
			case("Dave"):
				return 1;
			case("Marco"):
				return 2;
			case("Pondy"):
				return 3;
			default:
				return 4;
		}
	}
	
	/**
	 * return a player from a given number
	 * @param i
	 * @return
	 */
	public Player getPlayerFromNumber(int i){
		switch(i){
			case(1):
				return new DavePlayer(null, 0, 0);
			case(2):
				return new MarcoPlayer(null, 0, 0);
			case(3):
				return new PondyPlayer(null, 0, 0);
			default:
				return new StreaderPlayer(null, 0, 0);
		}
	}
	
	
}
