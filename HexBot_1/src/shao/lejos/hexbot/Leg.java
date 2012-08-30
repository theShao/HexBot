package shao.lejos.hexbot;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.remote.RemoteMotor;

public class Leg {
	
	boolean _isRemote; //True if the motor that powers this leg is attached to a remote nxt
	boolean _isReversed; //True if this motor needs to rotate backwards for the leg to step forwards	
	NXTRegulatedMotor _motor;
	RemoteMotor _rmotor;
	
	BuehlerClockValues clock;
	
	public String _name;
	
	public Leg(String name, boolean isRemote, boolean isReversed, NXTRegulatedMotor motor, RemoteMotor rmotor) {
		/*
		 * isRemote, isReversed, motor, rmotor
		 */
		_name = name;
		_isRemote = isRemote;
		_isReversed = isReversed;
		_motor = motor;
		_rmotor = rmotor;		
	}
	
	public void rotate(int degrees) {
		if (_isRemote) {
			if (_isReversed) {
				_rmotor.rotate(-degrees);				
			} else {
				_rmotor.rotate(degrees);
			}
		} else {
			if (_isReversed) {
				_motor.rotate(-degrees);				
			} else {
				_motor.rotate(degrees);
			}
		}
	}
	
	public void stop() {
		if (_isRemote) {
			_rmotor.stop();
		} else {
			_motor.stop();
		}
	}
	
	public void rotate(int degrees, boolean immediateReturn) {
		if (_isRemote) {
			if (_isReversed) {
				_rmotor.rotate(-degrees, immediateReturn);				
			} else {
				_rmotor.rotate(degrees, immediateReturn);
			}
		} else {
			if (_isReversed) {
				_motor.rotate(-degrees, immediateReturn);				
			} else {
				_motor.rotate(degrees, immediateReturn);
			}
		}
	}
	
	public void waitComplete() {
		if (_isRemote) {
			_rmotor.waitComplete();
		} else {
			_motor.waitComplete();
		}
	}
	
	public void rotateTo(int targetPos, boolean immediateReturn) {
				
		//Lets ignore all awkward cases...
		
		int curPos = getPos();

		if (curPos == targetPos) {
			//No point moving
			return;
		}
		/*
		 * distance_to_move = difference between current and target
		 * if that's >180, then turn the other way 360 - distance instead. 
		 */

		int distance = targetPos - curPos;
		//so if curpos = 90, target = 40, distance = -50
		//curpos = 270, target = 40, distance = -230, we want 360 - 230 = 130
		//curpos = 60, target = 300, distance = 240, we want -360 + 240 = -120
		
		if (Math.abs(distance) > 180) {
			//turn the other way...
			if (distance > 0) {
				//we were going forwards, so go backwards 360 - distance
				this.rotate(-(360 - distance), immediateReturn);
			} else {
				//we were going backwards, so go forwards 360 + distance
				this.rotate(360 + distance, immediateReturn);
			}
		} else {
			this.rotate(distance, immediateReturn);
		}
	}

	
	public void setSpeed(int speed) {
		if (_isRemote) {
			_rmotor.setSpeed(speed);
		} else {
			_motor.setSpeed(speed);
		}
	}
	
	public int getSpeed() {
		if (_isRemote) {
			return _rmotor.getSpeed();
		} else {
			return _motor.getSpeed();
		}
	}
	
	public void forward() {
		if (_isReversed) {
			if (_isRemote) {
				_rmotor.backward();
			} else {
				_motor.backward();
			}
		} else {
			if (_isRemote) {
				_rmotor.forward();
			} else {
				_motor.forward();
			}
		}
	}
	
	public void backward() {
		if (_isReversed) {
			if (_isRemote) {
				_rmotor.forward();
			} else {
				_motor.forward();
			}
		} else {
			if (_isRemote) {
				_rmotor.backward();
			} else {
				_motor.backward();
			}
		}
	}
	
	public void resetTachoCount() {
		if (_isRemote) {
			_rmotor.resetTachoCount();			
		} else {
			_motor.resetTachoCount();
		}
	}
	
	public int getTachoCount() {
		if (_isRemote) {
			return _rmotor.getTachoCount();			
		} else {
			return _motor.getTachoCount();
		}
	}
	
	public int getPos() {
		int t = getTachoCount();
		t = t % 360; //can return negative, we're playing java here and tachocount can be negative		
		if (t < 0) {t = 360 + t; } //so if it is negative, fix it...
		if (_isReversed) {t = 360 - t; }
		if (t == 360) { t = 0; } //not actually needed??			
		return t;	
	}
	
}
 