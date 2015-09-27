package characters.nonplayer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import gameObjects.Room;
import gameObjects.weapons.PaintballGun;
import characters.Player;
import characters.nonplayer.strategy.NonPlayerStrategy;
import characters.nonplayer.strategy.WaitStrategy;

public class NonPlayer extends Player {
	public static final NonPlayerStrategy GLOBAL_DEFAULT = new WaitStrategy();
	
	/**
	 * An enum to describe what events non-players respond to
	 * @author Carl
	 *
	 */
	public enum Events {
		COMBAT,
		DEATH,
		DEFAULT
	}
	
	protected Map<Events, NonPlayerStrategy> strategies;
	protected NonPlayerStrategy active;

	public NonPlayer(Room room, int posX, int posY, NonPlayerStrategy initial) {
		super(room, posX, posY);
		active = initial;
		strategies = new HashMap<Events, NonPlayerStrategy>();
		
		this.currentWeapon = new PaintballGun();
		
		this.setStrategy(Events.DEFAULT, initial);
		
		// Load sprites
		sprites = new Image[4][3];
		try {
			for (int dir = 0; dir < 4; dir++){
				for (int ani = 0; ani < 3; ani++){
					sprites[dir][ani] = ImageIO.read(new File("Resources"+File.separator+"Players"+File.separator+"Dave"+dir+ani+".png"));
				}
			}
		} catch (IOException e) {
			System.out.println("Error loading player images: " + e.getMessage());
		}
		scaledSprites = sprites;
	}
	
	@Override
	public void update(){
		active.update();
	}
	
	@Override
	public void modifyHealth(int amt){
		if (amt < 0){this.respond(Events.COMBAT);}
		super.modifyHealth(amt);
		if (health < 0){this.respond(Events.DEATH);}
	}
	
	@Override
	public void shoot(int x, int y) {
		double theta = Player.angleBetweenPlayerAndMouse(this.getX(), this.getY(),
				x, y);
		
		//Correct theta based on view direction
		theta += Math.toRadians(90)*viewDirection;
		currentRoom.addProjectile(currentWeapon.fire(this, theta));
	}
	
	@Override
	public void setCurrentRoom(Room newRoom, int newX, int newY) {
		currentRoom.removeNPC(this);
		newRoom.addNPC(this);
		this.currentRoom = newRoom;
	}
	
	/**
	 * Causes the npc to respond to a specific type of event
	 * @param event
	 */
	public void respond(Events event){
		NonPlayerStrategy strat = strategies.get(event);
		if (strat == null){
			strat = strategies.get(Events.DEFAULT) != null ? strategies.get(Events.DEFAULT) : GLOBAL_DEFAULT;
		}

		active = strat;
		active.initialize();
	}
	
	/**
	 * Sets which strategy the NPC will use for an event
	 * @param event
	 * @param strategy
	 */
	public void setStrategy(Events event, NonPlayerStrategy strategy){
		strategies.put(event, strategy);
		strategy.setNPCReference(this);
	}
	
}