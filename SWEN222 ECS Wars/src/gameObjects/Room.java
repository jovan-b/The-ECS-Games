package gameObjects;

import gameEvents.Event;
import gameEvents.GameClock;
import gameEvents.RespawnEvent;
import gameObjects.weapons.projectiles.Projectile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;

import characters.Player;
import characters.nonplayer.NonPlayer;
import main.Controller;
import main.GUICanvas;

/**
 * Represents an area in the game. Holds contents relevant to that area and
 * is responsible for drawing itself and everything in it.
 * @author Sarah Dobie, Chris Read
 *
 */
public class Room {
	private String name;
	private String description;
	private Image[][] images;
	private Image[][] scaledImages;
	private Item[][] contents; // items in the room
	private int cols;
	private int rows;
	private int squareSize = 24; //TODO get this value from player view scale
	private int width;
	private int height;
	private int xOrigin;
	private int yOrigin;
	
	private Set<Projectile> projectiles = Collections.synchronizedSet(new HashSet<Projectile>());
	private Set<Player> players = new HashSet<>();
	private Set<NonPlayer> npcs = new HashSet<>();
	private Set<Door> doors = new HashSet<>();
	
	/**
	 * Constructor for class Room
	 * @param roomName Name of room/file to parse.
	 */
	public Room(String roomName, Controller ctrl){
		name = roomName;
		images = new Image[4][2];
		loadImages();
		scaledImages = images;
		parseFile(ctrl);
		width = cols*squareSize;
		height = rows*squareSize;
		xOrigin = 0;
		yOrigin = 0;
	}

	/**
	 * Read room file and convert to array of items and other room data.
	 */
	private void parseFile(Controller ctrl) {
		try {
			Scanner s = new Scanner(new File("Resources"+File.separator+"Rooms"+File.separator+name+".txt"));
			description = s.nextLine();
			cols = Integer.parseInt(s.nextLine());
			rows = Integer.parseInt(s.nextLine());
			contents = new Item[cols][rows];
			
			// populate item array
			for(int r=0; r<rows; r++){
				String line = s.nextLine();
				int fileCol = 0;
				for(int c=0; c<cols; c++){
					String code = line.substring(fileCol, fileCol+2);
					contents[c][r] = itemFromCode(code, ctrl, c, r);
					fileCol+=2;
				}
			}
			s.close();
			
		} catch (IOException e) {
			System.out.println("Error loading room file: "+ e.getMessage());
		}
	}
	
	/**
	 * Converts a char code into an item.
	 * @param code The 2 char String from a room file being parsed.
	 * @return A new Item according to the code.
	 */
	private Item itemFromCode(String code, Controller ctrl, int col, int row){
		// test to see if the code is an integer: codes for a door
		try{
			Integer.parseInt(code);
			return parseDoor(code, ctrl, col, row);
		} catch(NumberFormatException e){}
		
		// code isn't an integer
		switch(code){
		case "__" : return new Floor();
		case "_k" : return new Floor(new KeyCard());
		case "_t" : return new Floor(new Torch());
		case "##" : return new Wall();
		case "PP" : return new Pillar();
		case "Dh" : return new Desk(true);
		case "Dv" : return new Desk(false);
		case "PF" : return new Photocopier('F');
		case "PB" : return new Photocopier('B');
		case "PL" : return new Photocopier('L');
		case "PR" : return new Photocopier('R');
		case "cF" : return new SmallChest('F');
		case "cB" : return new SmallChest('B');
		case "cL" : return new SmallChest('L');
		case "cR" : return new SmallChest('R');
		default: return new Floor(); // if no match, safely return a floor
		}
	}

