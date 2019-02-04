package ca.mcgill.ecse211.lab3;


import lejos.robotics.SampleProvider;

/**
 * Control of the wall follower is applied periodically by the UltrasonicPoller thread. The while
 * loop at the bottom executes in a loop. Assuming that the us.fetchSample, and cont.processUSData
 * methods operate in about 20mS, and that the thread sleeps for 50 mS at the end of each loop, then
 * one cycle through the loop is approximately 70 mS. This corresponds to a sampling rate of 1/70mS
 * or about 14 Hz.
 */
public class UltrasonicPoller extends Thread {
  private SampleProvider us;

  private float[] usData;
  private static final long POLLER_PERIOD = 50; // poller update period in ms
  
  /**
   * Constructor used to create a new instance of this class
   * Only one instance will be created in our program
   * @param us
   * @param usData
   * @param cont
   */
  public UltrasonicPoller(SampleProvider us, float[] usData) {
    this.us = us;

    this.usData = usData;
  }

  /*
   * Sensors now return floats using a uniform protocol. Need to convert US result to an integer
   * [0,255] (non-Javadoc)
   * 
   * @see java.lang.Thread#run()
   */
  
  /**
   * Run method starts a thread for this instance of an ultrasonic poller
   * Runs in a loop to retrieve data at a frequency of approximately 14Hz
   */
  public void run() {
	int distance;
    long updateStart, updateEnd;
    
    while (true) {
      updateStart = System.currentTimeMillis();
      
    /*  if(Lab3.pController.is_obstacle) {
    	  Lab3.leftMotor.stop();
    	  Lab3.rightMotor.stop();
    	  driver_running = false;
    	  
      } else if (!PController.is_obstacle && !driver_running) {
    	  driver_running = true;
    	  
    	  
      }*/
    	
      us.fetchSample(usData, 0); // acquire data
      distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
      //cont.processUSData(distance); // now take action depending on value
      // this ensures that the odometer only runs once every period
      updateEnd = System.currentTimeMillis();
      if (updateEnd - updateStart < POLLER_PERIOD) {
        try {
          Thread.sleep(POLLER_PERIOD - (updateEnd - updateStart));
        } catch (InterruptedException e) {
          // there is nothing to be done
        }
    }
  }
}
  
  public int getDistance() {
	  us.fetchSample(usData, 0); // acquire data
      return (int) (usData[0] * 100.0); // extract from buffer, cast to int
	  
  }
}
