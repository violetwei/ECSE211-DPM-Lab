package ca.mcgill.ecse211.wallfollowing;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {

  /* Constants */
  private static final int MOTOR_SPEED = 200;
  private static final int FILTER_OUT = 20;
  private final int MAX_CORRECTION = 175;
  
  private static final double PROPORTION_CONSTANT = 6;

  private final int bandCenter;
  private final int bandWidth;
  private int distance;
  private int filterControl;

  public PController(int bandCenter, int bandwidth) {
    this.bandCenter = bandCenter;
    this.bandWidth = bandwidth;
    this.filterControl = 0;

    WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED); // Initalize motor rolling forward
    WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance) {
	  int leftSpeed, rightSpeed;
	  int difference;

    // rudimentary filter - toss out invalid samples corresponding to null
    // signal.
    // (n.b. this was not included in the Bang-bang controller, but easily
    // could have).
    //
    if (distance >= 255 && filterControl < FILTER_OUT) {
      // bad value, do not set the distance var, however do increment the
      // filter value
      filterControl++;
    } else if (distance >= 255) {
      // We have repeated large values, so there must actually be nothing
      // there: leave the distance alone
      this.distance = distance;
    } else {
      // distance went below 255: reset filter and leave
      // distance alone.
      filterControl = 0;
      this.distance = distance;
    }

    // TODO: process a movement based on the us distance passed in (P style)
    
    int distError = bandCenter - this.distance;
    
    // The robot will move FORWARD if error is smaller than bandwidth
    if(Math.abs(distError) <= bandWidth) {
    	leftSpeed = MOTOR_SPEED;
    	rightSpeed = MOTOR_SPEED;
    	WallFollowingLab.leftMotor.setSpeed(leftSpeed);
    	WallFollowingLab.rightMotor.setSpeed(rightSpeed);
		WallFollowingLab.leftMotor.forward();
		WallFollowingLab.rightMotor.forward();
    }
    // The robot will turn RIGHT as it's approaching the wall, moving away from wall
    else if(distError > 0) {
			WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED/2);
			WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED/2);
			WallFollowingLab.leftMotor.forward(); 
			WallFollowingLab.rightMotor.backward();
	}
    else {
    	difference = calculateCorrection(distError);
    	leftSpeed = MOTOR_SPEED - difference/5;
		rightSpeed = MOTOR_SPEED + difference;
    	WallFollowingLab.leftMotor.setSpeed(leftSpeed);
		WallFollowingLab.rightMotor.setSpeed(rightSpeed);
		WallFollowingLab.leftMotor.forward(); 
		WallFollowingLab.rightMotor.backward();	
    }
    
    
  }
  
  private int calculateCorrection(int errorValue) {
	  
	  int speedCorrection;
	  if (errorValue < 0) {
		  errorValue = -errorValue;
	  }
			
	  speedCorrection = (int) (PROPORTION_CONSTANT * errorValue);

	  if (speedCorrection > MAX_CORRECTION) {
		 speedCorrection = 175;
	  }
			
	  return speedCorrection;

	  
  }


  @Override
  public int readUSDistance() {
    return this.distance;
  }

}