	private Item parseDoor(String doorCode, Controller ctrl, int col, int row) {
		// Check if door object already exists in current room
		for(Door d : doors){
			if(d.getParseCode().equals(doorCode)){
				return d;
			}
		}
		// Check if door object exists in another room
		for (Door d : ctrl.getDoors()){
			if (d.getParseCode().equals(doorCode)){
				d.addRoom2(this, col, row);
				doors.add(d);
				return d;
			}
		}
		Door door = new Door(doorCode, this, col, row);
		ctrl.getDoors().add(door);
		doors.add(door);
		return door;
	}

	/**
	 * Loads all background and foreground images necessary for this room.
	 */
	private void loadImages() {
		try {
			images[0][0] = ImageIO.read(new File("Resources"+File.separator+"Rooms"+File.separator+name+File.separator+"NorthBase.png"));
			images[0][1] = ImageIO.read(new File("Resources"+File.separator+"Rooms"+File.separator+name+File.separator+"NorthTop.png"));
			
			images[1][0] = ImageIO.read(new File("Resources"+File.separator+"Rooms"+File.separator+name+File.separator+"EastBase.png"));
			images[1][1] = ImageIO.read(new File("Resources"+File.separator+"Rooms"+File.separator+name+File.separator+"EastTop.png"));
			
			images[2][0] = ImageIO.read(new File("Resources"+File.separator+"Rooms"+File.separator+name+File.separator+"SouthBase.png"));
			images[2][1] = ImageIO.read(new File("Resources"+File.separator+"Rooms"+File.separator+name+File.separator+"SouthTop.png"));
			
			images[3][0] = ImageIO.read(new File("Resources"+File.separator+"Rooms"+File.separator+name+File.separator+"WestBase.png"));
			images[3][1] = ImageIO.read(new File("Resources"+File.separator+"Rooms"+File.separator+name+File.separator+"WestTop.png"));
		} catch (IOException e) {
			System.out.println("Error loading room images: "+e.getMessage());
		}
	}

	/**
	 * Draws this room and its contents on the graphics pane.
	 * @param g Graphics object with which to draw the room.
	 * @param c The canvas on which to draw the room.
	 * @param player The current player.
	 */
	public void draw(Graphics g, GUICanvas c, Player player){
		int viewScale = c.getViewScale();
		int viewDirection = player.getViewDirection(); 
		int playerX = player.getX(); 
		int playerY = player.getY(); 
		
		Item[][] rotated = contents;
		
		// calculate origin for drawing based on view direction
		switch(viewDirection){
			case 1: // EAST
				xOrigin = (c.getWidth()/2)-(playerY*viewScale);
				yOrigin = (c.getHeight()/2)-((width*viewScale)-(playerX*viewScale));
				rotated = rotatedArrayClockwise(contents);
				break;
			case 2: // SOUTH
				xOrigin = (c.getWidth()/2)-((width*viewScale)-(playerX*viewScale));
				yOrigin = (c.getHeight()/2)-((height*viewScale)-(playerY*viewScale));
				rotated = rotatedArray180(contents);
				break;
			case 3: // WEST
				xOrigin = (c.getWidth()/2)-((height*viewScale)-(playerY*viewScale));
				yOrigin = (c.getHeight()/2)-(playerX*viewScale);
				rotated = rotatedArrayAntiClockwise(contents);
				break;
			case 0: default: // DEFAULT TO NORTH
				xOrigin = (c.getWidth()/2)-(playerX*viewScale);
				yOrigin = (c.getHeight()/2)-(playerY*viewScale);
				break;
		}
		
		// Create copy of projectile set to avoid concurrent modification
		Set<Projectile> projectilesToDraw = new HashSet<Projectile>();
		projectilesToDraw.addAll(projectiles);
		
		// Set row value for projectiles and players
		checkPlayers(viewDirection);
		try {
			checkProjectiles(viewDirection, projectilesToDraw);
		} catch (ConcurrentModificationException e){}
		
		// draw the images
		drawRoomContents(g, c, viewDirection, xOrigin, yOrigin, rotated, projectilesToDraw);
	}

