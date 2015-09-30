package main;

import gameObjects.Door;
import gameObjects.Item;
import gameObjects.Room;
import network.ClientConnection;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import characters.Player;
import characters.DavePlayer;
import characters.nonplayer.NonPlayer;
import characters.nonplayer.strategy.RespawnStrategy;
import characters.nonplayer.strategy.SentryCombatStrategy;
import characters.nonplayer.strategy.WanderStrategy;

/**
 * Main controller for ECS Wars
 * Runs the main game loop which:
 * 		deals with user input
 * 		updates the game logic
 * 
 * @author Jah Seng Lee
 *
 */
public class Controller extends Thread implements KeyListener, MouseListener, MouseMotionListener{
	
	public static final double FRAME_RATE = 1.0/60;	//a 60th of a second
	private boolean isRunning = false;
	
	private GUIFrame gui;
	private Player player;
	private List<Player> players;
	private int uid;
	private ArrayList<Room> rooms;
	private Set<Door> doors;
	
	
	private BitSet keyBits = new BitSet(256);	//set of keys being pressed right now
	private int[] mouseLocation = new int[2];	//position of mouse if it is being clicked
												//mouseLocation[0] is x
												//mouseLocation[1] is y
	private int mouseX = 0;
	private int mouseY = 0;
		
	/**
	 * Controller constructor for a singeplayer game
	 */
	public Controller(){
		initialise(this, this, this);
		run();
		
	}
	
	/**
	 * Controller constructor for a multiplayer client
	 */
	public Controller(ClientConnection client, int numberOfClients, int uid){
		MPInitialise(client, client, client, numberOfClients, uid);
		start();
	}

	/**
	 * Initialises the fields of a multiplayer game
	 * @param key
	 * @param mouse
	 * @param mouse2
	 * @param numberOfClients
	 * @param uid
	 */
	public void MPInitialise(KeyListener key, MouseListener mouse, MouseMotionListener mouse2,
			int numberOfClients, int uid) {
		isRunning = true;
		
		rooms = new ArrayList<>();
		doors = new HashSet<>();
		setupRooms();
		
		Room room = rooms.get(0);
		rooms.add(room);
		
		players = new ArrayList<Player>();
		for(int i = 0; i<numberOfClients; i++){
			players.add(new DavePlayer(room, (i+2)*24, 2*24));
			room.addPlayer(players.get(i));
		}
		
		gui = new GUIFrame(this, players.get(uid), key, mouse, mouse2);
		players.get(uid).setCanvas(gui.getCanvas());
		gui.getCanvas().setMainMenu(false);
		
		SoundManager.playSong("battle_1.mp3");
	}
	
	/**
	 * Initialise the fields of this class
	 */
	private void initialise(KeyListener key, MouseListener mouse, MouseMotionListener mouse2) {
		isRunning = true;
		rooms = new ArrayList<>();
		doors = new HashSet<>();
		setupRooms();
		Room room = rooms.get(0); //FIXME
		player = new DavePlayer(room, 2*24, 2*24);
		players = new ArrayList<Player>();
		players.add(player);
		uid = 0;
		gui = new GUIFrame(this, player, key, mouse, mouse2);
		player.setCanvas(gui.getCanvas());
		
		SoundManager.playSong("battle_1.mp3");
	}
	
	/**
	 * Initialise the fields of this class
	 */
	public void initialiseGame() {
		isRunning = true;
		Room room = rooms.get(0); //FIXME
		room.addPlayer(player);
		player.setCanvas(gui.getCanvas());
		
		NonPlayer npc = new NonPlayer(room, 5*24, 7*24, new WanderStrategy());
		npc.setStrategy(NonPlayer.Events.DEATH, new RespawnStrategy(5000));
		room.addNPC(npc);
		
		SaveManager.saveGame(this, "test_save.xml");
	}
	
