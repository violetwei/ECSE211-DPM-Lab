package ca.mcgill.ecse211.wallfollowing;

import ca.mcgill.ecse211.lab1.UltrasonicController;
import ca.mcgill.ecse211.lab1.WallFollowingLab;

/**
 * PController class.
 * Implements the P-type controller model by calculating the speed 
 * correction depending on the distance from the bandcenter
 * It implements the UltrasonicController interface to process and read data 
 * from the US sensor with the methods of the interface
 * @author maxbo
 *
 */
public class PController implements UltrasonicController {

  /* Constants */
  private static final int MOTOR_SPEED = 200;
  private static final int FILTER_OUT = 20;
  private final int MAX_CORRECTION = 175;
  
  private static final double PROPORTION_CONSTANT = 8;

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
  /**
   * Defines the method of the interface to process
   * the data of the US sensor and determine the course of action
   * to take
   * @param distance
   * @return void
   */
  @Override 
  public void processUSData(int distance) {
	  float leftSpeed, rightSpeed;
	  float difference;

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
    
    float distError = bandCenter + 5 - this.distance;
    
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
    	//If its really close to the wall, it will turn according to a different pattern
    	if (this.distance < 10 ) {
    		difference = calculateCorrection(distError);
    		leftSpeed = MOTOR_SPEED - difference/5;
    		rightSpeed = MOTOR_SPEED - difference/5;
    		WallFollowingLab.leftMotor.setSpeed(leftSpeed);
    		WallFollowingLab.rightMotor.setSpeed(rightSpeed);
    		WallFollowingLab.leftMotor.forward(); 
    		WallFollowingLab.rightMotor.backward();
    	} //If its not too close to the wall yet, turn normally
    	else {
    		difference = calculateCorrection(distError);
        	rightSpeed = MOTOR_SPEED - difference/5;
    		leftSpeed = MOTOR_SPEED + difference;
    		WallFollowingLab.leftMotor.setSpeed(leftSpeed);
    		WallFollowingLab.rightMotor.setSpeed(rightSpeed);
    		WallFollowingLab.leftMotor.forward(); 
    		WallFollowingLab.rightMotor.forward();
    	}
    	//If its too far from the wall, come back
	} else if (distError < 0 ) {
    	difference = calculateCorrection(distError);
    	leftSpeed = MOTOR_SPEED - difference/5;
		rightSpeed = (float) (MOTOR_SPEED + difference * 3.0 / 5.0);
    	WallFollowingLab.leftMotor.setSpeed(leftSpeed);
		WallFollowingLab.rightMotor.setSpeed(rightSpeed);
		WallFollowingLab.leftMotor.forward(); 
		WallFollowingLab.rightMotor.forward();	
    } //If we have a false negative, we a re probably too close to the wall, so we move away from it
	else if (distError < -500){
		difference = calculateCorrection(distError);
		leftSpeed = MOTOR_SPEED - difference/5;
		rightSpeed = MOTOR_SPEED - difference/5;
		WallFollowingLab.leftMotor.setSpeed(leftSpeed);
		WallFollowingLab.rightMotor.setSpeed(rightSpeed);
		WallFollowingLab.leftMotor.forward(); 
		WallFollowingLab.rightMotor.backward();
    	
    }
    
    
  }
  /**
   * Method used to calculate the speed correction depending on the distance from the bandcenter
   * Use a linear approach to calculate the error correction
   * @param errorValue
   * @return sppedCorrection
   */
  private float calculateCorrection(float errorValue) {
	  
	  int speedCorrection;
	  if (errorValue < 0) {
		  errorValue = -errorValue;
	  }
	  //else if (errorValue > 0 ) {
		  //PROPORTION_CONSTANT *= 1.25;
	 // }
	  
	  speedCorrection = (int) (PROPORTION_CONSTANT * errorValue);
	  
	  //PROPORTION_CONSTANT = 6;
	  
	  if (speedCorrection > MAX_CORRECTION) {
		 speedCorrection = MAX_CORRECTION;
	  }
	 // System.out.println(speedCorrection);
	  
	  return speedCorrection;

	  
  }

  /**
   * Method from the interface
   * Returns the distance calculated by the US sensor
   * @return distance
   */
  @Override
  public int readUSDistance() {
    return this.distance;
  }

}
