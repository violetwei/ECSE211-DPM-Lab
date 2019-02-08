/* *
 * This class performs the light localization routine, aim to drive the cart to the origin
 * 
 * @author Maxime Bourassa
 * @author Violet Wei
 */
package ca.mcgill.ecse211.lab4;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.filter.MedianFilter;
import lejos.robotics.SampleProvider;

public class LightLocalizer extends Thread{

  private static final long CORRECTION_PERIOD = 10;
  private Odometer odometer;
  private Navigation navigation;
  private EV3ColorSensor colorSensor;
  private SampleProvider colorFiltered;

  private static final int THRESHOLD = 25;
  private static final int MIN_DATA_THRESHOLD = 5;
  private static final double DISTANCE_FROM_CENTER = 9.4;

  private int lastBeepCounter = 0; // Holds the counter that counts iterations since last beep

  // constructor
  public LightLocalizer(Odometer odometer, Navigation navigation) {
    this.odometer = odometer;
    this.navigation = navigation;

    Port colorPort = LocalEV3.get().getPort("S1");
    colorSensor = new EV3ColorSensor(colorPort);
    SampleProvider colorAmbient = colorSensor.getMode(1); // Ambient mode to get light intensity
    colorFiltered = new MedianFilter(colorAmbient, 5); // Use median filter to remove noise
  }

  public void run() {
    long correctionStart, correctionEnd;
    double thetaX1 = 0, thetaX2 = 0, thetaY1 = 0, thetaY2 = 0;

    navigation.travelTo(0, 0); // Move towards the origin

    while (navigation.isNavigating()) { // While robot is turning
      correctionStart = System.currentTimeMillis();

      if (lineDetected()) { // If a line is detected, stop moving
        navigation.stopBothMotors();
        Sound.setVolume(60);
        Sound.beep();
        break;
      }

      // this ensure the detection occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
        }
      }
    }

    navigation.travelTo((odometer.getX() - DISTANCE_FROM_CENTER) / Lab4.SQUARE_LENGTH,
        (odometer.getY() - DISTANCE_FROM_CENTER) / Lab4.SQUARE_LENGTH); // Return towards origin

    while (navigation.isNavigating()) {
      // Wait until we are at the origin
    }

    navigation.turnTo(-odometer.getTheta(), false, true); // Turn back to 0 degrees

    navigation.turnTo(360, true, false); // Perform a full 360 degree turn, returning before completion

    while (navigation.isNavigating()) { // While robot is turning
      correctionStart = System.currentTimeMillis();

      int numberOfLinesDetected = 0;

      if (lineDetected()) { // If a line is detected
        numberOfLinesDetected++;
        double currentTheta = odometer.getTheta();

        switch (numberOfLinesDetected) { // Set theta depending on which line is detected
          case 1:
            thetaX1 = currentTheta;
            break;
          case 2:
            thetaY1 = currentTheta;
            break;
          case 3:
            thetaX2 = currentTheta;
            break;
          case 4:
            thetaY2 = currentTheta;
            break;
          default:
            break;
        }

        // this ensure the detection occurs only once every period
        correctionEnd = System.currentTimeMillis();
        if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
          try {
            Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
          } catch (InterruptedException e) {
          }
        }
      }
    }

    // Calculate the new theta x and theta y once robot is done turning
    double newThetaX = (thetaX2 - thetaX1) / 2;
    double newThetaY = (thetaY2 - thetaY1) / 2;

    // Calculate the x and y positions
    double X = Math.cos(newThetaY);
    double Y = Math.cos(newThetaX);

    // Set new X and Y positions
    odometer.setX(X);
    odometer.setY(Y);

    navigation.travelTo(0, 0); // Travel to origin

    while (navigation.isNavigating()) {
      // Wait until robot is at the point
    }

    navigation.turnTo(-odometer.getTheta(), true, true); // Turn to 0 degrees
  }

  private boolean lineDetected() { // Returns true if a black line is detected
    float[] colorData = new float[colorFiltered.sampleSize()];
    colorFiltered.fetchSample(colorData, 0); // Get data from sensor

    while ((colorData[0] * 100) < MIN_DATA_THRESHOLD) { // Ensure that the sensor is getting usable data
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
      }

      colorFiltered.fetchSample(colorData, 0);
    }

    if (lastBeepCounter == 0) { // Ensures that last line detect was enough time ago
      if ((colorData[0] * 100) < THRESHOLD) { // If data is less than threshold, line detected
        Sound.setVolume(70);
        Sound.beep(); // Beep

        lastBeepCounter = 20; // Reset beep counter

        return true;
      }
    } else {
      lastBeepCounter--; // Otherwise, lower the beep counter
    }

    return false;
  }

}
