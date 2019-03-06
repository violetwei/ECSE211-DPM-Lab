package ca.mcgill.ecse211.lab4;

//To make our code easier to read and not repeat ourselves
import static ca.mcgill.ecse211.lab4.Lab4.*;
import lejos.hardware.Sound;

/**
 * This is the class used to perform the ultrasonic localization of our robot.
 * It implements both the rising edge and falling edge type.
 * At the end of this part, the robot should be facing almost zero degrees.
 * It runs in a thread
 * 
 * {@value #MAX_DISTANCE} Constant. Maximum distance used to detect when there is no wall in the filter for the falling edge type
 * {@value #SMALL_DISTANCE} Constant. Used in the filter for the rising edge type, helps to remove false negatives better than a higher value
 * {@value #WALL_DISTANCE} Constant. Value under which the robot is assumed to be facing a wall
 * {@value #MARGIN_DIsTANCE} Constant. Value used to remove noise when detecting the edge of the wall
 * {@value #FILTER_OUT} Constant. Number of constant large values we need in a row before considering a value to be actually large in our filter
 * {@value #filterControl} Class variable to be incremented in our filter to remove false negatives
 * {@value #lastDistance} Class variable used in our filter to keep track of the last distance returned by the ultrasonic sensor
 * {@value #locType} Class variable. Localization type that needs to be specified when an instance of the class is created
 *
 * 
 * @author Maxime Bourassa
 * @author Violet Wei
 */
public class UltrasonicLocalizer extends Thread {

  private LocalizationType locType;
  private static final int MAX_DISTANCE = 50;
  private static final int SMALL_DISTANCE = 31;
  private static final float WALL_DISTANCE = 30;
  private static final float MARGIN_DISTANCE =(float) 0.5; 
  private static int FILTER_OUT = 10;
  private int filterControl;
  private float lastDistance;

  /**
   * Constructor of the class. Takes in only the localization type 
   * as everything else we need are constants from the Lab4 class
   * @param locType
   */
  public UltrasonicLocalizer(LocalizationType locType) {
    this.locType = locType;
  }