	/**
	 * Main game loop
	 * Should update the game every frameRate seconds
	 * If it's time to update the logic:
	 * 		update logic
	 * 		check if it's time to redraw
	 * 
	 * Otherwise wait till it's time to update
	 * 
	 * @param frameRate seconds per frame
	 */
	public void run() {

		//convert time to seconds
		double nextTime = (double)System.nanoTime()/1000000000.0;
		
		while(isRunning){
			//convert time to seconds
			double currentTime = (double)System.nanoTime()/1000000000.0;
			
			if(currentTime >= nextTime){
				//assign time for the next update
				nextTime += FRAME_RATE;
				update();
				if(currentTime < nextTime) gui.draw();
			}
			else{
				// calculate the time to sleep
				int sleepTime = (int) (1000.0 * (nextTime - currentTime));
				// sanity check
				if (sleepTime > 0) {
					// sleep until the next update
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
		}
	}

	/**
	 * Update the game logic
	 * Take in user input and call appropriate methods
	 * Update the positions of non-input deterministic objects
	 * 
	 */
	private void update() {
		dealWithInput();//deal with user input
		updateAndCollide();//deal with other objects and with collision detection
		checkTooltip(); // check if a tooltip should be displayed
	}
	
	private void updateAndCollide() {
		players.get(uid).update();
	}

	/**
	 * Updates player appropriately depending on current keys pressed
	 */
	private void dealWithInput() {
		// Player Movement
		if(isKeyPressed(KeyEvent.VK_RIGHT) || isKeyPressed(KeyEvent.VK_D)){
			player.move("right");
		}
		if(isKeyPressed(KeyEvent.VK_LEFT) || isKeyPressed(KeyEvent.VK_A)){
			player.move("left");
		}
		if(isKeyPressed(KeyEvent.VK_UP) || isKeyPressed(KeyEvent.VK_W)){
			player.move("up");
		}
		if(isKeyPressed(KeyEvent.VK_DOWN) || isKeyPressed(KeyEvent.VK_S)){
			player.move("down");
		}
		if(isKeyPressed(KeyEvent.VK_SHIFT)){
			player.setSpeedMulti(2);
		} else {
			player.setSpeedMulti(1);
		}
		if(isLeftMousePressed()){
			player.shoot(mouseLocation[0], mouseLocation[1]);
		}
	}
	
	/**
	 * Returns true if key 'keycode' is being pressed 
	 * Otherwise returns false
	 * 
	 * @param keyCode
	 * @return
	 */
	private boolean isKeyPressed(int keyCode) {
		return keyBits.get(keyCode);
	}
	
	/**
	 * Returns true if user has pressed left mouse button and not released it
	 * Otherwise returns false (left mouse button is not being pressed down)
	 */
	private boolean isLeftMousePressed(){
		return mouseLocation[0] != 0 && mouseLocation[1] != 0;
	}
	
	private void setupRooms(){
		try {
			Scanner s = new Scanner(new File("Resources"+File.separator+"RoomIndex.txt"));
			String roomName;
			while (s.hasNextLine()){
				roomName = s.nextLine();
				rooms.add(new Room(roomName, this));
			}
		} catch (FileNotFoundException e) {
			System.out.println("Error loading RoomIndex file: " + e.getMessage());
		}
	}

	/**
	 * If key is pressed by user, add to the keyBits set
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		keyBits.set(e.getKeyCode());
	}

	/**
	 * If key is released by user, clear from the keyBits set
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		// View Rotation
		if(e.getKeyCode() == KeyEvent.VK_Q){
			player.rotateViewLeft();
		}
		if(e.getKeyCode() == KeyEvent.VK_E){
			player.rotateViewRight();
		}
		if(e.getKeyCode() == KeyEvent.VK_1){
			player.inventoryItem(0).use(player);
		}
		if(e.getKeyCode() == KeyEvent.VK_2){
			player.inventoryItem(1).use(player);
		}
		if(e.getKeyCode() == KeyEvent.VK_3){
			player.inventoryItem(2).use(player);
		}
		if (e.getKeyCode() == KeyEvent.VK_MINUS){
			gui.getCanvas().setViewScale(1);
			scaleEverything(1);
		}
		if (e.getKeyCode() == KeyEvent.VK_EQUALS){
			gui.getCanvas().setViewScale(2);
			scaleEverything(2);
		}
		keyBits.clear(e.getKeyCode());
	}

	public void scaleEverything(int scale) {
		GUICanvas c = gui.getCanvas();
		int viewScale = c.getViewScale();
		c.scaleUI();
		Image image;
		for (Room r : rooms){
			Image[][] images = r.getImages();
			Image[][] scaled = new Image[4][2];
			for (int i = 0; i < 4; i++){
				for (int j = 0; j < 2; j++){
					scaled[i][j] = scaleImage(images[i][j], c, scale);
				}
			}
			r.setScaledImages(scaled);
			
			// scale items
			Item[][] contents = r.getContents();
			for (int i = 0; i < contents.length; i++){
				for (int j = 0; j < contents[0].length; j++){
					for (int v = 0; v < 4; v++){
						image = contents[i][j].getImage(v);
						if (image != null){
							contents[i][j].setScaledImage(v, scaleImage(image, c, scale));
						}
					}
				}
			}
			
			// scale players
			for (Player p : r.getAllCharacters()){
				images = p.getImages();
				scaled = new Image[4][3];
				for (int i = 0; i < 4; i++){
					for (int j = 0; j < 3; j++){
						scaled[i][j] = scaleImage(images[i][j], c, scale);
					}
				}
				p.setScaledImages(scaled);
				for (Item i : p.getInventory()){
					if (i != null){
						image = i.getImage(0);
						i.setScaledImage(0, scaleImage(image, c, scale));
					}
				}
			}
		}
	}
	
	public Image scaleImage(Image image, GUICanvas c, int scale){
		return image.getScaledInstance(image.getWidth(c)*scale, image.getHeight(c)*scale, Image.SCALE_FAST);
	}

	//Don't care about this method
	@Override
	public void keyTyped(KeyEvent e) {

		
	}
	
	//don't care about this method
	@Override
	public void mouseClicked(MouseEvent e) {	
	}	
	//don't care about this method
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	//don't care about this method
	@Override
	public void mouseExited(MouseEvent e) {	
	}

	/**
	 * When mouse event is fired
	 * 	clear mouselocation
	 * 	get x, y position of mouse
	 * 	add new xy to mouseLocation
	 * 
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == 1){
			mouseLocation[0] = e.getX();
			mouseLocation[1] = e.getY();
		} else if (e.getButton() == 3){
			int x = e.getX();
			int y = e.getY();
			int viewScale = gui.getCanvas().getViewScale();
			if (24*2*viewScale < y && y < 24*3*viewScale){
				if (24*viewScale < x && x < 24*(Player.INVENTORY_SIZE+1)*viewScale){
					int index = (x-(24*viewScale))/(24*viewScale);
					player.dropItem(index);
				} else {
					Room room = player.getCurrentRoom();
					Item item = room.itemAtMouse(x, y, viewScale, player);
					item.use(player);
				}
			} else {
				Room room = player.getCurrentRoom();
				Item item = room.itemAtMouse(x, y, viewScale, player);
				item.use(player);
			}
			
		}
	}

	/**
	 * clear mouselocation, so that nothing is being pressed
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		mouseLocation = new int[2];
	}

	
	public static void main(String[] args){
		new Controller();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (e.getButton() != 3){
			mouseLocation[0] = e.getX();
			mouseLocation[1] = e.getY();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	private void checkTooltip() {
		int viewScale = gui.getCanvas().getViewScale();
		int x = mouseX;
		int y = mouseY;
		String desc = "";
		if (24*2*viewScale < y && y < 24*3*viewScale){
			if (24*viewScale < x && x < 24*(Player.INVENTORY_SIZE+1)*viewScale){
				int index = (x-(24*viewScale))/(24*viewScale);
				desc = player.inventoryItem(index).getDescription();
			} else {
				desc = player.getCurrentRoom().itemAtMouse(x, y, viewScale, player).getDescription();
			}
		} else {
			desc = player.getCurrentRoom().itemAtMouse(x, y, viewScale, player).getDescription();
		}
		gui.getCanvas().setToolTip(desc, x, y);
	}

	public Set<Door> getDoors() {
		return doors;
	}
	
	/**
	 * Returns the specified player in the player list
	 * @param i
	 * @return
	 */
	public Player getPlayer(int uid){
		return players.get(uid);
	}
	
	public GUIFrame getGUI(){
		return gui;
	}

	public ArrayList<Room> getRooms(){
		return rooms;
	}


}
