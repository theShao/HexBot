package shao.lejos.hexbot;

/**
 * 
 * BuehlerClock class - generic, 
 * @author Shao
 *
 */

public class BuehlerClockFactors {
	
	//Required input var's
	int dutyAngle, dutyAngleOffset; //degrees
	int cycleTime, dutyTime, dutyTimeOffset; //ms
	boolean reversed;

	//Devirved on init
	int restTime; //ms
	int restAngle, restAngleOffset; //degrees
	int dutySpeed, restSpeed; //deg/s

	void init(){
		
		restAngle = 360 - dutyAngle;
		restTime = cycleTime - dutyTime;
		restAngleOffset = dutyAngleOffset + dutyAngle; //Angle at which leg leaves the ground
		dutySpeed = dutyAngle * 1000/dutyTime;
		restSpeed = restAngle * 1000/restTime;		
		//dutyTime = cycleTime * dutyPhaseFactor;
	}
	
	int timeToSpeed(int time) {
		time = (time + dutyTimeOffset) % cycleTime;
		if (reversed) {
			if (time < restTime) {
				return restSpeed;
			} else {
				return dutySpeed;
			}
		} else {
			if (time < dutyTime) {
				return dutySpeed;
			} else {
				return restSpeed;
			}
		}
	}
	
	int timeToAngle(int time) {
		time = (time + dutyTimeOffset) % cycleTime;
		if (reversed) {
			if (time < restTime) {
				return 360 - ( (360 - dutyAngleOffset) + ((time * restSpeed)/1000)) % 360;
			} else {
				return 360 - ( (360 - restAngleOffset) + (((time - restTime) * dutySpeed)/1000)) % 360;
			}
		} else {
			if (time < dutyTime) {
				return (dutyAngleOffset + (time * dutySpeed)/1000) % 360;
			} else {
				return (restAngleOffset + (((time - dutyTime) * restSpeed)/1000)) % 360;
			}
		}
	}
	
	int angleToSpeed(int angle) {
		if (angle > dutyAngle) {
			return restSpeed;
		} else {
			return dutySpeed;
		}
	}
	
	int angleToTime(int angle){
		//TODO : invert timetoangle...
		return 1;
	}

}