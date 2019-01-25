package ca.mcgill.ecse211.odometer;

import java.util.ArrayList;

import ca.mcgill.ecse211.lab2.SquareDriver;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/*
 * OdometryCorrection.java
 */

public class OdometryCorrection extends SquareDriver implements Runnable {
  private static final long CORRECTION_PERIOD = 75;
  private Odometer odometer;
  //private static final double OFFSET = 13.5;
  
  static Port portTouch = LocalEV3.get().getPort("S1");// 1. Get port 
  static SensorModes mylight = new EV3ColorSensor(portTouch);// 2. Get sensor instance 
  static SampleProvider myLightStatus = mylight.getMode("Red");// 3. Get sample provider 
  static float[] sampleTouch = new float[myLightStatus.sampleSize()];  // 4. Create data buffer
  
  static int counter = 0;
  static double displayX = 0;
  static double displayY = 0;
  static double posX;
  static double posY;
  private int squaresize = 4;
  private static ArrayList<Double> increments = new ArrayList();
 
  double position[];
  

  /**
   * This is the default class constructor. An existing instance of the odometer is used. This is to
   * ensure thread safety.
   * 
   * @throws OdometerExceptions
   */
  public OdometryCorrection() throws OdometerExceptions {

    this.odometer = Odometer.getOdometer();

  }

  /**
   * Here is where the odometer correction code should be run.
   * 
   * @throws OdometerExceptions
   */
  // run method (required for Thread)
  public void run() {
    long correctionStart, correctionEnd;
    
    for (int i=0; i< (squaresize-1)*4; i++) {
    	
    	if (i<(squaresize-1)) {
    		increments.add(i*SquareDriver.getTS());
    	} else if (i<(squaresize-1)*2) {
    		increments.add((i % 3)*SquareDriver.getTS());
    	} else if (i<(squaresize-1)*3) {
    		increments.add((squaresize - 2 - i%3)*SquareDriver.getTS());
    	} else if (i<(squaresize-1)*4) {
    		increments.add((squaresize - 2 - i%3)*SquareDriver.getTS());
    	}
    }
    /*
     * 0 - 0
     * 1 - 30
     * 2 - 60
     * 3 -0
     * 4 - 30
     * 5 - 60
     * 6 - 60
     * 7 - 30
     * 8 - 0
     * 	9 - 60
     * 10 - 30
     * 11 - 0
     */
    

    while (true) {
      correctionStart = System.currentTimeMillis();

      // TODO Trigger correction (When do I have information to correct?)
      // TODO Calculate new (accurate) robot position
      myLightStatus.fetchSample(sampleTouch, 0);

      if ( sampleTouch[0] <= 0.35) {
    	  //we are over a black line
    	  LCD.clear();
    	  //System.out.println(sampleTouch[0]);
    	  
    	  Sound.beep();
    	  if (counter < (squaresize - 1) || (counter >= (squaresize - 1)*2 && counter < (squaresize-1)*3)) {
    		  odometer.setY(increments.get(counter));
    	  } else {
    		  odometer.setX(increments.get(counter));
    	  }
    	 /* position = odometer.getXYT();
    	  
    	  if (counter == 1 || counter == 2) {
    		  displayY+= SquareDriver.getTS(); 
    		  //posY = SquareDriver.getTS() - position[1];
    		  
    		 // odometer.getleftMotor().rotate(SquareDriver.getDistance(odometer.getRad(), posY), true);
    	     // odometer.getrightMotor().rotate(SquareDriver.getDistance(odometer.getRad(), posY), false);
    	      odometer.setY(displayY); 
    	      
    	      
    		  
    	  } else if(counter > 3 && counter <= 5) {
    		  displayX+= SquareDriver.getTS();
    		  posX = counter%3 * SquareDriver.getTS() - position[0];
    		  
    		  
    	      odometer.setX(displayX); 
    		  
    	  } else if (counter == 7  || counter <= 8) {
    		  displayY-= SquareDriver.getTS();
    		  posY = position[1] -  counter%3 * SquareDriver.getTS();
    		  
    		  //odometer.getleftMotor().rotate(SquareDriver.getDistance(odometer.getRad(), posY), true);
    	      //odometer.getrightMotor().rotate(SquareDriver.getDistance(odometer.getRad(), posY), false);
    	      odometer.setY(displayY); 
    	      
    	  } else if (counter > 9){
    		  displayX-= SquareDriver.getTS();
    		  posX = position[0] - counter%3 * SquareDriver.getTS();
    		  
    		 // odometer.getleftMotor().rotate(SquareDriver.getDistance(odometer.getRad(), posX), true);
    	      //odometer.getrightMotor().rotate(SquareDriver.getDistance(odometer.getRad(), posX), false);
    	      odometer.setX(displayX); 
    	      
    	 
    		  
    	  } else if(counter ==0){
    		  odometer.setY(0); 
    		  
    	  } else if (counter ==3) {
    		  odometer.setX(0);
    	  } else if (counter ==6) {
    		  odometer.setY(displayY);
    	  } else if (counter ==9) {
    		  odometer.setX(displayX);
    	  }
    	 //System.out.println(counter);*/
    	 counter++;
    	  
      }
      
      

      // TODO Update odometer with new calculated (and more accurate) vales

      

      // this ensure the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here
        }
      }
    }
  }
}