	/**
	 * Draws all items, players, and projectiles in this room.
	 * @param g The Grapics object with which to draw the room
	 * @param c The canvas on which to draw
	 * @param viewDirection The direction the player is viewing in
	 * @param drawX The x origin of the room
	 * @param drawY The y origin of the room
	 * @param rotated The rotated contents array according to the view dir
	 * @param projectiles The projectiles to be drawn
	 */
	private void drawRoomContents(Graphics g, GUICanvas c, int viewDirection, int drawX, int drawY,
			Item[][] rotated, Set<Projectile> projectiles) {
		int viewScale = c.getViewScale();
		
		// Draw background Image
		g.drawImage(scaledImages[viewDirection][0], drawX, drawY-(squareSize*viewScale*3), c);
		
		// draw contents
		Image image;
		for(int row=0; row<rotated[0].length; row++){
			for(int col=0; col<rotated.length; col++){
				Item item = rotated[col][row];
				// draw item at current square
				if(item.getScaledImage(viewDirection) != null){
					image = item.getScaledImage(viewDirection);
					g.drawImage(image, drawX+(col*squareSize*viewScale)-(item.xOffset(viewDirection)*squareSize*viewScale), 
							drawY+(row*squareSize*viewScale)-(item.yOffset(viewDirection)*squareSize*viewScale), c);
				}
				// draw projectile at this row
				for (Projectile p : projectiles){
					if (p.getRow() == row-1){ // Ensures the projectile is drawn above their current row
						drawProjectile(g, c, viewDirection, drawX, drawY, p);
						p.setRow(-1);
					}
				}
				// draw player at this row
				for (Player p : players){
					if (p.getRow() == row-1){ // Ensures the player is drawn above their current row
						drawPlayer(g, c, viewDirection, drawX, drawY, p);
						p.setRow(-1);
					}
				}
				
				for (NonPlayer npc : npcs){
					if (npc.getRow() == row-1){ // Ensures the player is drawn above their current row
						drawPlayer(g, c, viewDirection, drawX, drawY, npc);
						npc.setRow(-1);
					}
				}
			}
		}
		
		// Draw foreground Image
		g.drawImage(scaledImages[viewDirection][1], drawX, drawY-(squareSize*viewScale*3), c);
	}

	/**
	 * Draws the player.
	 * @param g The Grapics object with which to draw the room
	 * @param c The canvas on which to draw
	 * @param viewDirection The direction the player is viewing in
	 * @param drawX The x origin of the room
	 * @param drawY The y origin of the room
	 * @param p The player to draw
	 */
	private void drawPlayer(Graphics g, GUICanvas c, int viewDirection, int drawX, int drawY, Player p) {
		Image playerImage = p.getImage(viewDirection);
		int viewScale = c.getViewScale();
		int playerX = p.getX();
		int playerY = p.getY();
		// draw player relative to view direction
		switch(viewDirection){
		case 1: // EAST
			g.drawImage(playerImage, drawX+(playerY*viewScale)-(16*viewScale),
					drawY+((width-playerX)*viewScale)-(24*viewScale), c);
			break;
		case 2: // SOUTH
			g.drawImage(playerImage, drawX+((width-playerX)*viewScale)-(16*viewScale),
					drawY+((height-playerY)*viewScale)-(24*viewScale), c);
			break;
		case 3: // WEST
			g.drawImage(playerImage, drawX+((height-playerY)*viewScale)-(16*viewScale),
					drawY+(playerX*viewScale)-(24*viewScale), c);
			break;
		default: // NORTH
			g.drawImage(playerImage, drawX+(playerX*viewScale)-(16*viewScale),
					drawY+(playerY*viewScale)-(24*viewScale), c);
		}
	}
	
