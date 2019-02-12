package ca.mcgill.ecse211.lab4;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.lab4.Lab4.leftMotor;
import static ca.mcgill.ecse211.lab4.Lab4.rightMotor;
import static ca.mcgill.ecse211.lab4.Lab4.Nav;

/**
 * Class used to test the wheel radius and track constants to improve the accuracy of our odometer
 * Two test programs exist for now
 * 
 * {@value #SPEED_FOR_TESTING} speed used in our testing programs
 * 
 * @author Maxime Bourassa
 * @author Violet Wei
 *
 */
public class TestClass {
  public static final Port colorSampler = LocalEV3.get().getPort("S2");
  public static SensorModes colosSamplerSensor = new EV3ColorSensor(colorSampler);
  public static SampleProvider colorSensorValue = colosSamplerSensor.getMode("Red");
  public static float[] colorSensorData = new float[colosSamplerSensor.sampleSize()];

  //constant
  private static int SPEED_FOR_TESTING = 100;


  /**
   * Method used to test the wheel base constant of our robot 
   * by making it turn 360 degrees on itself
   * If a perfect turn is realized, the constant is good
   */
  public static void rotate360() {

    double angle = 2 * Math.PI;
    for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
      motor.stop();  
    }
    // Sleep for 2 seconds
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      // There is nothing to be done here
    }
    leftMotor.setSpeed(SPEED_FOR_TESTING);
    rightMotor.setSpeed(SPEED_FOR_TESTING);
    leftMotor.rotate(Nav.convertAngle( angle), true); //returns immediately
    rightMotor.rotate(-Nav.convertAngle(angle), false); //wait to return
  }

  /**
   * Method to test the wheel radius constant
   * Make the robot go forward by two tiles, if
   * it does so perfectly, our constant is correct
   */
  public static void forward2Tiles() {

    double distance = 2 * 30.48;
    for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
      motor.stop();
    }
    // Sleep for 2 seconds
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      // There is nothing to be done here
    }
    leftMotor.setSpeed(SPEED_FOR_TESTING);
    rightMotor.setSpeed(SPEED_FOR_TESTING);
    leftMotor.rotate(Nav.convertDistance(distance), true); //returns immediately
    rightMotor.rotate(Nav.convertDistance(distance), false); //wait to return
  }  
}
