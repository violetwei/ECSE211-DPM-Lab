package ca.mcgill.ecse211.lab4;

/**
 * class that performs the two ultrasonic localization routines: FallingEdge and RisingEdge
 */
 
 /* The method that performs the falling edge localization */
 
 
 
 /* The method that performs the rising edge localization */

//Avoid duplicate variables and make the code easier to read
import static ca.mcgill.ecse211.lab4.Lab4.*;

public class UlatrasonicLocolizer {
  private static double distance;
  

  public void risingEdgeLcolization() {
  //parameters
    int FILTER_OUT = 25;
    int filterControl = 0;
    
    
    fetchSample(usData,0);           // acquire data from the ultrasonic sensor
    distance=(int)(usData[0]*100.0);  // extract from buffer, cast to int
    Simplefilter(distance);               //Filter the distance received
  }
  
  
  public void Simplefilter(int distance){

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
