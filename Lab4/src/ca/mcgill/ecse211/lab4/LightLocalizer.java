package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Lab4.leftMotor;
import static ca.mcgill.ecse211.lab4.Lab4.rightMotor;
import static ca.mcgill.ecse211.lab4.Lab4.SPEED;
import static ca.mcgill.ecse211.lab4.Lab4.odometer;
import static ca.mcgill.ecse211.lab4.Lab4.Nav;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**
 * Class that implements the light localization
 * The robot will localize more precisely after the ultrasonic
 * localization using a light sensor and this algorithm
 * Runs in a thread
 * 
 * {@value #oldValue} used in the differential filter to calculate the difference between the current and the last values returned by the light sensor
 * {@value #correctionStart} time at the beginning of the loop 
 * {@value #correctionEnd} time t the end of the loop
 * {@value #DIFF_THRESHOLD} the differential threshold under which we assess that we have crossed a line
 * {@value #DISTANCE_FROM_CENTER} The distance between the light sensor and the middle of the robot
 * {@value #CORRECTION_PERIOD} The period of each loop
 * {@value #LINE_HALF_ANGLE} half angle of a black line 
 * 
 * @author Maxime Bourassa
 * @author Violet Wei
 */
public class LightLocalizer extends Thread {

  public static final Port colorSampler = LocalEV3.get().getPort("S2");
  public SensorModes colosSamplerSensor = new EV3ColorSensor(colorSampler);
  public SampleProvider colorSensorValue = colosSamplerSensor.getMode("Red");
  public float[] colorSensorData = new float[colosSamplerSensor.sampleSize()];
  private float oldValue = 0;
  long correctionStart, correctionEnd;
  private int DIFF_THREASHOLD = -25;
  private static final double DISTANCE_FROM_CENTER = 6.5;
  private static final long CORRECTION_PERIOD = 50;
  private static final int LINE_HALF_ANGLE = 3;

  /**
   * Run method. Entry point of the thread
   * Runs the correction until it calls other methods for the 
   * smaller adjustments
   * Uses a differential filter to spot lines
   */
  public void run() {
    //set the speeds of the motors
    Nav.setSpeeds(SPEED);
    //turns 45 degrees to face approximately the cornerof the tile
    Nav.turnTo(45);
    //move forward
    Nav.goForward();
    //runs in a loop until a line is seen
    while (true) {
      //keeps track of time
      correctionStart = System.currentTimeMillis();
      //fetching the values from the color sensor
      colorSensorValue.fetchSample(colorSensorData, 0);
      //getting the value returned from the sensor, and multiply it by 1000 to scale
      float value = colorSensorData[0]*1000;
      //computing the derivative at each point
      float diff = value - oldValue;
      //storing the current value, to be able to get the derivative on the next iteration
      oldValue = value;
      //if the derivative value at a given point is less than -25, this means that a line has been detected
      if(diff < DIFF_THREASHOLD) {
        //EV3 beeps
        Sound.beep();
        //stop the motors
        Nav.stopMotors();
        Nav.setSpeeds(SPEED);
        leftMotor.rotate(Nav.convertDistance(DISTANCE_FROM_CENTER),true);
        rightMotor.rotate(Nav.convertDistance(DISTANCE_FROM_CENTER),false);
        //begins true correction
        precorrect();
        //exit the loop after method returns
        break;
      }
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
          // expected that the odometry correction will be
          // interrupted by another thread
        }
      }
    }

  }

  /**
   * This method is used after the robot detected its first line.
   * At that point the error is only either in x or y but not both
   * Here we determine if we need to correct x or y
   * Uses a differential filter to spot lines
   */
  private void precorrect() {
    //Skip the 0 degree line
    //and maybe the 270 degrees line depending on what coordinate needs correction
    Nav.turnTo(-120);
    Nav.turnAnticlockwise();
    //runs in a loop until a line is seen
    while(true) {
      correctionStart = System.currentTimeMillis();
      //fetching the values from the color sensor
      colorSensorValue.fetchSample(colorSensorData, 0);
      //getting the value returned from the sensor, and multiply it by 1000 to scale
      float value = colorSensorData[0]*1000;
      //computing the derivative at each point
      float diff = value - oldValue;
      //storing the current value, to be able to get the derivative on the next iteration
      oldValue = value;
      //if the derivative value at a given point is less than -25, this means that a line is detected
      if(diff < DIFF_THREASHOLD) {
        //beeps
        Sound.beep();
        Nav.stopMotors();
        //we detected the line on the left (270 degrees)
        if (odometer.getTheta() < 285 && odometer.getTheta() > 195) {
          //x needs to be corrected
          correctX();
          break;
          //we detected the 180 degree line
        } else if (odometer.getTheta() > 165) {
          //y needs to be corrected
          correctY();
          break;
        }
      }
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
          // expected that the odometry correction will be
          // interrupted by another thread
        }
      }

    }
  }

  /**
   * Method that implements the x correction
   * Uses a differential filter to spot lines
   */
  private void correctX() {
    //number of lines crossed in this method
    int numlines = 0;
    double distanceCorr = 0;
    leftMotor.forward();
    rightMotor.backward();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // there is nothing to be done here because it is not
      // expected that the odometry correction will be
      // interrupted by another thread
    }
    while(true) {
      correctionStart = System.currentTimeMillis();
      //fetching the values from the color sensor
      colorSensorValue.fetchSample(colorSensorData, 0);
      //getting the value returned from the sensor, and multiply it by 1000 to scale
      float value = colorSensorData[0]*1000;
      //computing the derivative at each point
      float diff = value - oldValue;
      //storing the current value, to be able to get the derivative on the next iteration
      oldValue = value;
      //if the derivative value at a given point is less than -25, this means that a black line is detected
      if(diff < DIFF_THREASHOLD) {
        //first line spotted (0 degree line)
        if(numlines == 0) {
          //beeps
          Sound.beep();
          //calculate the correction needed
          distanceCorr = Math.sin(odometer.getTheta()*Math.PI/180)*DISTANCE_FROM_CENTER;
          Nav.turnClockwise();
          numlines++;
          //wait 1 second to avoid seeing the same line twice
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            // there is nothing to be done here because it is not
            // expected that the odometry correction will be
            // interrupted by another thread
          }
          //90 degrees line spotted
        } else if(numlines == 1) {
          Sound.beep();
          //correct the error
          Nav.stopMotors();
          Nav.goForward();
          leftMotor.rotate(Nav.convertDistance(distanceCorr),true);
          rightMotor.rotate(Nav.convertDistance(distanceCorr),false);
          Nav.turnAnticlockwise();
          numlines++;
          //90 degrees line spotted again
        } else {
          //as I always spot the beginning of lines, I need to add an offset of half of a black line's angle
          //to my turn
          Nav.turnTo(-90-LINE_HALF_ANGLE);
          Nav.stopMotors();
          //we are done, reset the odometer to 0
          odometer.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
          //exit the loop
          break;
        }
      }
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
          // expected that the odometry correction will be
          // interrupted by another thread
        }
      }
    }
  }

  /**
   * Method used to implement the Y correction of the robot
   * Very similar to the X correction but some non trivial differences
   * stopped us from combining the methods
   * Implements a differential filter
   */
  private void correctY() {
    int numlines = 0;
    double distanceCorr = 0;
    Nav.turnAnticlockwise();
    //we are at the 180 degree line after the previous metohd
    //correct the angle on the odometer
    odometer.setTheta(180);
    //makes sure we don't spot the same line twice
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // there is nothing to be done here because it is not
      // expected that the odometry correction will be
      // interrupted by another thread
    }

    while(true) {
      correctionStart = System.currentTimeMillis();
      //fetching the values from the color sensor
      colorSensorValue.fetchSample(colorSensorData, 0);
      //getting the value returned from the sensor, and multiply it by 1000 to scale
      float value = colorSensorData[0]*1000;
      //computing the derivative at each point
      float diff = value - oldValue;
      //storing the current value, to be able to get the derivative on the next iteration
      oldValue = value;
      //if the derivative value at a given point is less than -25, this means that a black line is detected
      if(diff < DIFF_THREASHOLD) {
        //90 degrees line spotted
        if(numlines == 0) {
          //beeps
          Sound.beep();
          //calculates the correction
          distanceCorr = Math.cos(odometer.getTheta()*Math.PI/180)*DISTANCE_FROM_CENTER;
          numlines++;
          try {
            Thread.sleep(1000);
          } catch (Exception e) {
          } // Poor man's timed sampling
          // 0 degrees line spotted
        } else if(numlines == 1) {
          Sound.beep();
          Nav.stopMotors();
          Nav.goForward();
          leftMotor.rotate(Nav.convertDistance(distanceCorr),true);
          rightMotor.rotate(Nav.convertDistance(distanceCorr),false);
          //we are done, reset the odometer to 0
          odometer.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
          break;
        }
      }
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
          // expected that the odometry correction will be
          // interrupted by another thread
        }
      }
    }   
  }
}
