package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Lab4.TRACK;
import static ca.mcgill.ecse211.lab4.Lab4.WHEEL_RAD;
import static ca.mcgill.ecse211.lab4.Lab4.leftMotor;
import static ca.mcgill.ecse211.lab4.Lab4.rightMotor;
import lejos.robotics.RegulatedMotor;

/**
 * Implements methods to help our robot navigate while it is localizing
 * Not the full class of Lab3, only a couple methods to move around
 * Used by light and ultrasonic localizing classes 
 * 
 * @author Maxime Bourassa
 * @author Violet Wei
 *
 */
public class NavigationLab4 {

  /**
   * Method used to make the robot go forward from other classes
   */
  public void goForward() {
    //makes sure both motors are synchronized perfectly
    RegulatedMotor motorlist[] = {(RegulatedMotor) rightMotor};
    leftMotor.synchronizeWith(motorlist);
    leftMotor.startSynchronization();
    leftMotor.forward();
    rightMotor.forward();
    leftMotor.endSynchronization();
  }
  /**
   * Method used to set the speed of both motors to the same value 
   * from other classes
   * 
   * @param speed the speed we wantto set the motors to
   */
  public void setSpeeds(int speed) {
    leftMotor.setSpeed(speed);
    rightMotor.setSpeed(speed);
  }

  /**
   * Method used to stop both motors
   * from other classes
   */
  public void stopMotors() {
    rightMotor.stop(true);
    leftMotor.stop(true);
  }

  /**
   * Method used to make the robot turn clockwise 
   * continuously by reversing the right motor
   */
  public void turnClockwise() {
    leftMotor.forward();
    rightMotor.backward();
  }

  /**
   * Method used to make the robot turn anti clockwise 
   * continuously by reversing the left motor
   */
  public void turnAnticlockwise() {
    leftMotor.backward();
    rightMotor.forward();
  }

  /**
   * Method used to turn to a specific angle from the one
   * the robot is currently  pointing at according to the odometer
   * 
   * @param theta
   */
  public void turnTo(double theta) { //method from navigation program
    //System.out.println("\n\n\n"+odometer.getT());
    double angle = SmallestAngle(theta);

    leftMotor.rotate(convertAngle(angle),true);
    rightMotor.rotate(-convertAngle(angle),false);

  }

  /**
   * Method to find if the smallest angle is achieved
   * by turning left or right
   * 
   * @param angle the angle we want the robot to turn to
   */
  public double SmallestAngle(double angle){
    if (angle > 180) {
      angle =  angle - 360;
    } else if (angle < -180) {
      angle = angle + 360;
    }
    //the minimal angle
    return angle; 
  }

  /**
   * Method from lab 2 to transform a distance into wheel rotations
   * @param : double distance the distance we want the robot to travel
   * @return: amount of degrees the motors have to turn to traverse the distance
   */
  public int convertDistance(double distance){
    return (int) (360*distance/(2*Math.PI*WHEEL_RAD));
  }
  /**
   * Method from lab 2 to convert an angle (heading) to motor rotations
   * 
   *  @param : double angle the angle we want the robot to turn
   *  @return : amount of degrees the motors have to turn to change this heading
   */
  public int convertAngle(double angle){
    return convertDistance(TRACK*angle*Math.PI/360);
  }
}
