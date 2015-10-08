package gameWorld.gameObjects.weapons;

import gameWorld.Controller;
import gameWorld.characters.Player;
import gameWorld.gameObjects.weapons.projectiles.PaintBall;
import gameWorld.gameObjects.weapons.projectiles.Projectile;

import java.awt.Image;

public class PaintballGun extends Weapon {
	public static final double BULLET_SPREAD = 10;
	private String description;
	
	public PaintballGun(){
		super(4, new PaintBall());
		this.description = "A basic paintball gun";
	}
	
	@Override
	public Projectile fire(Player p, double theta){
		double spread = Math.toRadians((Math.random()*BULLET_SPREAD)-BULLET_SPREAD/2);
		return super.fire(p, theta+spread);
	}

	@Override
	public void use(Player p, Controller ctrl) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getImage(int viewDirection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int xOffset(int viewDirection) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int yOffset(int viewDirection) {
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

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public WeaponType getWeaponType() {
		return WeaponType.PaintballGun;
	}

}
