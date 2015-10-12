package tests;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import gameWorld.Controller;
import gui.GUIFrame;

/**
 * A controller class that does the bare minimum to exist.
 * Exists solely for testing purposes.
 * @author Sarah Dobie 300315033
 *
 */
public class TestController extends Controller {

	public TestController(int uid) {
		super(uid);
		gui = new GUIFrame();
	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {}
	
}