  /**
   * Entry point of the thread. Will run either falling or
   * rising edge localization. All the heavy lifting of the
   * class is done in this method
   */
  public void run() {
    //variable used to start an average of the values we get in our noise margin
    boolean noiseZone = false;
    //first angle before noise margin
    double firstangle = 0;
    //First angle at the end of the noise margin
    double firstangle2 = 0;
    //second angle before noise margin
    double secondangle = 0;
    //Second angle at the end of the noise margin
    double secondangle2 = 0;
    //If falling edge has been selected, continue
    if(locType == LocalizationType.FALLING_EDGE) {
      //Calls our navigation method to set
      Nav.setSpeeds(SPEED);
      //if the robot starts facing a wall, turn until there is no longer a wall
      while (getFilter(MAX_DISTANCE)<=WALL_DISTANCE) {
        //turns the robot in a clockwise direction
        Nav.turnClockwise();
      }
      // When the robot sees a wall, record the angle at the beginning and at the end of the noise margin
      while (true) {
        Nav.turnClockwise();
        //Used to detect the angle before the noise margin
        if (!noiseZone && getFilter(MAX_DISTANCE) <= WALL_DISTANCE + MARGIN_DISTANCE) {
          //get it from the odometer
          firstangle = odometer.getTheta();
          //we are now in the noise margin
          noiseZone = true;
          //records the angle at the end of the noise margin
        } else if ( getFilter(MAX_DISTANCE) >= WALL_DISTANCE - MARGIN_DISTANCE && noiseZone) {
          firstangle2 = odometer.getTheta();
        } else if(noiseZone) {
          //We passed through the noise zone, put the boolean variable back to false
          noiseZone = false;
          //Exit the loop
          break;
        }
      }
      //Make a sound. Helps for debugging
      Sound.beep();
      //If we recorded an angle in the noise margin, continue
      if(firstangle2!=0) {
        //Average the values to eliminate noise
        firstangle = (firstangle+firstangle2)/2.0;
        //We passed through the loop without seeing anything. Happens very rarely but better be safe than sorry
      } else if(firstangle == 0) {
        //We have no choice to get the current angle as the edge of the wall. Not perfect but better than nothing
        firstangle = odometer.getTheta();
      }

      // Change direction and wait until it sees no wall
      while (getFilter(MAX_DISTANCE)<=WALL_DISTANCE) {
        //turns in the anticlockwise direction
        Nav.turnAnticlockwise();
      }
      // When the robot sees a wall, record the second angle
      //Same code as for the first angle
      while (true) {
        Nav.turnAnticlockwise();
        if (!noiseZone && getFilter(MAX_DISTANCE) <= WALL_DISTANCE + MARGIN_DISTANCE) {
          secondangle = odometer.getTheta();
          noiseZone = true;
        } else if ( getFilter(MAX_DISTANCE) >= WALL_DISTANCE - MARGIN_DISTANCE && noiseZone) {
          secondangle2 = odometer.getTheta();
        }else if(noiseZone) {
          noiseZone = false;
          break;
        }
      }
      Sound.beep();
      if(secondangle2!=0) {
        secondangle = (secondangle+secondangle2)/2.0;
      } else if(secondangle == 0) {
        secondangle = odometer.getTheta();
      }
      //Both angle have been recorded, stop the motors
      Nav.stopMotors();
      //If the second angle is bigger than the first, subtract a full turn so the average of the first and
      //second angle is 45 degrees
      if(secondangle > firstangle) {
        secondangle -= 360;
      }
      //Average the angle to get the angle we need to turn to face 45 degrees
      double averageAngle = (firstangle + secondangle)/2.0;
      //Get the angle to zero degrees from our current angle
      //Subtract 45 degrees from the average to get the 0 degrees
      //instead of the average
      double zeroangle =  averageAngle - 45 - odometer.getTheta();
      //Turn to that angle
      Nav.turnTo(zeroangle);
      //We can now set the odometer to (0,0,0) before the light localization
      odometer.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
      //We will do rising edge
    } else {
      //To make sure that as the sensor starts it doesn't sees that as a rising edge
      lastDistance = 255;
      //Set the speed of the motors
      Nav.setSpeeds(SPEED);
      //While there is no wall turn to the right
      while (getFilter(SMALL_DISTANCE)>=WALL_DISTANCE - MARGIN_DISTANCE) {
        Nav.turnClockwise();
      }
      //Record the first angle
      while (true) {
        Nav.turnClockwise();
        //Before noise
        if (!noiseZone && getFilter(SMALL_DISTANCE) >= WALL_DISTANCE - MARGIN_DISTANCE ) {
          firstangle = odometer.getTheta();
          //we have now entered the noise margin
          noiseZone = true;
          //In the noise margin, continue
        } else if ( getFilter(SMALL_DISTANCE) <= WALL_DISTANCE + MARGIN_DISTANCE && noiseZone) {
          firstangle2 = odometer.getTheta();
          //We have exited the noise margin
        } else if(noiseZone) {
          noiseZone = false;
          //Exit the loop
          break;
        }
      }
      Sound.beep();
      //Average the angle before and after the noise margin
      if(firstangle2!=0) {
        firstangle = (firstangle+firstangle2)/2.0;
      } else if(firstangle == 0) {
        firstangle = odometer.getTheta();
      }
      //Change direction and wait there is a wall
      while (getFilter(SMALL_DISTANCE)>=WALL_DISTANCE - MARGIN_DISTANCE) {
        Nav.turnAnticlockwise();
      }
      // Same as for the first angle, record the angle at the edge of the wall
      lastDistance = 0;
      while (true) {
        Nav.turnAnticlockwise();
        if (!noiseZone && getFilter(SMALL_DISTANCE) >= WALL_DISTANCE - MARGIN_DISTANCE) {
          secondangle = odometer.getTheta();
          noiseZone = true;
        } else if ( getFilter(SMALL_DISTANCE) <= WALL_DISTANCE + MARGIN_DISTANCE && noiseZone){
          secondangle2 = odometer.getTheta();
        }else if(noiseZone) {
          noiseZone = false;
          break;
        }
      }
      Sound.beep();
      if(secondangle2!=0) {
        secondangle = (secondangle+secondangle2)/2.0;
      } else if(secondangle == 0) {
        secondangle = odometer.getTheta();
      }
      //stop both motors
      Nav.stopMotors();
      //If the first angle is bigger, wrap it around so the average 
      //of both angle is 45 degrees
      if(firstangle > secondangle) {
        firstangle -= 360;
      }
      //calculate the average angle
      double averageAngle = (firstangle + secondangle)/2.0;
      double zeroangle =  averageAngle - 45 - odometer.getTheta();
      //Turn to zero degrees
      Nav.turnTo(zeroangle);
      //reset the odometer to 0
      odometer.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
    }
  }

  /**
   * Method to filter out wrong readings from the ultrasonic sensor
   * Wait until it sees large distances 10 times in a row before actually
   * accepting it as a large distance
   * @param max_dist The maximal distance before a distance has to be filtered for false negative
   * @return result the distance after being filtered
   */
  private float getFilter(int max_dist) {
    //Gets data from the ultrasonic sensor
    usSensor.fetchSample(usData, 0);
    float distance = (float)(usData[0]*100.0);
    float result = 0;
    //false negative, throw that value away
    if (distance > max_dist && filterControl < FILTER_OUT) {
      //increase to keep track of repeated large values
      filterControl ++;
      //will return the previous value instead
      result = lastDistance;
      //true large value, return it
    } else if (distance > max_dist){
      result = max_dist; //clips it at 50
      //its a small value, reset the filter and return it
    } else {
      filterControl = 0;
      result = distance;
    }
    //records the last distance given by the ultrasonic sensor
    lastDistance = distance;
    return result;
  }

}