	/**
	 * Draws an individual projectile.
	 * @param g The Grapics object with which to draw the room
	 * @param c The canvas on which to draw
	 * @param viewDirection The direction the player is viewing in
	 * @param drawX The x origin of the room
	 * @param drawY The y origin of the room
	 * @param p The projectile to draw
	 */
	private void drawProjectile(Graphics g, GUICanvas c, int viewDirection, int drawX, int drawY, Projectile p){
		int viewScale = c.getViewScale();
		int pSize = p.getSize()/2;
		Image image = p.getImage(viewScale);
		g.setColor(Color.GREEN);
		// draw projectile relative to view direction
		switch(viewDirection){
		case 1:
			g.drawImage(image, drawX+(p.getY()*viewScale)-(viewScale*pSize), 
					drawY+((width-p.getX())*viewScale)-(viewScale*pSize), c);
			break;
		case 2:
			g.drawImage(image, drawX+((width-p.getX())*viewScale)-(viewScale*pSize), 
					drawY+((height-p.getY())*viewScale)-(viewScale*pSize), c);
			break;
		case 3:
			g.drawImage(image, drawX+((height-p.getY())*viewScale)-(viewScale*pSize), 
					drawY+(p.getX()*viewScale)-(viewScale*pSize), c);
			break;
		default:
			g.drawImage(image, drawX+(p.getX()*viewScale)-(viewScale*pSize), 
					drawY+(p.getY()*viewScale)-(viewScale*pSize), c);
		}
	}
	
	/**
	 * Sets the current row of the players for drawing relative to
	 * the view direction.
	 * @param viewDirection The direction the room is being viewed in.
	 */
	private void checkPlayers(int viewDirection) {
		switch(viewDirection){
		case 1: // EAST
			for (Player p : getAllCharacters()){
				p.setRow(rowFromY(width-p.getX()));
			}
			break;
		case 2: // SOUTH
			for (Player p : getAllCharacters()){
				p.setRow(rowFromY(height-p.getY()));
			}
			break;
		case 3: // WEST
			for (Player p : getAllCharacters()){
				p.setRow(rowFromY(p.getX()));
			}
			break;
		case 0: default: // DEFAULT TO NORTH
			for (Player p : getAllCharacters()){
				p.setRow(rowFromY(p.getY()));
			}
			break;
		}
	}
	
	/**
	 * Sets the current row of the projectiles for drawing relative to
	 * the view direction.
	 * @param viewDirection The direction the room is being viewed in.
	 */
	private void checkProjectiles(int viewDirection, Set<Projectile> toDraw) {
		switch(viewDirection){
		case 1: // EAST
			for (Projectile p : toDraw){
				p.setRow(rowFromY(width-p.getX()));
			}
			break;
		case 2: // SOUTH
			for (Projectile p : toDraw){
				p.setRow(rowFromY(height-p.getY()));
			}
			break;
		case 3: // WEST
			for (Projectile p : toDraw){
				p.setRow(rowFromY(p.getX()));
			}
			break;
		case 0: default: // DEFAULT TO NORTH
			for (Projectile p : toDraw){
				p.setRow(rowFromY(p.getY()));
			}
			break;
		}
	}
	
	/**
	 * Converts an x position to a column value.
	 * @param x The pixel x position
	 * @return The column value of the x position.
	 */
	public int colFromX(int x){
		double xCol = (double)x/(double)squareSize;
		if (xCol > contents.length-1){
			return contents.length-1;
		}
		return (int)xCol;
	}
	
	/**
	 * Converts a y position to a row value.
	 * @param y The pixel y position
	 * @return The row value of the y position.
	 */
	public int rowFromY(int y){
		double yRow = (double)y/(double)squareSize;
		if (yRow > contents[0].length-1){
			return contents[0].length-1;
		}
		return (int)yRow;
	}

	/** 
	* Create a clone of the contents array which has then been
	* rotated 90 degrees clockwise
	* @return A clone of this.contents which has been rotated 90
	* degrees clockwise.
	*/
	private Item[][] rotatedArrayClockwise(Item[][] contents){
		int rows = contents.length;
		int cols = contents[0].length;
		
		 // Make a new array where width and height are swtiched
		Item[][] rotate = new Item[cols][rows];
		
		// Rows and cols for rotated array
		int rRows = cols;
		int rCols = rows;
		
		// Rotates 90 clockwise
		for(int r=0; r<rRows; r++){
			for(int c=0; c<rCols; c++){
				rotate[r][c] = contents[rCols-1-c][r];
			}
		}
		
		return rotate;
	}
	
