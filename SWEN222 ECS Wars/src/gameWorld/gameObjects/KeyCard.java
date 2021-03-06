package gameWorld.gameObjects;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import gameWorld.Controller;
import gameWorld.characters.Player;

/**
 * A key card that allows the player to open certain in-game doors.
 * 
 * @author Sarah Dobie 300315033
 * @author Chris Read 300254724
 *
 */
public class KeyCard implements Item {

	private Image image;
	private Image scaledImage;
	private String description;
	
	/**
	 * Constructor for class KeyCard.
	 */
	public KeyCard(){
		description = "Key Card: Allows access to locked rooms.";
		loadImages();
	}

	/**
	 * Parse and store all required images.
	 */
	private void loadImages() {
		try{
			image = ImageIO.read(KeyCard.class.getResource("/Items/IDCard.png"));
			scaledImage = image;
		} catch(IOException e){
			System.out.println("Error loading KeyCard file: "+e.getMessage());
		}
	}
	
	@Override
	public void use(Player p, Controller ctrl) {
	}

	@Override
	public Image getImage(int viewDirection) {
		return image;
	}

	@Override
	public boolean canWalk() {
		return true;
	}

	@Override
	public int yOffset(int viewDirection) {
		return 0;
	}

	@Override
	public int xOffset(int viewDirection) {
		return 0;
	}

	@Override
	public void setScaledImage(int viewDirection, Image scaledImage) {
		this.scaledImage = scaledImage;
	}

	@Override
	public Image getScaledImage(int viewDirection) {
		return scaledImage;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public Type getType() {
		return Type.KeyCard;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Item){
			return this.getType() == ((Item) o).getType();
		}
		return false;
	}

}
