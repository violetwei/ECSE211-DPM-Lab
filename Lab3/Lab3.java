package ca.mcgill.ecse211.lab3;


//import ca.mcgill.ecse211.lab3.PController;
import ca.mcgill.ecse211.lab3.UltrasonicPoller;
//import ca.mcgill.ecse211.lab3.*;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**
 * This is the main class for our program. It starts the program
 * and it allows the user to select different options on how they want the run 
 * to work (either run the demo or our test programs)
 * It has been slightly modified from the version given in the lab 2 documents to fit this lab
 * 
 * @author Maxime Bourassa
 * @author Violet Wei
 *
 */
public class Lab3 {

  // Motor Objects, and Robot related constant parameters
  //These all have getters to be accessed from other classes
  private static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
  private static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  private static final EV3MediumRegulatedMotor sensorMotor =
	      new EV3MediumRegulatedMotor(LocalEV3.get().getPort("B"));
  private static final TextLCD LCD = LocalEV3.get().getTextLCD();
  private static Odometer odometer = new Odometer();
  private static final double WHEEL_RAD = 2.13;
  private static final double TRACK = 10.1;
  
  //The four different maps are entered here and only the one being currently use in not in comments
  
  //public static double[][] four_points = {{0.0,2.0},{1.0,1.0},{2.0,2.0},{2.0,1.0},{1.0,0.0}};
  //public static double[][] four_points = {{1.0,1.0},{0.0,2.0},{2.0,2.0},{2.0,1.0},{1.0,0.0}};
  public static double[][] four_points = {{1.0,0.0},{2.0,1.0},{2.0,2.0},{0.0,2.0},{1.0,1.0}}; 	//last turn doesn't work, go look in turn conditions!!
  //public static double[][] four_points = {{0.0,1.0},{1.0,2.0},{1.0,0.0},{2.0,1.0},{2.0,2.0}};
  
 
  private static final Port usPort = LocalEV3.get().getPort("S2");
  //static PController pController;
  static UltrasonicPoller usPoller; // the selected controller on each cycle.
 
  
 
  /**
   * Main entry of the program. Starts the run and lets the user select between
   * different options
   * 
   * @param args
   * @throws OdometerExceptions
   */
public static void main(String[] args) throws OdometerExceptions {  
	  
	//Parameter used in the method
    int buttonChoice;
                                                                      // implementation
    Display odometryDisplay = new Display(); // No need to change
    
    @SuppressWarnings("resource") // Because we don't bother to close this resource
    SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
    SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples provider from
                                                              // this instance
    float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data is
                                                         // returned in

    usPoller = null; //for now, will be initialized after the user chose an option

    do {
      // clear the display
      LCD.clear();

      // ask the user what program he wants to run
      LCD.drawString("   Press enter          ", 0, 0);
      LCD.drawString("    to start            ", 0, 1);
      LCD.drawString("   the program          ", 0, 2);
      LCD.drawString("                        ", 0, 3);
      LCD.drawString(" Left to test dist      ", 0, 4);
      LCD.drawString("    Press down          ", 0, 5);
      LCD.drawString(" to test rotation       ", 0, 6);

      buttonChoice = Button.waitForAnyPress(); //wait for the user
    } while (buttonChoice != Button.ID_ENTER && buttonChoice != Button.ID_DOWN && buttonChoice != Button.ID_LEFT); //Exit when a valid option has been selected
    

    // Start odometer and display threads
    Thread odoThread = new Thread(odometer);
    odoThread.start();
    Thread odoDisplayThread = new Thread(odometryDisplay);
    odoDisplayThread.start();

    if (buttonChoice == Button.ID_ENTER) {
      // clear the display
      LCD.clear();
      usPoller = new UltrasonicPoller(usDistance, usData); //initialize the ultrasonic poller
      NavigationAvoidance navigation = new NavigationAvoidance(usDistance, usData);
      navigation.start(); //starts the navigation thread
      
    } else if (buttonChoice == Button.ID_DOWN) {
    	//testing the rotation
    	LCD.clear();
    	usPoller = new UltrasonicPoller(usDistance, usData);
    	(new Thread() {
            public void run() {
                 TestClass.rotate360();
            }
          }).start(); //starts the thread
    } else if (buttonChoice == Button.ID_LEFT) {
    	
    	LCD.clear();
    	usPoller = new UltrasonicPoller(usDistance, usData);
    	(new Thread() {
            public void run() {
                 TestClass.forward_2_tiles();
            }
          }).start();
    }
    
    	usPoller.start(); //start whatever has been selected

    while (Button.waitForAnyPress() != Button.ID_ESCAPE); //to end program
    System.exit(0);
  }

/**
 * Getter method for the left motor
 * 
 * @return leftMotor
 */
public static EV3LargeRegulatedMotor getLeftMotor(){
	  return leftMotor;
}

/**
 * Getter method for the right motor
 * 
 * @return rightMotor
 */
public static EV3LargeRegulatedMotor getRightMotor(){
	  return rightMotor;
}

/**
 * Getter method for the sensor motor
 * 
 * @return sensorMotor
 */
public static EV3MediumRegulatedMotor getSensorMotor(){
	  return sensorMotor;
}

/**
 * Getter method for the radius of the wheels
 * 
 * @return WHEEL_RAD
 */
public static double getWR() {
	  return WHEEL_RAD;
}

/**
 * Getter method for the wheel base of the robot
 * 
 * @return TRACK
 */
public static double getTrack() {
	  return TRACK;
}

/**
 * Getter method for the odometer instance
 * 
 * @return odometer
 */
public static Odometer getOdo(){
	  return odometer;
}

/**
 * Getter method for the LCD screen of the EV3
 * 
 * @return LCD
 */
public static TextLCD getLCD(){
	  return LCD;
}

  
}







 

 


