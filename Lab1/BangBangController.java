package ca.mcgill.ecse211.wallfollowing;

import lejos.hardware.motor.*;

/**
 * Class that implements the BangBang Controller model.
 * If the robot is not in the right distance from the bandCenter (bandwidth)
 * the robot will apply the maximum correction and turn to come back within the bandwidth
 * @author maxbo
 *
 */

public class BangBangController implements UltrasonicController {

  
	private final int bandCenter;
  
	private final int bandwidth;
  
	private final int motorLow;
 
	private final int motorHigh;
  
	private int distance;

	private static final int FILTER_OUT = 15;
	
	private int filterControl;
 
	/**
	 * Class constructor. Creates a new instance of this controller.
	 * Will be called in the WallFollowingLab class
	 * @param bandCenter
	 * @param bandwidth
	 * @param motorLow
	 * @param motorHigh
	 */

	public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) {
    // Default Constructor
    
	
		this.bandCenter = bandCenter;
    
		this.bandwidth = bandwidth;
    
		this.motorLow = motorLow;
    
		this.motorHigh = motorHigh;
    

		WallFollowingLab.leftMotor.setSpeed(motorHigh); // Start robot moving forward
    
		WallFollowingLab.rightMotor.setSpeed(motorHigh);
    
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
    
		this.distance = distance;
    // TODO: process a movement based on the us distance passed in (BANG-BANG style)
		
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

		
		if (distance - bandCenter >= bandwidth) {
			//too far from the wall, will turn left
			WallFollowingLab.rightMotor.setSpeed(motorHigh);
			WallFollowingLab.leftMotor.setSpeed(motorLow);
			
			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.forward();
			
		} else if (distance < bandCenter - bandwidth && distance > bandCenter/2) {
			//Too close from the wall, will turn right
			
			WallFollowingLab.rightMotor.setSpeed(motorLow);
			WallFollowingLab.leftMotor.setSpeed(motorHigh);
			
			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.forward();
				
		}else if (distance < bandCenter/2) {
			// Way too close from the wall, will turn right super fast
			
			WallFollowingLab.rightMotor.setSpeed(motorLow);
			WallFollowingLab.leftMotor.setSpeed(motorLow);
			
			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.backward();
		} 
		else if (distance > 100){
			//False negative, move awy from the wall
			WallFollowingLab.rightMotor.setSpeed(motorLow);
			WallFollowingLab.leftMotor.setSpeed(motorLow);
			
			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.backward();
	    	
	    }
		else {
			//go forward, the robot is within the bandwidth
			
			WallFollowingLab.rightMotor.setSpeed(motorHigh);
			WallFollowingLab.leftMotor.setSpeed(motorHigh);
			
			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.forward();
		}
			

  	}
	

	/**
	   * Method used to calculate the speed correction depending on the distance from the bandcenter
	   * Use a linear approach to calculate the error correction
	   * @param errorValue
	   * @return sppedCorrection
	   */
  @Override
	public int readUSDistance() {
    
		return this.distance;
  
	}

}

