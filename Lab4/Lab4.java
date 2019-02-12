package ca.mcgill.ecse211.lab4;

  import lejos.hardware.Button;
  import lejos.hardware.ev3.LocalEV3;
  import lejos.hardware.lcd.TextLCD;
  import lejos.hardware.motor.EV3LargeRegulatedMotor;
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
   * {@value #leftMotor}
   * {@value #rightMotor}
   * {@value #WHEEL_RAD}
   * {@value #TRACK}
   * {@value #SPEED}
   * {@value #usPort}
   * 
   * @author Maxime Bourassa
   * @author Violet Wei
   *
   */
  public class Lab4 {

    // Motor Objects, and Robot related constant parameters
    //These all have getters to be accessed from other classes
    public static final EV3LargeRegulatedMotor leftMotor =
        new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
    public static final EV3LargeRegulatedMotor rightMotor =
        new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
    public static NavigationLab4 Nav = new NavigationLab4();
    public static final TextLCD LCD = LocalEV3.get().getTextLCD();
    public static Odometer odometer = new Odometer();
    public static final double WHEEL_RAD = 2.1;
    public static final double TRACK = 11.1;
    public static final int SPEED = 75;

    public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
    
    private static final Port usPort = LocalEV3.get().getPort("S1");
    @SuppressWarnings("resource") // Because we don't bother to close this resource
    public static SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
    public static SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples provider from this instance
    public static float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data is
                                                                       // returned in

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
      DisplayLab4 odometryDisplay = new DisplayLab4(); // No need to change

      do {
        // clear the display
        LCD.clear();

        // ask the user what program he wants to run
        LCD.drawString("   Press left        ", 0, 0);
        LCD.drawString(" for Falling edge    ", 0, 1);
        LCD.drawString("   Press right       ", 0, 2);
        LCD.drawString(" for Rising edge     ", 0, 3);
        LCD.drawString(" Left to test dist   ", 0, 4);
        LCD.drawString("    Press down       ", 0, 5);
        LCD.drawString(" to test rotation    ", 0, 6);

        buttonChoice = Button.waitForAnyPress(); //wait for the user
      } while (buttonChoice != Button.ID_ENTER && buttonChoice != Button.ID_DOWN && buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT); //Exit when a valid option has been selected


      // Start odometer and display threads
      Thread odoThread = new Thread(odometer);
      odoThread.start();
      Thread odoDisplayThread = new Thread(odometryDisplay);
      odoDisplayThread.start();

      if (buttonChoice == Button.ID_ENTER) {
        
        LCD.clear();
        (new Thread() {
          public void run() {
            TestClass.forward2Tiles();
          }
        }).start();
      } 
      
      else if (buttonChoice == Button.ID_DOWN) {
        //testing the rotation
        LCD.clear();
        (new Thread() {
          public void run() {
            TestClass.rotate360();
          }
        }).start(); //starts the thread
      } 
      else if (buttonChoice == Button.ID_LEFT) {

     // clear the display
        LCD.clear();
        
        UltrasonicLocalizer USL = new UltrasonicLocalizer(LocalizationType.FALLING_EDGE);
        USL.start();
        
        Button.waitForAnyPress();
        
        LightLocalizer LightLoc = new LightLocalizer();
        LightLoc.start();
        
      }else if (buttonChoice == Button.ID_RIGHT) {

        // clear the display
           LCD.clear();
           
           UltrasonicLocalizer USL = new UltrasonicLocalizer(LocalizationType.RISING_EDGE);
           USL.start();
           
           Button.waitForAnyPress();
           
           LightLocalizer LightLoc = new LightLocalizer();
           LightLoc.start();
           
         }
      while (Button.waitForAnyPress() != Button.ID_ESCAPE); //to end program
      System.exit(0);
    }
    
  }














