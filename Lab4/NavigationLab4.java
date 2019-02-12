package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Lab4.TRACK;
import static ca.mcgill.ecse211.lab4.Lab4.WHEEL_RAD;
import static ca.mcgill.ecse211.lab4.Lab4.leftMotor;
import static ca.mcgill.ecse211.lab4.Lab4.rightMotor;

public class NavigationLab4 {

  public void setSpeeds(int speed) {
    leftMotor.setSpeed(speed);
    rightMotor.setSpeed(speed);
  }
  
  public void stopMotors() {
    rightMotor.stop(true);
    leftMotor.stop(true);
  }

  public void turnClockwise() {
    leftMotor.forward();
    rightMotor.backward();
  }

  public void turnAnticlockwise() {
    leftMotor.backward();
    rightMotor.forward();
  }

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
   * @param angle
   * @return
   */
  public double SmallestAngle(double angle){
    if (angle > 180) {
      angle =  angle - 360;
    } else if (angle < -180) {
      angle = angle + 360;
    }
    return angle; //the minimal angle
  }

  /**
   * Method from lab 2 to transform a distance into wheel rotations
   * @param : double distance 
   * @return: amount of degrees the motors have to turn to traverse the distance
   */
  public int convertDistance(double distance){
    return (int) (360*distance/(2*Math.PI*WHEEL_RAD));
  }
  /**
   * Method from lab 2 to convert an angle (heading) to motor rotations
   * 
   *  @param : double angle 
   *  @return : amount of degrees the motors have to turn to change this heading
   */
  public int convertAngle(double angle){
    return convertDistance(TRACK*angle*Math.PI/360);
  }
}
