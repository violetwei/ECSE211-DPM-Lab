package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Lab4.*;
/** 
 * Class used to keep track of the distance the robot traveled through wheel rotations
 * It is a combination of both the Odometer and the OdometerData class from lab 2
 * Runs in a thread at a frequency of 13.33 Hz
 * 
 * @author Rodrigo Silva
 * @author Dirk Dubois
 * @author Derek Yu
 * @author Karim El-Baba
 * @author Michael Smith
 *
 * @author Violet Wei
 * @author Maxime Bourassa
 */
public class Odometer extends Thread {
  // robot position
  private double x, y, theta;
  private int leftMotorTachoCount, rightMotorTachoCount;

  // odometer update period, in ms
  private static final long ODOMETER_PERIOD = 75;

  // lock object for mutual exclusion
  private Object lock;


  /**
   * Constructor of the class 
   * Dosen't need any arguments
   * All are set by default
   */
  public Odometer() {
    this.x = 0.0;
    this.y = 0.0;
    this.theta = 0.0;
    this.leftMotorTachoCount = 0;
    this.rightMotorTachoCount = 0;
    lock = new Object();
  }

  /**
   * Run method, main entry point of the thread
   * Most of the class is being run in here
   */
  public void run() {
    long updateStart, updateEnd;
    int currentTachoL, currentTachoR, lastTachoL, lastTachoR;
    double distL, distR, dDistance, dTheta, dX, dY;
    
    
    
    leftMotor.resetTachoCount();
    rightMotor.resetTachoCount();
    lastTachoL=leftMotor.getTachoCount();
    lastTachoR=rightMotor.getTachoCount();

    while (true) {
      updateStart = System.currentTimeMillis();

      //odometer code adapted from myCourses example
      currentTachoL = leftMotor.getTachoCount();
      currentTachoR = rightMotor.getTachoCount();

      distL = Math.PI*WHEEL_RAD*(currentTachoL - lastTachoL)/180;
      distR = Math.PI*WHEEL_RAD*(currentTachoR - lastTachoR)/180;

      lastTachoL=currentTachoL;                             // save the tacho counts for next iteration of the loop
      lastTachoR=currentTachoR;

      dDistance = 0.5*(distL+distR);                            // distance traveled by the robot
      dTheta = (distL-distR)/TRACK*180/Math.PI;                                // compute change in angle of the robot
      dX = dDistance * Math.sin(theta*Math.PI/180);                     // X component of displacement
      dY = dDistance * Math.cos(theta*Math.PI/180);                     // Y component of displacement
      synchronized (lock) {
        //only place these variables are updated (x,y and theta)
        theta = (theta + (360 + dTheta) % (360)) % (360); //keeps the angle within 360 degrees
        x = x + dX;                                         
        y = y + dY; 
      }

      // this ensures that the odometer only runs once every period
      updateEnd = System.currentTimeMillis();
      if (updateEnd - updateStart < ODOMETER_PERIOD) {
        try {
          Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
        } catch (InterruptedException e) {
          // Nothing to be done here, no other thread is should be able to interrupt this one
        }
      }
    }
  }




  /**
   * Method used to set the position of the robot on the odometer
   * @param position
   * @param update
   */
  public void setPosition(double[] position, boolean[] update) {
    // ensure that the values don't change while the odometer is running
    synchronized (lock) {
      if (update[0])
        x = position[0];
      if (update[1])
        y = position[1];
      if (update[2])
        theta = position[2];
    }
  }

  /**
   * Setter for X component lock to be mutually exclusive
   * @param x
   */
  public void setX(double x) {
    synchronized (lock) {
      this.x = x;
    }
  }
  /**
   * Setter for Y component lock to be mutually exclusive
   * @param y
   */
  public void setY(double y) {
    synchronized (lock) {
      this.y = y;
    }
  }

  /**
   * Setter for Theta component lock to be mutually exclusive
   * @param theta
   */
  public void setTheta(double theta) {
    synchronized (lock) {
      this.theta = theta;
    }
  }

  /**
   * Setter for the left motor tacho count
   * @param leftMotorTachoCount the leftMotorTachoCount to set
   */
  public void setLeftMotorTachoCount(int leftMotorTachoCount) {
    synchronized (lock) {
      this.leftMotorTachoCount = leftMotorTachoCount;   
    }
  }

  /**
   * Setter for the right motor tacho count
   * @param rightMotorTachoCount the rightMotorTachoCount to set
   */
  public void setRightMotorTachoCount(int rightMotorTachoCount) {
    synchronized (lock) {
      this.rightMotorTachoCount = rightMotorTachoCount; 
    }
  }

  /**
   * Getter method for the left motor tacho count
   * @return the leftMotorTachoCount
   */
  public int getLeftMotorTachoCount() {
    return leftMotorTachoCount;
  }



  /**
   * Getter method for the right motor tacho count
   * @return the rightMotorTachoCount
   */
  public int getRightMotorTachoCount() {
    return rightMotorTachoCount;
  }

  /**
   * Getter method for the position as calculated by the odometer
   * @param position
   * @param update
   */
  public void getPosition(double[] position, boolean[] update) {
    // ensure that the values don't change while the odometer is running
    synchronized (lock) {
      if (update[0])
        position[0] = x;
      if (update[1])
        position[1] = y;
      if (update[2])
        position[2] = theta;
    }
  }

  /**
   * Getter for X component
   * @return X component of the odometer
   */
  public double getX() {
    double result;

    synchronized (lock) {
      result = x;
    }

    return result;
  }

  /**
   * Getter for Y component
   * @return Y component of the odometer
   */
  public double getY() {
    double result;

    synchronized (lock) {
      result = y;
    }

    return result;
  }

  /**
   * Getter for Theta component
   * @return Theta component of the odometer
   */
  public double getTheta() {
    double result;

    synchronized (lock) {
      result = theta;
    }

    return result;
  }
}




