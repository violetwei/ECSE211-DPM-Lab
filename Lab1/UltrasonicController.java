package ca.mcgill.ecse211.wallfollowing;

//interface for the ultrasonic controller
/**
 * This interface will be implemented by both type of controllers
 * It will serve as a template for the methods to process the data of the ultrasonic sensor 
 * and the one to read the distance with the ultrasonic sensor
 * @author maxbo
 *
 */
public interface UltrasonicController {
	

		/**
		 * This method will be used to process the data of the ultrasonic sensor and determine the action that our robot should take
		 * @param distance
		 * @return void
		 */
	  public void processUSData(int distance);

		/**
		 * This method returns the distance calculated by the ultrasonic sensor
		 * @param distance
		 * @return distance calculated by the US sensor
		 */

	  public int readUSDistance();
	}
