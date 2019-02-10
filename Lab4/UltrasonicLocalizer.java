package ca.mcgill.ecse211.lab4;



import static ca.mcgill.ecse211.lab4.Lab4.*;
import lejos.hardware.Sound;


  public class UltrasonicLocalizer extends Thread {
      

      
      private LocalizationType locType;
      
      
      private boolean noiseZone = false;
      private static final int MAX_DISTANCE = 50;
      private static final int SMALL_DISTANCE = 31;
      private static final float WALL_DISTANCE = 30;
      private static final float MARGIN_DISTANCE =(float) 0.5; 
      
      
      private static int FILTER_OUT = 10;
      private int filterControl;
      private float lastDistance;
      
      public UltrasonicLocalizer(LocalizationType locType) {
          this.locType = locType;
      }
      
      public void run() {
        
          double firstangle = 0;
          double secondangle = 0;
          double firstangle2 = 0;
          double secondangle2 = 0;

          
          if(locType == LocalizationType.FALLING_EDGE) {
              
            setSpeeds(SPEED);
            
             
              
              while (getFilteredData()<=WALL_DISTANCE) {
                  
                turnClockwise();
       
              }
              // keep rotating until the robot sees a wall, then latch the angle
              
              
              while (true) {
                turnClockwise();
                if (!noiseZone && getFilteredData() <= WALL_DISTANCE + MARGIN_DISTANCE) {
                  firstangle = odometer.getTheta();
                  noiseZone = true;
                  Sound.beep();
              } else if ( getFilteredData() >= WALL_DISTANCE - MARGIN_DISTANCE && noiseZone){
                  firstangle2 = odometer.getTheta();
                  Sound.beep();
                  
                  
              } else if(noiseZone) {
                noiseZone = false;
                break;
              }
                  
              }
              //Sound.beep();
              if(firstangle2!=0) {
                firstangle = (firstangle+firstangle2)/2.0;
              } else if(firstangle == 0) {
                firstangle = odometer.getTheta();
              }
              
              // switch direction and wait until it sees no wall

              while (getFilteredData()<=WALL_DISTANCE) {
                
                turnAnticlockwise();
   
          }
              
              // keep rotating until the robot sees a wall, then latch the angle

              while (true) {
                turnAnticlockwise();
                if (!noiseZone && getFilteredData() <= WALL_DISTANCE + MARGIN_DISTANCE) {
                  secondangle = odometer.getTheta();
                  noiseZone = true;
                  Sound.beep();
              } else if ( getFilteredData() >= WALL_DISTANCE - MARGIN_DISTANCE && noiseZone){
                  secondangle2 = odometer.getTheta();
                  Sound.beep();
              }else if(noiseZone) {
                noiseZone = false;
                break;
              }
                  
              }
              
              
              if(secondangle2!=0) {
                secondangle = (secondangle+secondangle2)/2.0;
              } else if(secondangle == 0) {
                secondangle = odometer.getTheta();
              }
              
              rightMotor.stop(true);
              leftMotor.stop(true);
              
              
              if(secondangle > firstangle){
                  secondangle -= 360;
              }
              
              double averageAngle = (firstangle + secondangle)/2.0;
              double ZeroPoint =  averageAngle - 45 - odometer.getTheta();

              
              turnTo(ZeroPoint);
              
              System.out.println("\n\n\n"+(int)firstangle+" "+(int)secondangle+" "+(int)averageAngle);
              System.out.println((int)firstangle2+" "+(int)secondangle2);
              
              //odometer.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
      
              
          } else {
            lastDistance = 255;

            setSpeeds(SPEED);
            
             
              
              while (getData()>=WALL_DISTANCE - MARGIN_DISTANCE) {
                  
                turnClockwise();
       
              }
              // keep rotating until the robot stops seing a wall, then latch the angle
              
              lastDistance = 0;
              while (true) {
                turnClockwise();
                if (!noiseZone && getData() >= WALL_DISTANCE - MARGIN_DISTANCE ) {
                  firstangle = odometer.getTheta();
                  noiseZone = true;
                  
              } else if ( getData() <= WALL_DISTANCE + MARGIN_DISTANCE && noiseZone){
                  firstangle2 = odometer.getTheta();
                  
              } else if(noiseZone) {
                noiseZone = false;
                break;
              }
              }
              
              Sound.beep();
              if(firstangle2!=0) {
                firstangle = (firstangle+firstangle2)/2.0;
              } else if(firstangle == 0) {
                firstangle = odometer.getTheta();
              }
              
              // switch direction and wait until it sees a wall
              
              

              while (getData()>=WALL_DISTANCE - MARGIN_DISTANCE) {
                
                turnAnticlockwise();
          }
              
              // keep rotating until the robot sees a wall, then latch the angle
              

              lastDistance = 0;
              while (true) {
                turnAnticlockwise();
                if (!noiseZone && getData() >= WALL_DISTANCE - MARGIN_DISTANCE) {
                  secondangle = odometer.getTheta();
                  noiseZone = true;
                  
              } else if ( getData() <= WALL_DISTANCE + MARGIN_DISTANCE && noiseZone){
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
              
              rightMotor.stop(true);
              leftMotor.stop(true);
              
              
              if(firstangle > secondangle){
                  firstangle -= 360;
              }
              
              double averageAngle = (firstangle + secondangle)/2.0;
              double ZeroPoint =  averageAngle - 45 - odometer.getTheta();

             
              turnTo(ZeroPoint);
              System.out.println("\n\n\n"+(int)firstangle+" "+(int)secondangle+" "+(int)averageAngle);
              System.out.println((int)firstangle2+" "+(int)secondangle2);
              
              //odometer.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
      
            }
              
          
      }
      
      private void setSpeeds(int speed) {
        leftMotor.setSpeed(speed);
        rightMotor.setSpeed(speed);
      }
      
      private void turnClockwise() {
        leftMotor.forward();
        rightMotor.backward();
      }
      
      private void turnAnticlockwise() {
        leftMotor.backward();
        rightMotor.forward();
      }
      
      
      private float getFilteredData() {
        usSensor.fetchSample(usData, 0);
        float distance = (float)(usData[0]*100.0);
        float result = 0;
        if (distance > MAX_DISTANCE && filterControl < FILTER_OUT) {
            // bad value, do not set the distance var, however do increment the filter value
            filterControl ++;
            result = lastDistance;
        } else if (distance > MAX_DISTANCE){
            // true 255, therefore set distance to 255
            result = MAX_DISTANCE; //clips it at 50
        } else {
            // distance went below 255, therefore reset everything.
            filterControl = 0;
            result = distance;
        }
        lastDistance = distance;
        return result;
    }
      
      private float getData() {
        usSensor.fetchSample(usData, 0);
        float distance = (float)(usData[0]*100.0);
        float result = 0;
        if (distance > SMALL_DISTANCE && filterControl < FILTER_OUT) {
            // bad value, do not set the distance var, however do increment the filter value
            filterControl ++;
            result = lastDistance;
        } else if (distance > SMALL_DISTANCE){
            // true 255, therefore set distance to 255
            result = SMALL_DISTANCE; //clips it at 50
        } else {
            // distance went below 255, therefore reset everything.
            filterControl = 0;
            result = distance;
        }
        lastDistance = distance;
        return result;
      }

      
      
      public void turnTo(double theta) { //method from navigation program
        //System.out.println("\n\n\n"+odometer.getT());
        double angle = SmallestAngle(theta);

        leftMotor.rotate(convertAngle(angle),true);
        rightMotor.rotate(-convertAngle(angle),false);

      }

      /**
       * Method to find if the smallest angle is achieved
       * by turning left or right
       * 
       * @param angle
       * @return
       */
      public double SmallestAngle(double angle){
        if (angle > 180) {
          angle =  angle - 360;
        } else if (angle < -180) {
          angle = angle + 360;
        }
        return angle; //the minimal angle
      }
      
      /**
       * Method from lab 2 to transform a distance into wheel rotations
       * @param : double distance 
       * @return: amount of degrees the motors have to turn to traverse the distance
       */
      private int convertDistance(double distance){
        return (int) (360*distance/(2*Math.PI*WHEEL_RAD));
      }
      /**
       * Method from lab 2 to convert an angle (heading) to motor rotations
       * 
       *  @param : double angle 
       *  @return : amount of degrees the motors have to turn to change this heading
       */
      private int convertAngle(double angle){
        return convertDistance(TRACK*angle*Math.PI/360);
      }

    }
  