	/** 
	* Create a clone of the contents array which has then been
	* rotated 90 degrees anti-clockwise
	* @return A clone of this.contents which has been rotated 90
	* degrees anti-clockwise.
	*/
	private Item[][] rotatedArrayAntiClockwise(Item[][] contents){
		int rows = contents.length;
		int cols = contents[0].length;

		// Makes a new array where width and height are swtiched
		Item[][] rotate = new Item[cols][rows];
		
		// Rows and cols for rotated array
		int rRows = cols;
		int rCols = rows;
		
		// Rotates 90 anticlockwise
		for(int r=0; r<rRows; r++){
			for(int c=0; c<rCols; c++){
				rotate[r][c] = contents[c][rRows-1-r];
			}
		}
		
		return rotate;
	}
	
	/** 
	* Create a clone of the contents array which has then been
	* rotated 180 degrees.
	* @return A clone of this.contents which has been rotated 180
	* degrees
	*/
	private Item[][] rotatedArray180(Item[][] contents){
		int rows = contents.length;
		int cols = contents[0].length;

		// Makes a new array where width and height are swtiched
		Item[][] rotate = new Item[rows][cols];
		
		//Rotates the image
        for(int r=0; r<rows; r++){
            for(int c=0; c<cols; c++){
                rotate[r][c] = contents[rows-1-r][cols-1-c];
            }
        }
		
		return rotate;
	}

	/**
	 * Adds a player to this room.
	 * @param player The player to add
	 */
	public void addPlayer(Player player) {
		players.add(player);
	}
	
	/**
	 * Removes a player from the room.
	 * @param player The player to remove
	 */
	public void removePlayer(Player player){
		players.remove(player);
		
		//If this is the last player in the room,
		//clean up projectiles
		if (players.size() == 0){
			projectiles.removeAll(projectiles);
		}
	}
	
	/**
	 * 
	 * @param npc
	 */
	public void addNPC(NonPlayer npc){
		npcs.add(npc);
	}
	
	/**
	 * 
	 * @param npc
	 */
	public void removeNPC(NonPlayer npc){
		npcs.remove(npc);
	}
	
	/**
	 * Adds a projectile to this room.
	 * @param p The projectile to add
	 */
	public void addProjectile(Projectile p){
		if (players.contains(p.getPlayer()) || npcs.contains(p.getPlayer())){
			projectiles.add(p);
		}
	}
	
	/**
	 * Removes a projectile from this room
	 * @param p The projectile to remove
	 */
	public void removeProjectile(Projectile p){
		projectiles.remove(p);
	}

	/**
	 * Get the item at a given (x,y) co-ordinate.
	 * @param x The x position of the item
	 * @param y The y position of the item
	 * @return The Item at position (x,y)
	 */
	public Item itemAt(int x, int y) {
		//Bounds check
		int colX = colFromX(x);
		int colY = rowFromY(y);
		
		//If it's outside of the room's contents, treat it as a wall
		if (colX < 0 || colY < 0 || colX >= cols || colY >= rows){
			return new Wall();
		};
		
		return contents[colX][colY];
	}
	
