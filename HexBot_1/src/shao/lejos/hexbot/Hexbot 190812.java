package shao.lejos.hexbot;

import java.io.IOException;
import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTCommConnector;
import lejos.nxt.comm.RS485;
import lejos.nxt.remote.RemoteMotor;
import lejos.nxt.remote.RemoteNXT;
import lejos.util.TextMenu;

public class Hexbot implements ButtonListener {
	
	Leg L1, R1, L2, R2, L3, R3;
	Leg[] legs;
	Leg[] tripod_l;
	Leg[] tripod_2;
	
	int pace = 60;
	int rem = 360 - pace;
	int speed = 120;
	
	
	robotState state_standing = new robotState(0, 0, 0, 0, 0, 0);
	robotState state_walk_right = new robotState(rem, 0, rem, 0, rem, 0);
	robotState state_walk_left = new robotState(0, rem, 0, rem, 0, rem);
	
	robotState current_state;
	
	static RemoteNXT nxt = null;	
	boolean connected = false;
	
	public static void main(String[] args) throws Exception {
		//this is OOP, remmember? let's make an instance of Hexbot and call run on it...
		Hexbot thisHexbot = new Hexbot();
		thisHexbot.run();		
	}
		
	public void run() throws Exception {
		Button.ESCAPE.addButtonListener(this);		 

        // Now connect
        try {
            LCD.clear();
            LCD.drawString("Connecting...",0,0);
        	nxt = new RemoteNXT("NXT", RS485.getConnector());
        	LCD.clear();
            LCD.drawString("Connected",0,1);
            connected = true;
            Thread.sleep(500);
        } catch (IOException ioe) {
        	LCD.clear();
            LCD.drawString("Conn Failed",0,0);
            Thread.sleep(2000);
            System.exit(1);
        }

        LCD.clear();
		
		R1 = new Leg("R1", true, false, null, nxt.A);
		L2 = new Leg("L2", true, true, null, nxt.B);		
		R3 = new Leg("R3", true, true, null, nxt.C);
		
		L1 = new Leg("L1", false, false, Motor.A, null);
		R2 = new Leg("L2", false, true, Motor.B, null); 
		L3 = new Leg("R3", false, true, Motor.C, null);
		
		legs = new Leg[]{L1, L2, L3, R1, R2, R3};
		tripod_l = new Leg[]{L1, R2, L3};
		tripod_2 = new Leg[]{R1, L2, R3};
		
		ScreenUpdater su = new ScreenUpdater(this);
		Thread thr = new Thread(su);
		thr.start();
		
		
		for (Leg l : legs) {
			l.setSpeed(100);
		}
		
		Button.waitForAnyPress();
		
		stand_up();
		
		Button.waitForAnyPress();
		
		//int pace = 45;
		//setLegs(pace);
		setState(state_walk_right);
		Button.waitForAnyPress();
		
		while (true) {
			//walk_next_step(60);
			turn(60);
		}
	}

	public void stand_up() {

		for (Leg l : legs) {
			l.rotate(225, true);		
		}										

		waitForLegs();
		
		current_state = state_standing;

		for (Leg l : legs) {
			l.resetTachoCount();		
		}	
		
	}
	
	public void setState(robotState state) {
		L1.rotateTo(state._L1, true);
		L2.rotateTo(state._L2, true);
		L3.rotateTo(state._L3, true);
		R1.rotateTo(state._R1, true);
		R2.rotateTo(state._R2, true);
		R3.rotateTo(state._R3, true);		
		waitForLegs();
		current_state = state;
		
	}
		
	public void walk_next_step(int overlap) {
		
		int rem = 360 - overlap;
		
		if (current_state == state_walk_left) {
			walk_left_step(overlap, rem);
		} else if (current_state == state_walk_right) {
			walk_right_step(overlap, rem);			
		} else {
			//Pick at random? Pick nearest to current leg state?
			setState(state_walk_left);
			walk_left_step(overlap, rem);
		}	}
	
	void walk_left_step(int overlap, int rem) {
		
		if (current_state != state_walk_left) {
			setState(state_walk_left);
		}

		for (Leg l : tripod_2) {
			l.rotate(overlap, true);
		}
		
		for (Leg l : tripod_l) {
			l.rotate(rem, true);
		}
		
		
		waitForLegs();
		
		current_state = state_walk_right;
	}
	
	void walk_right_step(int overlap, int rem) {
		
		if (current_state != state_walk_right) {
			setState(state_walk_right);
		}
		
		for (Leg l : tripod_2) {
			l.rotate(rem, true);
		}
		

		for (Leg l : tripod_l) {
			l.rotate(overlap, true);
		}

		waitForLegs();
		
		current_state = state_walk_left;
	}
	
	
	public void turn(int overlap) {
		
		int rem = 360 - overlap;
		
		L1.rotate(rem, true);
		L2.rotate(overlap, true);
		L3.rotate(rem, true);
		
		R1.rotate(-overlap, true);
		R2.rotate(-rem, true);	
		R3.rotate(-overlap, true);
		
		Button.waitForAnyPress();
		
		L1.rotate(overlap, true);
		L2.rotate(rem, true);
		L3.rotate(overlap, true);
		
		R1.rotate(-rem, true);
		R2.rotate(-overlap, true);	
		R3.rotate(-rem, true);
						
		Button.waitForAnyPress();
	}
	
	public void waitForLegs() {
		for (Leg l : legs) {
			l.waitComplete();
		}
	}
	
	public int calculateRelativeSpeeds(int duty_factor, int duty_angle) {
		//we need to duty_angle/360 degrees in duty_factor/time
		int time = speed/360;
		int duty_speed_factor = ((duty_angle/360)/(duty_factor/time));
		int rest_speed_factor = (((360 - duty_angle)/360)/((1-duty_factor)/time));
				
				
		return null;
		
		
	}
	
	//Button listener for kill

	@Override
 	public void buttonPressed(Button btn) {
		if (btn.getId() == 8) {
			if (connected) {
				nxt.close();		
			}
			System.exit(0);
		}
	}
	@Override
	public void buttonReleased(Button arg0) {
		// TODO Auto-generated method stub
		
	}
}


class robotState {
	
	int _L1, _L2, _L3, _R1, _R2, _R3;	
	
	robotState(int L1, int L2, int L3, int R1, int R2, int R3) {
		_L1 = L1;
		_L2 = L2;
		_L3 = L3;
		_R1 = R1;
		_R2 = R2;
		_R3 = R3;
	}

}

class ScreenUpdater implements Runnable{
	
	Hexbot thisHexbot;	
	
	public ScreenUpdater(Hexbot hexbot) {
		thisHexbot = hexbot;
	}
	
	@Override
	public void run() {
		while (true) {
			LCD.clear();
			for (int i = 0; i < 6; i++) {
				String str = thisHexbot.legs[i]._name + ":" + thisHexbot.legs[i].getPos(); 
				LCD.drawString(str, 0, i);	
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
