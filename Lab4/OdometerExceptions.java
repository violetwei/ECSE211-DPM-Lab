package ca.mcgill.ecse211.lab4;

/**
 * This class is used to handle errors regarding the singleton pattern used for the odometer and
 * odometerData
 *
 * @author Maxime Bourassa
 * @author Violet Wei
 */

@SuppressWarnings("serial")

public class OdometerExceptions extends Exception {
  
 /**
  * constructor of the class
  * @param Error
  */
  public OdometerExceptions(String Error) {
    super(Error);
  }

}
