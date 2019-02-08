package ca.mcgill.ecse211.lab4;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * This is the class of the program that does most of the heavy lifting
 * Makes the robot run to the specified five points
 * If an obstacle is seen it will be avoided with a BanBang controller
 * The robot will always turn with the smallest angle possible
 * 
 * @author Maxime Bourassa
 * @author Violet Wei
 *
 */
public class Navigation extends Thread {

  //Private instance parameters for the class
  private Odometer odometer;
  private int bandCenter = 10;
  private int bandWidth = 2;
  private int distance;
  private SampleProvider us;
  private float[] usData;



  /**
   * constructor of the class, takes in the necessary info from the ultrasonic 
   * sensor, the rest is accessed through getters
   * 
   * @param us
   * @param usData
   */
  public Navigation(SampleProvider us, float[] usData){
    this.odometer = Lab3.getOdo();
    this.us = us;
    this.usData = usData;
  }

  //constants of the class
  private static final int SWEEPING_SPEED = 175;
  private static final int NORMAL_SPEED = 100;
  private static final int ROTATE_SPEED = 100;
  private static final int OBSTACLE_TOO_FAR_SPEED = 150;
  private static final int OBSTACLE_TOO_CLOSE_SPEED = 200;
  private static final int TO_THE_RIGHT = 45;
  private static final int TO_THE_LEFT = -45;
  private static final int SWITCH_ANGLE = 20;
  private static final int SENSOR_ANGLE_AVOIDANCE = 70;
  private static final double PI = Math.PI;
  private static final double TILE_SIZE = 30.48;
  private static boolean navigating = true;

  /**
   * Run method, entry pointof the thread, calls the travel to 
   * method with a point for the robot to go there
   */
  @Override
  public void run() {

    for(int i=0; i<Lab3.NUMPOINTS; i++) {
      travelTo(Lab3.four_points[i][0]*TILE_SIZE,Lab3.four_points[i][1]*TILE_SIZE);
    }
  }

  /**
   * This method makes the robot travel to a specified point
   * Calls other methods for turns and obstacle avoidance
   * 
   * @param x
   * @param y
   */
  public void travelTo(double x, double y) {

    //parameters
    double trajectory;
    int distance;

    //reset left and right motors
    for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {Lab3.getLeftMotor(), Lab3.getRightMotor()}) {
      motor.stop();
    }

    //get the angle
    double XDirection = x - odometer.getX();
    double YDirection = y - odometer.getY();
    double NewAngle = Math.atan2(XDirection, YDirection)- odometer.getTheta();
    Sound.beep();
    Lab3.getLeftMotor().setSpeed(ROTATE_SPEED);
    Lab3.getRightMotor().setSpeed(ROTATE_SPEED);
    turnTo(NewAngle); //turn

    //get the distance
    trajectory = Math.hypot(XDirection, YDirection);
    //move to the point
    Lab3.getLeftMotor().setSpeed(NORMAL_SPEED);
    Lab3.getRightMotor().setSpeed(NORMAL_SPEED);
    Lab3.getLeftMotor().rotate(convertDistance(trajectory),true);
    Lab3.getRightMotor().rotate(convertDistance(trajectory),true);
    //resets the tacho count of the motor to 0, makes it easier to use the rotateTo method
    //afterwards
    Lab3.getSensorMotor().resetTachoCount();
    Lab3.getSensorMotor().setSpeed(SWEEPING_SPEED);


