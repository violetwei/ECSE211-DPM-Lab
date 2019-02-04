package ca.mcgill.ecse211.lab3;

import lejos.hardware.lcd.TextLCD;

/**
 * This class is used to display information on the EV3 screen
 * It is not necessary for this lab, but we still displayed the odometer information at all 
 * time to help debugging
 * This thread runs at 1.33 Hz
 * Based on lab 2 code
 * 
 * @author Maxime Bourassa
 * @author Violet Wei
 *
 */
public class Display extends Thread {
	//constants of the class
	private static final long DISPLAY_PERIOD = 750;
	private Odometer odometer;
	private TextLCD t;

	/**
	 * Constructor of the class, doesn't take in any arguments
	 * all parameters needed are constants from other classes 
	 * that will be accessed through getter methods
	 */
	public Display() {
		this.odometer = Lab3.getOdo();
		this.t = Lab3.getLCD();
	}

	/**
	 *  Run method, entry point of the thread
	 *  All the updates for the display will be done here
	 */
	public void run() {
		//parameters
		long displayStart, displayEnd;
		double[] position = new double[3];

		// clear the display 
		t.clear();

		//infinite loop, at each iteration, the display is updated
		while (true) {
			displayStart = System.currentTimeMillis(); //kep track of time

			// Erases previous info on the screen to make place for the new odometer values
			t.drawString("X:              ", 0, 0);
			t.drawString("Y:              ", 0, 1);
			t.drawString("T:              ", 0, 2);

			// get the odometry information
			odometer.getPosition(position, new boolean[] { true, true, true });

			// display odometry information
			for (int i = 0; i < 3; i++) {
				t.drawString(formattedDoubleToString(position[i], 2), 3, i);
			}

			// Make sure it only runs once per thread period
			displayEnd = System.currentTimeMillis();
			if (displayEnd - displayStart < DISPLAY_PERIOD) {
				try {
					Thread.sleep(DISPLAY_PERIOD - (displayEnd - displayStart));
				} catch (InterruptedException e) {
					//Nothing is done here as this thread should never be interrupted by other threads
				}
			}
		}
	}
	
	/**
	 * Method to display info on thescreen in a nice way
	 * 
	 * @param x
	 * @param places
	 * @return String to be displayed
	 */
	private static String formattedDoubleToString(double x, int places) {
		//parameters
		String result = "";
		String stack = "";
		long t;
		
		// put in a minus sign as needed
		if (x < 0.0)
			result += "-";
		
		// put in a leading 0
		if (-1.0 < x && x < 1.0)
			result += "0";
		else {
			t = (long)x;
			if (t < 0)
				t = -t;
			
			while (t > 0) {
				stack = Long.toString(t % 10) + stack;
				t /= 10;
			}
			
			result += stack;
		}
		
		// put the decimal, if needed
		if (places > 0) {
			result += ".";
		
			// put the appropriate number of decimals
			for (int i = 0; i < places; i++) {
				x = Math.abs(x);
				x = x - Math.floor(x);
				x *= 10.0;
				result += Long.toString((long)x);
			}
		}
		
		return result;
	}

}
