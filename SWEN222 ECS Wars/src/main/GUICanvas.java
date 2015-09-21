package main;

import gameObjects.Room;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import characters.Player;
import characters.TestPlayer;

/**
 * The main canvas inside the game window in which the game is drawn.
 * 
 * @author Sarah Dobie, Chris Read
 *
 */
@SuppressWarnings("serial")
public class GUICanvas extends JComponent{
	
	private GUIFrame frame;
	
	private Player player;
	
	public GUICanvas(GUIFrame frame, Player player){
		this.frame = frame;
		this.player = player;
	}
	
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
	}
	
	@Override
	public Dimension getPreferredSize(){
		return new Dimension(GUIFrame.INIT_WIDTH, GUIFrame.INIT_HEIGHT);
	}
	
	/**
	 * Draws all the graphics on the screen
	 * Mainly calls each objects own draw method
	 */
	@Override
	public void paint(Graphics g){
		//paint background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		Room r = ((TestPlayer) player).getCurrentRoom();
		r.draw(g, this, player);
		drawHUD(g, this, r);
	}

	private void drawHUD(Graphics g, GUICanvas guiCanvas, Room r) {
		// TODO Auto-generated method stub
	}
}