package ca.mcgill.ecse211.wallfollowing;


import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
/**
 * Printer class runs in a separate thread. 
 * It is used to display information about the program currently running
 * such as the type of controller in use and the distance to the wall
 * It doesn't give every distance caculated by the US poller, only some of them
 * @author maxbo
 *
 */
public class Printer extends Thread {

  //
  // In addition to the UltrasonicPoller, the printer thread also operates
  // in the background. Since the thread sleeps for 200 mS each time through
  // the loop, screen updating is limited to 5 Hz.
  //

  private UltrasonicController cont;
  private final int option;

  public Printer(int option, UltrasonicController cont) {
    this.cont = cont;
    this.option = option;
  }

  public static TextLCD t = LocalEV3.get().getTextLCD(); // n.b. how the screen is accessed

  /**
   * Run method. 
   * Starts a thread for the printer and displays information on the screen
   * Operates at a lower frequency than the US poller
   */
  public void run() {
    while (true) { // operates continuously
      t.clear();
      t.drawString("Controller Type is... ", 0, 0); // print header
      if (this.option == Button.ID_LEFT)
        t.drawString("BangBang", 0, 1);
      else if (this.option == Button.ID_RIGHT)
        t.drawString("P type", 0, 1);
      t.drawString("US Distance: " + cont.readUSDistance(), 0, 2); // print last US reading

      try {
        Thread.sleep(200); // sleep for 200 mS
      } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
      }
    }
  }

  /**
   * Method that handles printing on the EV3 screen
   * Clears the display every time before printing
   */
  public static void printMainMenu() { // a static method for drawing
    t.clear(); // the screen at initialization
    t.drawString("left = bangbang", 0, 0);
    t.drawString("right = p type", 0, 1);
  }
}
