package shao.lejos.hexbot;

import java.io.IOException;
import lejos.nxt.comm.RConsole;
import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.comm.RS485;
import lejos.nxt.remote.RemoteNXT;
import lejos.robotics.subsumption.Behavior;

public class Hexbot implements ButtonListener {

	Leg L1, R1, L2, R2, L3, R3;
	Leg[] legs;
	
	//constants for this design of leg
	int dutyAngleOffset = 110; 
	int dutyAngle = 90;

	double dutyPhaseFactor = 0.65; //0-1
	
	int targetAverageSpeed = 80; // deg/s
	int cycleTime = (360/targetAverageSpeed) * 1000; //ms

	static RemoteNXT nxt = null;	
	boolean connected = false;
	
	//int angle_highest_point = 
	
	boolean go = true;
	
	//duty Angles - ought to be 2*180-duty angle offset, but using non-symetrical legs....
	double[] da = new double[]{65, 65, 65, 65, 65, 65};
	//duty agle offsets
	double[] dao = new double[]{135, 135, 135, 135, 135, 135};
	//duty time offset
	double[] dto = new double[]{0, 0.5, 0, 0.5, 0, 0.5};
	//duty time factors
	double[] dtf = new double[]{0.6, 0.6, 0.6, 0.6, 0.6, 0.6};
	boolean[] reversed = new boolean[]{false, false, false, false, false, false};
	Gait alternating_tripod_forwards = new Gait(da, dao, dto, dtf, reversed);
	

	public static void main(String[] args) throws Exception {
		//this is OOP, remmember? let's make an instance of Hexbot and call run on it...
		Hexbot thisHexbot = new Hexbot();
		thisHexbot.run();		
	}

	public void run() throws Exception {
		//Add button listener to quit
		Button.ESCAPE.addButtonListener(this);
		Button.LEFT.addButtonListener(this);
		
		//Wait for possible console connection
		//RConsole.openAny(5000);
		
		//Connect to the remote nxt over RS485 - really no point making this any more abstract than it has to be
		connectToRemoteNxt();	
		LCD.clear();
		
		//Gait is a set of values that we can use to construct the correct clocks and assign the legs apropriately
		//dutyAngleOffset and DutyAngle are properties of the robot, not the gait? dutyAngle??
		
		//, dutyPhaseOffsets, dutyTimeFactors[], reversed[] 

		
		//Legs with remote motors
		L1 = new Leg("L1", true, false, null, nxt.A);
		R2 = new Leg("R2", true, true, null, nxt.B);		
		L3 = new Leg("L3", true, true, null, nxt.C);
		//legs with local motors
		R1 = new Leg("R1", false, false, Motor.A, null);
		L2 = new Leg("L2", false, true, Motor.B, null); 
		R3 = new Leg("R3", false, true, Motor.C, null);
		


		legs = new Leg[]{L1, L2, L3, R1, R2, R3};
		
		bindGaitToLegs(alternating_tripod_forwards, targetAverageSpeed);

		/*
		ScreenUpdater su = new ScreenUpdater(this);
		Thread thr = new Thread(su);
		thr.start();
		*/
		if (RConsole.isOpen()) {
			for (Leg l : legs) {
			//Calc some speed and angle pairs for each clock if debugging to check it looks sane
			
				for (int i = 0; i < cycleTime; i += 200) {					
					int angle = l.clock.timeToAngle(i);
					int speed = l.clock.timeToSpeed(i);
					
					String s = l._name + " - T: " + i + ",  Angle: " + angle + ", Speed: " + speed;
					RConsole.println(s);				
				}
			}
		}
		
		Button.waitForAnyPress();
		
		for (Leg l : legs) {
			l.resetTachoCount();
			//reasonable speed for moving leg to initial position...
			l.setSpeed(150);
			//get the starting position for this leg
			int angle = l.clock.timeToAngle(0);
			int speed = l.clock.timeToSpeed(0);
			if (RConsole.isOpen()) {
				RConsole.println("Leg: " + l._name + ", T: 0, angle = " + angle + ", Speed: " + speed);
			}
			l.rotateTo(angle, true);
			
		}
						
		Button.waitForAnyPress();		

		double startTime = (double)System.currentTimeMillis();
		while (true) {
			if (go) {
				
				for (Leg l : legs) {
					//Calculate current time offset
					int time = (int) ( ( ((System.currentTimeMillis () - startTime))) % cycleTime);
					//Get current leg poition
					int curPos = l.getPos();				 
					//plug current phase into this Leg's clock				
					int speed = l.clock.timeToSpeed(time);
					int angle = l.clock.timeToAngle(time);
					
					//Calculate how far off we are - t`his is how far we are away from modeled angle at this phase
					int diff;
					if (l.clock.reversed) {
						diff = curPos - angle;
					} else {
						diff = angle - curPos;
					}
	
					//prevent trivial corrections
					if (Math.abs(diff) > 5) {
						//we probably shouldn't be changing our speed if we're on the ground either if we want to maintain heading						
						// some code that does that here...
						
						// if we're more than half a turn out of sync, jump a cycle instead
						if (diff > 180) { diff = diff - 360;} else if (diff < -180) { diff = 360 + diff;}
						
						// naive, max speed correctioin = +- 180deg/sec
						speed += diff;
					} 
					if (RConsole.isOpen()) {
						int curSpeed = l.getSpeed();
						String s = l._name + ", T: " + time + ", CurPos:" + curPos + ", TargetPos = " + angle + ", CurSpeed: "+ curSpeed + ", TargetSpeed: " + speed + ", Modifier: " + diff;
						RConsole.println(s);
					}
					if (speed > 700) { speed = 700; }
					else if (speed < 1) { speed = 1; }
					l.setSpeed(speed);
					if (l.clock.reversed) {
						l.backward();
					} else {
						l.forward();
					}
				}
				
			} else {
				for (Leg l : legs) {
					
					/*
					 * How to stop gracefully?
					 * If leg in the air, take the route that doesn't involve paassing through duty angle
					 * if already in duty, take shortest distance to correct angle - use rotateTo?
					 * 
					 */

					//Get current leg poition
					int curPos = l.getPos();
					
					//plug current phase into this Leg's clock				
					int speed = 0; //ignore speed - if we're at the correct angle we don't want to move at all...
					int angle = l.clock.timeToAngle(0); //Set time to be zero so legs return to initial state for the gait
					
					if (Math.abs(curPos - l.clock.timeToAngle(0)) < 5) {
						//we're done here...
						l.stop();
					} else if ((curPos > l.clock.dutyAngleOffset) && (curPos < (l.clock.dutyAngleOffset + l.clock.dutyAngle))) {
							//We're in duty, just rotate to our start position for this stance
							l.rotateTo(l.clock.timeToAngle(0), true);
					} else {
						/*
						 * Get shortest distance to where we need to be, if it goes through stance angle go the other way
						 */
						
						//Calculate how far off we are - this is how far we are away from modeled angle at this phase						
						int diff;
						if (l.clock.reversed) {
							diff = curPos - angle;
						} else {
							diff = angle - curPos;
						}						
						if (diff > 0) {
							if ((curPos + diff) > 180) {
								diff = diff - 360;							
							}
						} else {
							if ((curPos + diff) < 180) {
								diff = 360 + diff;
							}
						}
						l.setSpeed(diff + 20);
						l.rotate(diff, true);
						
					}
				}
			}
		}

	}
	