	/**
	 * Get the item at the given x,y mouse coordinates
	 * converts mouse x,y to room x,y. 
	 * then returns the item at those coordinates
	 * @param x the X position relative to the player
	 * @param y The Y position relative to the player
	 * @param p the current player
	 * @return the item
	 */
	public Item itemAtMouse(int x, int y, int viewScale, Player p){
		int newX;
		int newY;
		switch(p.getViewDirection()){
		case 1:
			newX = (int)((double)((yOrigin+(width*viewScale))-y)/(double)viewScale);
			newY = (int)((double)(x-xOrigin)/(double)viewScale);
			break;
		case 2:
			newX = (int)((double)((xOrigin+(width*viewScale))-x)/(double)viewScale);
			newY = (int)((double)((yOrigin+(height*viewScale))-y)/(double)viewScale);
			break;
		case 3:
			newX = (int)((double)(y-yOrigin)/(double)viewScale);
			newY = (int)((double)((xOrigin+(height*viewScale))-x)/(double)viewScale);
			break;
		default: 
			newX = (int)((double)(x-xOrigin)/(double)viewScale);
			newY = (int)((double)(y-yOrigin)/(double)viewScale);
		}
		int xDiff = newX-p.getX();
		int yDiff = newY-p.getY();
		if (Math.abs(xDiff) < 48 && Math.abs(yDiff) < 48){
			return itemAt(newX, newY);
		}
		return new Wall();
	}
	
	/**
	 * Gets the array of the unscaled background and foreground images for this room
	 * @return The array of room images
	 */
	public Image[][] getImages() {
		return images;
	}
	
	/**
	 * sets the array of scaled background and foregrouns images for this room
	 * @param newImages The new array of images to set
	 */
	public void setScaledImages(Image[][] newImages){
		scaledImages = newImages;
	}

	/**
	 * Gets the array of Items contained in this room
	 * @return the contents array
	 */
	public Item[][] getContents() {
		return contents.clone();
	}

	//FIXME: Do we want to hand back the actual collection, or a copy?
	/**
	 * Gets the Set of players in this room
	 * @return The Set of players
	 */
	public Set<Player> getPlayers() {
		return players;
	}
	
	/**
	 * Gets all players and non-player characters
	 * in this room
	 * @return
	 */
	public Set<Player> getAllCharacters() {
		Set<Player> rtn = new HashSet<Player>();
		rtn.addAll(players);
		rtn.addAll(npcs);
		
		return rtn;
	}

	/**
	 * Updates the room and all contained items
	 */
	public void update() {
		//Update the projectiles
		Iterator<Projectile> projectileIter = projectiles.iterator();
		Iterator<Player> playerIter = players.iterator();
		Iterator<NonPlayer> npcIter = npcs.iterator();
		
		try{
			while(projectileIter.hasNext()){
				Projectile p = projectileIter.next();
				
				p.update();
				// check if projectile is outside room bounds
				if (p.getX() < 0 || p.getX() > width || p.getY() < 0 || p.getY() > height){
					p.setActive(false);
				}
				
				//Remove the projectile if it's inactive
				if (!p.isActive()){
					projectileIter.remove();
				}
			}
		} catch (ConcurrentModificationException e){
			//If we get an exception, try again
			//FIXME: Dirty as fuck. 
			update();
			return;
		}
		
		
		while(playerIter.hasNext()){
			Player p = playerIter.next();
			
			//Player is dead
			if (p.getHealth() < 0){
				playerIter.remove(); //Make the player invisible
				
				//Schedule a respawn event
				//TODO: Change this to respawn somewhere that isn't the tile they died on
				Event respawn = new RespawnEvent(p, this, p.getX(), p.getY());
				GameClock.getInstance().scheduleEvent(respawn , Player.RESPAWN_TIME);
			}
		}
		
		while(npcIter.hasNext()){
			NonPlayer npc = npcIter.next();
			npc.update();
			
			if (npc.getHealth() < 0){
				npcIter.remove();
			}
		}
	}
	
	/**
	 * Gets the Set of projectiles in this room
	 * @return the Set of projectiles
	 */
	public Set<Projectile> getProjectiles() {
		return projectiles;
	}
	
	public String getName(){
		return name;
	}
	
	public String getDescription() {
		return description;
	}

	public int getCols(){
		return cols;
	}
	
	public int getRows(){
		return rows;
	}
}
