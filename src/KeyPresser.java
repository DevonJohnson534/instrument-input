import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;

public class KeyPresser {
	private Robot robot;
	private mouseMover mouse;
	private HashMap<String, Integer> keybinds; //note name, keycode or mouse action number
	private final Map<Integer, String> mouseActionNames = Map.of(
			65500, "MOUSE UP",
			65501, "MOUSE DOWN",
			65502, "MOUSE LEFT",
			65503, "MOUSE RIGHT",
			65504, "LEFT CLICK",
			65505, "MIDDLE CLICK",
			65506, "RIGHT CLICK",
			65507, "SCROLL UP",
			65508, "SCROLL DOWN"
		);
	private int mouseXspeed;
	private int mouseYspeed;
	private int scrollAmount;
	
	public KeyPresser() {
		keybinds = Main.slHandler.getKeybinds();//should reference instead of copy, need to check. TODO
		mouseXspeed = 1; //TODO make these and the line settings save between sessions and also display initial value
		mouseYspeed = 1;
		scrollAmount = 1;
        try {
			robot = new Robot();
		} catch (AWTException e) {
			System.err.println("Something went wrong with setting up the keystroke emulator. Make sure you have a keyboard connected to your computer.");
			e.printStackTrace();
		}
		mouse = new mouseMover(robot);
	}
	
	public void pressKey(String noteName) {
		Integer keycode = keybinds.get(noteName);
		if (keycode == null) {
			return;
		}
		if (keycode < 65500) {
			robot.keyPress(keycode);
		}
		else {
			mouseAction(keycode); //TODO holding mouse move button continuously moves. probably need a mouse moving class?
		}
	}
	
	public void releaseKey(String noteName) {
		Integer keycode = keybinds.get(noteName);
		if (keycode == null) {
			return;
		}
		if (keycode < 65500) {
			robot.keyRelease(keycode);
		}
		else {
			mouseButtonRelease(keycode);
		}
	}
	
	//MOUSE
	
	private int[] getMousePos() {
		PointerInfo info = MouseInfo.getPointerInfo();
		Point p = info.getLocation();
		int x = (int) p.getX();
		int y = (int) p.getY();
		
		return new int[] {x,y};
	}
	
	private void mouseAction(int actionNum) {
		//int[] pos = getMousePos();
		switch (actionNum){
			//MOVE MOUSE==========
			case 65500:	//move mouse up
				mouse.move(mouseYspeed, new int[]{0,-2});
				//robot.mouseMove(pos[0],pos[1]-mouseYspeed);
				break;
			case 65501:	//move mouse down
				mouse.move(mouseYspeed, new int[]{0,1});
				//robot.mouseMove(pos[0],pos[1]+mouseYspeed);
				break;
			case 65502:	//move mouse left
				mouse.move(mouseXspeed, new int[]{-2,0});
				//robot.mouseMove(pos[0]-mouseXspeed,pos[1]);
				break;
			case 65503:	//move mouse right
				mouse.move(mouseXspeed, new int[]{2,0});
				//robot.mouseMove(pos[0]+mouseXspeed,pos[1]);
				break;
			//CLICKS==============
			case 65504: //left click
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				break;
			case 65505: //mid click
				robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
				break;
			case 65506: //right click
				robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
				break;
			//SCROLL WHEEL========
			case 65507: //scroll up
				robot.mouseWheel(-scrollAmount);
				break;
			case 65508: //scroll down
				robot.mouseWheel(scrollAmount);
				break;
		}
	}
	private void mouseButtonRelease(int actionNum) {
		switch (actionNum){
			case 65500: case 65501: case 65502: case 65503:
				mouse.stopMoving();
				break;
			case 65504: //left click
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				break;
			case 65505: //mid click
				robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
				break;
			case 65506: //right click
				robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
				break;
		}
	}
	
	//GET SET ===================================================
	public String getMouseActionName(int actionNum) {
		return mouseActionNames.get(actionNum);
	}
	public void setXspeed(int speed) {
		mouseXspeed = speed;
	}
	public void setYspeed(int speed) {
		mouseYspeed = speed;
	}
	public void setScrollAmount(int amount) {
		scrollAmount = amount;
	}
}














