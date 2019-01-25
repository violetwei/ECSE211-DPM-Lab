package ca.mcgill.ecse211.odometer;

import ca.mcgill.ecse211.lab2.SquareDriver;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/*
 * OdometryCorrection.java
 */

public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 100;
  private Odometer odometer;
  private static final double OFFSET = 13.5;
  
  static Port portTouch = LocalEV3.get().getPort("S1");// 1. Get port 
  static SensorModes mylight = new EV3ColorSensor(portTouch);// 2. Get sensor instance 
  static SampleProvider myLightStatus = mylight.getMode("Red");// 3. Get sample provider 
  static float[] sampleTouch = new float[myLightStatus.sampleSize()];  // 4. Create data buffer
  
  static int counter = 0;
  static float X = 0;
  static float Y = 0;
  static int Theta = 0;
  

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

    while (true) {
      correctionStart = System.currentTimeMillis();

      // TODO Trigger correction (When do I have information to correct?)
      // TODO Calculate new (accurate) robot position
      myLightStatus.fetchSample(sampleTouch, 0);
      if (sampleTouch[0] > 0.3) {
    	  //we are over a black line
    	  if (counter > 0 && counter < 3) {
    		  X+= SquareDriver.getTS();
    	  } else if(counter > 3 && counter < 6) {
    		  Y+= SquareDriver.getTS();
    	  }else if (counter > 6 && counter < 9) {
    		  X-= SquareDriver.getTS();
    	  } else if (counter > 9){
    		  Y-= SquareDriver.getTS();
    	  }
    	  
    	 counter++;
    	  
      }
      
      

      // TODO Update odometer with new calculated (and more accurate) vales

      odometer.setXYT(X - OFFSET, Y, Theta);

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
