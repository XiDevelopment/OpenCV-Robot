package at.int3ro.robot.controller;

import android.content.Context;
import android.widget.Toast;

public class MoveFacade {
	private static MoveFacade instance = null;
	
	public static MoveFacade getInstance() {
		if(instance == null)
			instance = new MoveFacade();
		
		return instance;
	}
	
	private BasicMovement basicMovement = BasicMovement.getInstance();
	
	Context context = null;
	
	public void setContext(Context context) {
		this.context = context;
		basicMovement.SetContext(this.context);		
	}
	
	public void close() {
		basicMovement.disconnect();
	}
	
	public void move(int time) {
		basicMovement.moveForward();
		
		try {
			Thread.sleep(time*1000);
		} catch (InterruptedException e) {

		}
		
		basicMovement.stop();
	}
}
