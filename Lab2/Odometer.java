/**
 * This class is meant as a skeleton for the odometer class to be used.
 * package ca.mcgill.ecse211.odometer;

/**
 * This class is meant as a skeleton for the odometer class to be used.
 * 
 * @author Rodrigo Silva
 * @author Dirk Dubois
 * @author Derek Yu
 * @author Karim El-Baba
 * @author Michael Smith
 */


import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends OdometerData implements Runnable {

  private OdometerData odoData;
  private static Odometer odo = null; // Returned as singleton

  // Motors and related variables
  private int nowLeftMotorTachoCount; // Current tacho Left
  private int nowRightMotorTachoCount; // Current tacho Right
  private int lastTachoLeft; // Tacho Left at last sample
  private int lastTachoRight; // Tacho Right at last sample
  private double Theta; //Current orientation

  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;

  private double displacementLeft;
  private double displacementRight;
  
  
  
  
  private final double TRACK;
  private final double WHEEL_RAD;

  private double[] position;


  private static final long ODOMETER_PERIOD = 100; // odometer update period in ms

  /**
   * This is the default constructor of this class. It initiates all motors and variables once.It
   * cannot be accessed externally.
   * 
   * @param leftMotor
   * @param rightMotor
   * @throws OdometerExceptions
   */
  private Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
      final double TRACK, final double WHEEL_RAD) throws OdometerExceptions {
    odoData = OdometerData.getOdometerData(); // Allows access to x,y,z
                                              // manipulation methods
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;

    // Reset the values of x, y and z to 0
    odoData.setXYT(0, 0, 0);

    this.lastTachoLeft = 0;
    this.lastTachoRight = 0;
    this.Theta = 0;
    
    this.TRACK = TRACK;
    this.WHEEL_RAD = WHEEL_RAD;

  }

  /**
   * This method is meant to ensure only one instance of the odometer is used throughout the code.
   * 
   * @param leftMotor
   * @param rightMotor
   * @return new or existing Odometer Object
   * @throws OdometerExceptions
   */
  public synchronized static Odometer getOdometer(EV3LargeRegulatedMotor leftMotor,
      EV3LargeRegulatedMotor rightMotor, final double TRACK, final double WHEEL_RAD)
      throws OdometerExceptions {
    if (odo != null) { // Return existing object
      return odo;
    } else { // create object and return it
      odo = new Odometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
      return odo;
    }
  }

  /**
   * This class is meant to return the existing Odometer Object. It is meant to be used only if an
   * odometer object has been created
   * 
   * @return error if no previous odometer exists
   */
  public synchronized static Odometer getOdometer() throws OdometerExceptions {

    if (odo == null) {
      throw new OdometerExceptions("No previous Odometer exits.");

    }
    return odo;
  }

  /**
   * This method is where the logic for the odometer will run. Use the methods provided from the
   * OdometerData class to implement the odometer.
   */
  // run method (required for Thread)
  public void run() {
    long updateStart, updateEnd;

    while (true) {
      updateStart = System.currentTimeMillis();
      
      nowLeftMotorTachoCount = leftMotor.getTachoCount();
      nowRightMotorTachoCount = rightMotor.getTachoCount();
 

      double displacementX, displacementY, displacementTheta;
      double angle;
      double displacementChange;

      // Calculate the displacement of the left motor and right motor in centimeters
      displacementLeft = Math.PI*WHEEL_RAD*(nowLeftMotorTachoCount - lastTachoLeft)/180;
      displacementRight = Math.PI*WHEEL_RAD*(nowRightMotorTachoCount - lastTachoRight)/180;

      // Update the Tacho count
      lastTachoLeft = nowLeftMotorTachoCount;
      lastTachoRight = nowRightMotorTachoCount;

      // Calculate angle
      displacementChange = 0.5*(displacementLeft + displacementRight);
      angle = (displacementLeft - displacementRight) / TRACK;
      Theta += angle;

      //Change in displacement and angle
      displacementX = displacementChange*Math.sin(Theta);
      displacementY = displacementChange*Math.cos(Theta);
      displacementTheta = angle*180/Math.PI;
      
      position = odoData.getXYT();
      

      position[0]+= displacementX;
      position[1]+= displacementY;
      position[2]+=displacementTheta;
      
      
      // TODO Update odometer values with new calculated values
      odo.update(position[0], position[1], position[2]);

      // this ensures that the odometer only runs once every period
      updateEnd = System.currentTimeMillis();
      if (updateEnd - updateStart < ODOMETER_PERIOD) {
        try {
          Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
        } catch (InterruptedException e) {
          // there is nothing to be done
        }
      }
    }
  }

}