    while (Lab3.getLeftMotor().isMoving() || Lab3.getRightMotor().isMoving()) { // Scan around when moving
      while (!Lab3.getSensorMotor().isMoving()){ //Rotate the sensor either left or right to a specified angle
        if (Lab3.getSensorMotor().getTachoCount()>=SWITCH_ANGLE){
          Lab3.getSensorMotor().rotateTo(TO_THE_LEFT,true);
        } else {
          Lab3.getSensorMotor().rotateTo(TO_THE_RIGHT,true);
        }
      }
      us.fetchSample(usData,0);			// acquire data from the ultrasonic sensor
      distance=(int)(usData[0]*100.0);	// extract from buffer, cast to int
      Simplefilter(distance);				//Filter the distance received

      if(distance <= bandCenter){
        //There is an obstacle
        Sound.beep();
        Lab3.getLeftMotor().stop(true); // Stop the robot 
        Lab3.getRightMotor().stop(true);
        navigating = false; //exit navigation mode
      }
      try { Thread.sleep(25); } catch(Exception e){}		// Poor man's timed sampling on one line
    }

    if (!this.isNavigating()){ //if an obstacle has been seen
      avoidBlock(); //avoidance method 
      Lab3.getSensorMotor().rotateTo(0); // reset sensor position
      navigating = true; // re-enable navigation mode
      travelTo(x,y); // continue traveling to destination
      return; //the destination has been reached
    }
    Lab3.getSensorMotor().rotateTo(0); //sensor back to the middle

  }

  /**
   * Method used to make the robot rotate to a specified angle
   * with the smallest angle possible
   * 
   * @param theta
   */
  public void turnTo(double theta) { //method from navigation program
    //System.out.println("\n\n\n"+odometer.getT());
    double angle = SmallestAngle(theta);

    Lab3.getLeftMotor().rotate(convertAngle(angle),true);
    Lab3.getRightMotor().rotate(-convertAngle(angle),false);

  }

  /**
   * Method to find if the smallest angle is achieved
   * by turning left or right
   * 
   * @param angle
   * @return
   */
  public double SmallestAngle(double angle){
    if (angle > PI) {
      angle =  angle - 2*PI;
    } else if (angle < -PI) {
      angle = angle + 2*PI;
    }
    return angle; //the minimal angle
  }

  /**
   *  Method that returns a boolean indicating
   *  whether or not the robot is currently navigating
   */
  public boolean isNavigating() {
    return navigating;
  }
  /**
   * Method from lab 2 to transform a distance into wheel rotations
   * @param : double distance 
   * @return: amount of degrees the motors have to turn to traverse the distance
   */
  private int convertDistance(double distance){
    return (int) (360*distance/(2*PI*Lab3.getWR()));
  }
  /**
   * Method from lab 2 to convert an angle (heading) to motor rotations
   * 
   *  @param : double angle 
   *  @return : amount of degrees the motors have to turn to change this heading
   */
  private int convertAngle(double angle){
    return convertDistance(Lab3.getTrack()*angle/2);
  }

  /**
   * Method used to avoid a block that is blocking the robot in its run
   * It implements a BangBang Controller
   * Only returns after the obstacle has been avoided completely
   */
  public void avoidBlock(){
    //parameters
    boolean turning_outside;
    double lastAngle = odometer.getTheta();
    boolean first_time = true;

    //Decides if the robot should avoid to the right or to the left of the block
    //depending on the current robots position
    if(odometer.getY()>=1.9*TILE_SIZE || odometer.getY() < 0.1*TILE_SIZE || odometer.getX()>=1.9*TILE_SIZE || odometer.getX() < 0.1*TILE_SIZE) {
      turning_outside = false;
    } else {
      turning_outside = true;
    }
    if(Lab3.clockwise) {
      turning_outside = !turning_outside;
    }

    if(!turning_outside) { //turns left to avoid
      // Make sure the US sensor is facing the block
      Lab3.getSensorMotor().rotateTo(-SENSOR_ANGLE_AVOIDANCE);

      // BangBang controller to avoid the block
      do{
        if(first_time) { //if first iteration adjust the heading of the robot to facilitate the avoidance
          Lab3.getLeftMotor().rotate(-convertAngle(PI/2),true);
          Lab3.getRightMotor().rotate(convertAngle(PI/2),false);
          first_time = false;
        }
        us.fetchSample(usData,0);							// acquire data from the sample provider
        distance=(int)(usData[0]*100.0);					// extract from buffer, cast to int
        Simplefilter(distance);								//always filter the distance
        int errorDistance = bandCenter - distance; 			//error from the band center

        if (Math.abs(errorDistance)<= bandWidth){ //moving in straight line
          Lab3.getLeftMotor().setSpeed(NORMAL_SPEED);
          Lab3.getRightMotor().setSpeed(NORMAL_SPEED);
          Lab3.getLeftMotor().forward();
          Lab3.getRightMotor().forward();
        } else if (errorDistance > 0){ //too close to wall
          Lab3.getLeftMotor().setSpeed(OBSTACLE_TOO_CLOSE_SPEED);// Setting the outer wheel to reverse
          Lab3.getRightMotor().setSpeed(NORMAL_SPEED); 
          Lab3.getLeftMotor().backward();
          Lab3.getRightMotor().forward();
        } else if (errorDistance < 0){ // getting too far from the wall
          Lab3.getRightMotor().setSpeed(NORMAL_SPEED);
          Lab3.getLeftMotor().setSpeed(OBSTACLE_TOO_FAR_SPEED);// Setting the outer wheel to move faster
          Lab3.getRightMotor().forward();
          Lab3.getLeftMotor().forward();
        }
      } while (odometer.getTheta() < lastAngle + PI/4); //exit condition of the loop

    } else {
      //same thing but everything is inversed as the block is avoided by turning to its right
      Lab3.getSensorMotor().rotateTo(SENSOR_ANGLE_AVOIDANCE);


      // BangBang controller to avoid the obstacle
      do{
        if(first_time) {
          Lab3.getLeftMotor().rotate(convertAngle(PI/2),true);
          Lab3.getRightMotor().rotate(-convertAngle(PI/2),false);
          first_time = false;
        }
        us.fetchSample(usData,0);							// acquire data from the sample provider
        distance=(int)(usData[0]*100.0);					// extract from buffer, cast to int
        Simplefilter(distance);
        int errorDistance = bandCenter - distance;

        if (Math.abs(errorDistance)<= bandWidth){ //moving in straight line
          Lab3.getLeftMotor().setSpeed(NORMAL_SPEED);
          Lab3.getRightMotor().setSpeed(NORMAL_SPEED);
          Lab3.getLeftMotor().forward();
          Lab3.getRightMotor().forward();
        } else if (errorDistance > 0){ //too close to wall
          Lab3.getRightMotor().setSpeed(OBSTACLE_TOO_CLOSE_SPEED);// Setting the outer wheel to reverse
          Lab3.getLeftMotor().setSpeed(NORMAL_SPEED); 
          Lab3.getRightMotor().backward();
          Lab3.getLeftMotor().forward();
        } else if (errorDistance < 0){ // getting too far from the wall
          Lab3.getLeftMotor().setSpeed(NORMAL_SPEED);
          Lab3.getRightMotor().setSpeed(OBSTACLE_TOO_FAR_SPEED);// Setting the outer wheel to move faster
          Lab3.getLeftMotor().forward();
          Lab3.getRightMotor().forward();
        } 
      } while (odometer.getTheta() > lastAngle -PI/4);
      Sound.beep();
      Lab3.getLeftMotor().stop();
      Lab3.getRightMotor().stop();
    }
  }

  /**
   * Reads the distance from the US sensor
   * @return Distance calculated by the US sensor
   */
  public int readUSDistance() {
    return this.distance;
  }
  
  void stopBothMotors() {
    leftMotor.setSpeed(0);
    rightMotor.setSpeed(0);
  }
  
  /**
   * Simple filter to remove false far distances given by the sensor
   * Same as the one used in lab 1
   * @param distance
   */
  public void Simplefilter(int distance){
    //parameters
    int FILTER_OUT = 25;
    int filterControl = 0;

    // rudimentary filter - copied from TA code on myCourses
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
  }
}
