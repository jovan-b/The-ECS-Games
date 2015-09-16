package gameObjects;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.GUICanvas;

/**
 * Represents an area in the game. Holds contents relevant to that area and
 * is responsible for drawing itself and everything in it.
 * @author Sarah Dobie, Chris Read
 *
 */
public class Room implements Drawable {
	private Image[][] images;
	private Item[][] contents; // items in the room
	private int cols = 12; //FIXME temporarily hardcoded for testing
	private int rows = 14;
	
	public Room(String roomName){
		images = new Image[4][4];
		contents = new Item[cols][rows];
		try {
			images[0][0] = ImageIO.read(new File("Resources"+File.separator+roomName+File.separator+"NorthBase.png"));
			images[0][1] = ImageIO.read(new File("Resources"+File.separator+roomName+File.separator+"NorthTop.png"));
			
			images[1][0] = ImageIO.read(new File("Resources"+File.separator+roomName+File.separator+"EastBase.png"));
			images[1][1] = ImageIO.read(new File("Resources"+File.separator+roomName+File.separator+"EastTop.png"));
			
			images[2][0] = ImageIO.read(new File("Resources"+File.separator+roomName+File.separator+"SouthBase.png"));
			images[2][1] = ImageIO.read(new File("Resources"+File.separator+roomName+File.separator+"SouthTop.png"));
			
			images[3][0] = ImageIO.read(new File("Resources"+File.separator+roomName+File.separator+"WestBase.png"));
			images[3][1] = ImageIO.read(new File("Resources"+File.separator+roomName+File.separator+"WestTop.png"));
		} catch (IOException e) {
			System.out.println("Error loading room images: "+e.getMessage());
		}
	}

	public void draw(Graphics g, GUICanvas c){
		int viewDirection = 3; //TODO get from player view direction (0=North, 1=East, 2=South, 3=West)
		int squareSize = 24; //TODO get this value from player view scale
		int playerX = cols*12; //TODO replace these with player coordinates in room
		int playerY = rows*12; //TODO replace these with player coordinates in room
		
		int drawX;
		int drawY;
		
		switch(viewDirection){
			case 1: // EAST
				drawX = (c.getWidth()/2)-playerY;
				drawY = (c.getHeight()/2)-((cols*24)-playerX)-(squareSize*2);
				break;
			case 2: // SOUTH
				drawX = (c.getWidth()/2)-((cols*24)-playerX);
				drawY = (c.getHeight()/2)-((rows*24)-playerY)-(squareSize*2);
				break;
			case 3: // WEST
				drawX = (c.getWidth()/2)-((rows*24)-playerY);
				drawY = (c.getHeight()/2)-playerX-(squareSize*2);
				break;
			case 0: default: // DEFAULT TO NORTH
				drawX = (c.getWidth()/2)-playerX;
				drawY = (c.getHeight()/2)-playerY-(squareSize*2);
				break;
		}
		
		g.drawImage(images[viewDirection][0], drawX, drawY, c);
		g.drawImage(images[viewDirection][1], drawX, drawY, c);
		for(int col=0; col<cols; col++){
			for(int row=0; row<rows; row++){
				if(contents[col][row] != null){
					contents[col][row].draw(g, c);
				}
			}
		}
	}
	
	/** 
	* Create a clone of the contents array which has then been
	* rotated 90 degrees clockwise
	* @return A clone of this.contents which has been rotated 90
	* degrees clockwise.
	*/
	public Item[][] rotatedArrayClockwise(){
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
	public Item[][] rotatedArrayAntiClockwise(){
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
}
