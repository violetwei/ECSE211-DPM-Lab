package ca.mcgill.ecse211.lab4;


import lejos.hardware.motor.EV3LargeRegulatedMotor;
import static ca.mcgill.ecse211.lab4.Lab4.leftMotor;
import static ca.mcgill.ecse211.lab4.Lab4.rightMotor;
import static ca.mcgill.ecse211.lab4.Lab4.WHEEL_RAD;
import static ca.mcgill.ecse211.lab4.Lab4.TRACK;

  /**
   * Class used to test the wheel radius and track constants to improve the accuracy of our odometer
   * Two test programs exist
   * @author Maxime Bourassa
   * @author Violet Wei
   *
   */
  public class TestClass {
    //constant
    private static int SPEED = 100;
    
    private static final double PI = Math.PI;

    
    /**
     * Method used to test the wheel base constant of our robot 
     * by making it turn 360 degrees on itself
     * If a perfect turn is realized, the constant is good
     */
    public static void rotate360() {
      // TODO Auto-generated method stub
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

      leftMotor.setSpeed(SPEED);
      rightMotor.setSpeed(SPEED);
      leftMotor.rotate(convertAngle( angle), true); //returns immediately
      rightMotor.rotate(-convertAngle(angle), false); //wait to return
    }

    /**
     * Method to test the wheel radius constant
     * Make the robot go forward by two tiles, if
     * it does so perfectly, our constant is correct
     */
    public static void forward_2_tiles() {

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

      leftMotor.setSpeed(SPEED);
      rightMotor.setSpeed(SPEED);
      leftMotor.rotate(convertDistance(distance), true); //returns immediately
      rightMotor.rotate(convertDistance(distance), false); //wait to return
    }

    /**
     * Method from lab 2 to transform a distance into wheel rotations
     * @param : double distance 
     * @return: amount of degrees the motors have to turn to traverse the distance
     */
    private static int convertDistance(double distance){
      return (int) (360*distance/(2*PI*WHEEL_RAD));
    }
    /**
     * Method from lab 2 to convert an angle (heading) to motor rotations
     * 
     *  @param : double angle 
     *  @return : amount of degrees the motors have to turn to change this heading
     */
    private static int convertAngle(double angle){
      return convertDistance(TRACK*angle/2);
    }
  
}