	public void bindGaitToLegs(Gait g, int speed) {
		if (speed == 0) {
			/*
			 * Special case, set the legs to have zero target speed and target angle of the rest position for the target gait
			 * maybe
			 * 
			 * for now, do nothing at all...
			 */

			
			
		} else {
			for (int i = 0; i < 6; i++) {
				/*
				 * Make a clock based on params from the gait and the leg/bot characteristics
				 */
				BuehlerClockValues c = new BuehlerClockValues();
				c.dutyTimeOffset = (int) (g.dutyTimeOffsetFactors[i] * cycleTime); //What time int the cycle this clock starts, ie leg offset
				c.dutyTime = (int) (cycleTime * g.dutyTimeFactors[i]); //how many ms this leg needs to spend covering the duty angle
				c.cycleTime = (360/Math.abs(speed)) * 1000;				
				c.reversed = false; //(g.reversed[i] == (speed < 0)); //negative speed means run the gait backwards (A == B) = (A xnor B) 
				c.dutyAngleOffset = (int) g.dutyAngleOffsets[i]; //What anlgle forwards from vertical does the leg hit the ground at?
				dutyAngle = (int) g.dutyAngles[i]; //How many degreed of revolution does the leg stay in ground contact for?
				c.init();
				//give the leg it's clock
				legs[i].clock = c;
				
				
			}
		}
	}

	public void waitForLegs() {
		for (Leg l : legs) {
			l.waitComplete();
		}
	}
	
	public void connectToRemoteNxt() throws InterruptedException {
		try {
			LCD.clear();
			LCD.drawString("Connecting...",0,0);
			nxt = new RemoteNXT("1", RS485.getConnector());
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
	}

	//Button listener for kill

	@Override
	public void buttonPressed(Button btn) {
		if (btn.getId() == 8) {
			if (connected) {
				nxt.close();		
			}
			System.exit(0);
		} else if (btn.getId() == 2) {
			if (go) {
				go = false;
			} else {
				go = true;
			}
		}
	}
	@Override
	public void buttonReleased(Button arg0) {
		// TODO Auto-generated method stub

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
			//LCD.clear();
			for (int i = 0; i < 6; i++) {
				String str = thisHexbot.legs[i]._name + ":" + thisHexbot.legs[i].getPos() + "  "; 
				LCD.drawString(str, 0, i);	
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}



/**
 * Gait class has properties that we can use to construct the correct clocks and assign the legs apropriately
 * dutyAngleOffsets[], dutyTimeFactors[], directions[]
 */

class Gait {
	
	double[] dutyTimeOffsetFactors,dutyTimeFactors, speedFactors, dutyAngleOffsets, dutyAngles;
	boolean[] reversed;
	
	Gait(double[] dutyAngleOffsets, double[] dutyAngles, double[] dutyPhaseOffsets, double[] dutyTimeFactors, boolean[] reversed) {
		this.dutyAngles = dutyAngles;
		this.dutyAngleOffsets = dutyAngleOffsets;
		this.dutyTimeOffsetFactors = dutyPhaseOffsets;
		this.dutyTimeFactors = dutyTimeFactors;
		this.reversed = reversed;
	}
}

class WalkForwards implements Behavior {
	
	//WalkForwards(LegSet tripod_1, LegSet tripod_2, )

	@Override
	public boolean takeControl() {
		// TODO Auto-generated method stub+-
		return false;
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		
	}

	@Override 
	public void suppress() {
		// TODO Auto-generated method stub
		
	}
	
}