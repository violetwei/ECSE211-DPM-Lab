package ca.mcgill.ecse211.lab4;

import static ca.mcgill.ecse211.lab4.Lab4.leftMotor;
import static ca.mcgill.ecse211.lab4.Lab4.rightMotor;
import static ca.mcgill.ecse211.lab4.Lab4.SPEED;
import static ca.mcgill.ecse211.lab4.Lab4.odometer;
import static ca.mcgill.ecse211.lab4.Lab4.Nav;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class LightLocalizer extends Thread {

  public static final Port colorSampler = LocalEV3.get().getPort("S2");
  public SensorModes colosSamplerSensor = new EV3ColorSensor(colorSampler);
  public SampleProvider colorSensorValue = colosSamplerSensor.getMode("Red");
  public float[] colorSensorData = new float[colosSamplerSensor.sampleSize()];
  private float oldValue = 0;
  private static final double DISTANCE_FROM_CENTER = 6.5;
  long correctionStart, correctionEnd;
  private int DIFF_THREASHOLD = -25;

  private static final long CORRECTION_PERIOD = 50;
  public void run() {
    boolean firsttime = true;



    Nav.setSpeeds(SPEED);
    Nav.turnTo(45);

    leftMotor.forward();
    rightMotor.forward();





    while (true) {
      correctionStart = System.currentTimeMillis();

      //fetching the values from the color sensor
      colorSensorValue.fetchSample(colorSensorData, 0);

      //getting the value returned from the sensor, and multiply it by 1000 to scale
      float value = colorSensorData[0]*1000;

      //computing the derivative at each point
      float diff = value - oldValue;

      //storing the current value, to be able to get the derivative on the next iteration
      oldValue = value;

      //if the derivative value at a given point is less than -50, this means that a black line is detected
      if(diff < DIFF_THREASHOLD) {

        //robot beeps
        Sound.beep();

        leftMotor.stop(true);
        rightMotor.stop(true);

        if(firsttime) {
          Nav.setSpeeds(SPEED);
          leftMotor.rotate(Nav.convertDistance(DISTANCE_FROM_CENTER),true);
          rightMotor.rotate(Nav.convertDistance(DISTANCE_FROM_CENTER),false);
          firsttime = false;


          precorrect();
          break;
        }



      }




      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
          // expected that the odometry correction will be
          // interrupted by another thread
        }
      }
    }

  }

  private void precorrect() {

    Nav.turnTo(-120);
    leftMotor.backward();
    rightMotor.forward();
    while(true) {
      correctionStart = System.currentTimeMillis();
      //fetching the values from the color sensor
      colorSensorValue.fetchSample(colorSensorData, 0);

      //getting the value returned from the sensor, and multiply it by 1000 to scale
      float value = colorSensorData[0]*1000;

      //computing the derivative at each point
      float diff = value - oldValue;

      //storing the current value, to be able to get the derivative on the next iteration
      oldValue = value;


      //if the derivative value at a given point is less than -50, this means that a black line is detected
      if(diff < DIFF_THREASHOLD) {

        //robot beeps
        Sound.beep();

        leftMotor.stop(true);
        rightMotor.stop(true);

        if (odometer.getTheta() < 285 && odometer.getTheta() > 195) {

          correctX();
          break;

        } else if (odometer.getTheta() > 165) {



          correctY();
          break;

        }
      }
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
          // expected that the odometry correction will be
          // interrupted by another thread
        }
      }

    }
  }

  private void correctX() {

    boolean firsttime = true;
    boolean secondtime = false;
    double distanceCorr = 0;
    leftMotor.forward();
    rightMotor.backward();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // there is nothing to be done here because it is not
      // expected that the odometry correction will be
      // interrupted by another thread
    }
    while(true) {
      correctionStart = System.currentTimeMillis();
      //fetching the values from the color sensor
      colorSensorValue.fetchSample(colorSensorData, 0);

      //getting the value returned from the sensor, and multiply it by 1000 to scale
      float value = colorSensorData[0]*1000;

      //computing the derivative at each point
      float diff = value - oldValue;

      //storing the current value, to be able to get the derivative on the next iteration
      oldValue = value;


      //if the derivative value at a given point is less than -50, this means that a black line is detected
      if(diff < DIFF_THREASHOLD) {

        if(firsttime) {
          //robot beeps
          Sound.beep();

          distanceCorr = Math.sin(odometer.getTheta()*Math.PI/180)*DISTANCE_FROM_CENTER;

          leftMotor.forward();
          rightMotor.backward();

          firsttime = false;
          secondtime = true;
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            // there is nothing to be done here because it is not
            // expected that the odometry correction will be
            // interrupted by another thread
          }

        } else if(secondtime) {
          Sound.beep();
          leftMotor.stop(true);
          rightMotor.stop(true);
          leftMotor.forward();
          rightMotor.forward();
          leftMotor.rotate(Nav.convertDistance(distanceCorr),true);
          rightMotor.rotate(Nav.convertDistance(distanceCorr),false);
          leftMotor.backward();
          rightMotor.forward();
          secondtime = false;



        } else {

          Nav.turnTo(-90-3);
          leftMotor.stop(true);
          rightMotor.stop(true);
          odometer.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
          break;
        }

      }
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
          // expected that the odometry correction will be
          // interrupted by another thread
        }
      }
    }

  }


  private void correctY() {
    boolean firsttime = true;
    boolean secondtime = false;
    double distanceCorr = 0;
    leftMotor.backward();
    rightMotor.forward();
    odometer.setTheta(180);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // there is nothing to be done here because it is not
      // expected that the odometry correction will be
      // interrupted by another thread
    }

    while(true) {
      correctionStart = System.currentTimeMillis();
      //fetching the values from the color sensor
      colorSensorValue.fetchSample(colorSensorData, 0);

      //getting the value returned from the sensor, and multiply it by 1000 to scale
      float value = colorSensorData[0]*1000;

      //computing the derivative at each point
      float diff = value - oldValue;

      //storing the current value, to be able to get the derivative on the next iteration
      oldValue = value;


      //if the derivative value at a given point is less than -50, this means that a black line is detected
      if(diff < DIFF_THREASHOLD) {

        if(firsttime) {
          //robot beeps
          Sound.beep();

          distanceCorr = Math.cos(odometer.getTheta()*Math.PI/180)*DISTANCE_FROM_CENTER;

          firsttime = false;
          secondtime = true;
          try {
            Thread.sleep(1000);
          } catch (Exception e) {
          } // Poor man's timed sampling

        } else if(secondtime) {

          Sound.beep();
          leftMotor.stop(true);
          rightMotor.stop(true);
          leftMotor.forward();
          rightMotor.forward();
          leftMotor.rotate(Nav.convertDistance(distanceCorr),true);
          rightMotor.rotate(Nav.convertDistance(distanceCorr),false);
          odometer.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
          break;
        }
      }
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
          // expected that the odometry correction will be
          // interrupted by another thread
        }
      }
    }   
  }
}
