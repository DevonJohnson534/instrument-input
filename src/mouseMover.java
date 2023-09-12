import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;

public class mouseMover implements Runnable{
	private Robot robot;
	private boolean isRunning;
	
	private int speed;
	private int[] direction;

	public mouseMover(Robot robot) {
		this.robot = robot;
	}
	
	public void move(int speed, int[] direction) { //direction is x,y with only zeros, ones, or negative ones
		this.speed = speed;
		//speed = 10;
		this.direction = direction;
		isRunning = true;
		new Thread(this).start();
	}
	
	public void stopMoving() {
		isRunning = false;
	}
	
	private int[] getMousePos() {
		PointerInfo info = MouseInfo.getPointerInfo();
		Point p = info.getLocation();
		int x = (int) p.getX();
		int y = (int) p.getY();
		
		return new int[] {x,y};
	}

	@Override
	public void run() {
		while (isRunning) {
			int[] pos = getMousePos();
			robot.mouseMove(pos[0]+direction[0],pos[1]+direction[1]);
			try {
				Thread.sleep(speed);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


}
