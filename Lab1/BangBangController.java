package ca.mcgill.ecse211.HelloWorld;

import lejos.hardware.motor.*;



public class BangBangController implements UltrasonicController {

  
	private final int bandCenter;
  
	private final int bandwidth;
  
	private final int motorLow;
 
	private final int motorHigh;
  
	private int distance;

 

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

  
	
	@Override
  

	public void processUSData(int distance) {
    
		this.distance = distance;
    // TODO: process a movement based on the us distance passed in (BANG-BANG style)
		
		if (Math.abs(distance - bandCenter) <= bandwidth) {
			//go forward
			WallFollowingLab.rightMotor.setSpeed(motorHigh);
			WallFollowingLab.leftMotor.setSpeed(motorHigh);
			
			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.forward();
			
		} else if (distance - bandCenter >= bandwidth) {
			//Too far from the wall
			
			WallFollowingLab.rightMotor.setSpeed(motorHigh);
			WallFollowingLab.leftMotor.setSpeed(motorLow);
			
			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.forward();
				
		}else {
			//too close from the wall
			
			WallFollowingLab.rightMotor.setSpeed(motorLow);
			WallFollowingLab.leftMotor.setSpeed(motorHigh);
			
			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.forward();
		}
			

  	}

  @Override
  

	public int readUSDistance() {
    
		return this.distance;
  
	}

}
