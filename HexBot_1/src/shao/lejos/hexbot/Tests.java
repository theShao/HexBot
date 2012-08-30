package shao.lejos.hexbot;

import java.io.IOException;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTCommConnector;
import lejos.nxt.comm.RS485;
import lejos.nxt.remote.RemoteMotor;
import lejos.nxt.remote.RemoteNXT;
import lejos.util.TextMenu;

public class Tests {
	
	static Leg L1 = new Leg("L1", false, true, Motor.A, null);
	
	public static void main(String[] args) throws Exception {
	    	L1.resetTachoCount();	
		updateScreen();
		Button.waitForAnyPress();
		L1.rotateTo(90, false);
		updateScreen();
		Button.waitForAnyPress();
		L1.rotateTo(0, false);
		updateScreen();
		Button.waitForAnyPress();
		L1.rotateTo(-90, false);
		updateScreen();
		Button.waitForAnyPress();
		L1.rotateTo(120, false);
		updateScreen();
		Button.waitForAnyPress();
		L1.rotateTo(350, false);
		updateScreen();
		Button.waitForAnyPress();
		L1.rotateTo(10, false);
		updateScreen();
		Button.waitForAnyPress();
		
	}
	
	static void updateScreen() {
		LCD.clear();
		LCD.drawInt(L1.getTachoCount(), 0, 1);
		LCD.drawInt(L1.getPos(), 0, 2);
	}

}

