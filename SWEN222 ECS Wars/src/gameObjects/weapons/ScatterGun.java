package gameObjects.weapons;

import java.awt.Image;

import characters.Player;
import gameObjects.weapons.projectiles.Projectile;
import gameObjects.weapons.projectiles.RubberBullet;

public class ScatterGun extends Weapon{
	public final int MAX_SHOTS = 5;
	public final double MAX_SPREAD = Math.toRadians(30); //Maximum spread in degrees 

	public ScatterGun() {
		super(10, new RubberBullet());
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Projectile fire(Player p, double theta){
		if (canFire){
			int max = (int)(Math.random()*MAX_SHOTS)+1;
			for (int i = 0; i < max; i++){
				double t = theta+(Math.random()*MAX_SPREAD - MAX_SPREAD/2);
				p.getCurrentRoom().addProjectile(projectile.newInstance(p, t));
			}
		}
		
		return super.fire(p, theta);
	}

	@Override
	public void use(Player p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Image getImage(int viewDirection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int yOffset(int viewDirection) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int xOffset(int viewDirection) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setScaledImage(int viewDirection, Image scaledImage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Image getScaledImage(int viewDirection) {
		// TODO Auto-generated method stub
		return null;
	}

}