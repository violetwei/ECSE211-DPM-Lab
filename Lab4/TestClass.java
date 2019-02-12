package ca.mcgill.ecse211.lab4;


import static ca.mcgill.ecse211.lab4.Lab4.*;
import lejos.hardware.Sound;
  
  public class USLocal extends Thread {
     

      
      private LocalizationType locType;
      
      
      private boolean noiseZone = false;
      private static final int MAX_DISTANCE = 50;
      private static final int WALL_DISTANCE_R = 50;
      private static final float WALL_DISTANCE = 30;
      private static final float MARGIN_DISTANCE =(float) 0.5; 
      private static final float MOTOR_SPEED = 75;
      
      private static int FILTER_OUT = 10;
      private int filterControl;
      private float lastDistance;
      
      public USLocal(LocalizationType locType) {
          
          this.locType = locType;
          
      }
      public void run() {
      
          
          double angleA = 0;
          double angleB = 0;
          double angleA2 = 0;
          double angleB2 = 0;

          
          if(locType == LocalizationType.FALLING_EDGE) {
              
              // rotate the robot until it sees no wall
            leftMotor.setSpeed(MOTOR_SPEED);
            rightMotor.setSpeed(MOTOR_SPEED);
            
             
              
              while (getFilteredData()<=WALL_DISTANCE) {
                  
                    leftMotor.forward();
                    rightMotor.backward();
       
              }
              // keep rotating until the robot sees a wall, then latch the angle
              
              
              while (true) {
                leftMotor.forward();
                rightMotor.backward();
                if (!noiseZone && getFilteredData() <= WALL_DISTANCE + MARGIN_DISTANCE) {
                  angleA = odometer.getTheta();
                  noiseZone = true;
                  Sound.beep();
              } else if ( getFilteredData() >= WALL_DISTANCE - MARGIN_DISTANCE && noiseZone){
                  angleA2 = odometer.getTheta();
                  Sound.beep();
                  
                  
              } else if(noiseZone) {
                noiseZone = false;
                break;
              }
                  
              }
              //Sound.beep();
              if(angleA2!=0) {
                angleA = (angleA+angleA2)/2.0;
              } else if(angleA == 0) {
                angleA = odometer.getTheta();
              }
              
              // switch direction and wait until it sees no wall
              
              
              
              while (getFilteredData()<=WALL_DISTANCE) {
                
                leftMotor.backward();
                rightMotor.forward();
   
          }
              
              // keep rotating until the robot sees a wall, then latch the angle
              

              
              while (true) {
                leftMotor.backward();
                rightMotor.forward();
                if (!noiseZone && getFilteredData() <= WALL_DISTANCE + MARGIN_DISTANCE) {
                  angleB = odometer.getTheta();
                  noiseZone = true;
                  Sound.beep();
              } else if ( getFilteredData() >= WALL_DISTANCE - MARGIN_DISTANCE && noiseZone){
                  angleB2 = odometer.getTheta();
                  Sound.beep();
              }else if(noiseZone) {
                noiseZone = false;
                break;
              }
                  
              }
              
              //Sound.beep();
              if(angleB2!=0) {
                angleB = (angleB+angleB2)/2.0;
              } else if(angleB == 0) {
                angleB = odometer.getTheta();
              }
              
              rightMotor.stop(true);
              leftMotor.stop(true);
              
              //Sound.beep();
              //get the angle from the odometer
              //angleB = odo.getTheta();
              //if our angle A is larger than B, it means we passed the 0 point, and that angle A is "negative".
              if(angleB > angleA){
                  angleB -= 360;
              }
              
              double averageAngle = (angleA + angleB)/2.0;
              double ZeroPoint =  averageAngle - 45 - odometer.getTheta();

              //System.out.println("A" + angleA);
              //System.out.println("B:" + angleB);
              //System.out.println("Average" + averageAngle);
              //System.out.println("To Turn" + (FortyFiveDegPastNorth + 45));
              //rotate to the diagonal + 45 (to the horizontal x axis)
              //leftMotor.rotate(convertAngle( ZeroPoint), true);
              //rightMotor.rotate(-convertAngle(ZeroPoint), false);
              turnTo(ZeroPoint);
              System.out.println("\n\n\n"+(int)angleA+" "+(int)angleB+" "+(int)averageAngle);
              System.out.println((int)angleA2+" "+(int)angleB2);
              // update the odometer position to 0 0 0 (that's how we are facing. Position (x and y) will
              //be wrong but that will be fixed by the LightLocalizer
              //odo.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
      
              
              // angleA is clockwise from angleB, so assume the average of the
              // angles to the right of angleB is 45 degrees past 'north'
              
              /*double endAngle = getEndAngle(angleA,angleB);
              if (endAngle<0) {
                  turnTo(endAngle+360);
              } else if (endAngle>360){
                  turnTo(endAngle-360);
              } else {
                  turnTo(endAngle);
              }
              
              // update the odometer position (example to follow:)
              
              odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});*/
          } else {
            //lastDistance = 255;
            // rotate the robot until it sees no wall
            leftMotor.setSpeed(MOTOR_SPEED);
            rightMotor.setSpeed(MOTOR_SPEED);
            
             
              
              while (getData()>=WALL_DISTANCE_R) {
                  
                    leftMotor.forward();
                    rightMotor.backward();
       
              }
              // keep rotating until the robot stops seing a wall, then latch the angle
              
              
              while (true) {
                leftMotor.forward();
                rightMotor.backward();
                if (!noiseZone && getFilteredData() >= WALL_DISTANCE - MARGIN_DISTANCE) {
                  angleA = odometer.getTheta();
                  noiseZone = true;
                  
              } else if ( getFilteredData() <= WALL_DISTANCE + MARGIN_DISTANCE && noiseZone){
                  angleA2 = odometer.getTheta();
                  
              } else if(noiseZone) {
                noiseZone = false;
                break;
              }
              }
              
              Sound.beep();
              if(angleA2!=0) {
                angleA = (angleA+angleA2)/2.0;
              } else if(angleA == 0) {
                angleA = odometer.getTheta();
              }
              
              // switch direction and wait until it sees no wall
              
              
              
              while (getData()>=WALL_DISTANCE_R) {
                
                leftMotor.backward();
                rightMotor.forward();
   
          }
              
              // keep rotating until the robot sees a wall, then latch the angle
              

              
              while (true) {
                leftMotor.backward();
                rightMotor.forward();
                if (!noiseZone && getFilteredData() >= WALL_DISTANCE - MARGIN_DISTANCE) {
                  angleB = odometer.getTheta();
                  noiseZone = true;
                  
              } else if ( getFilteredData() <= WALL_DISTANCE + MARGIN_DISTANCE && noiseZone){
                  angleB2 = odometer.getTheta();
                  
              }else if(noiseZone) {
                noiseZone = false;
                break;
              }
                  
              }
              
              Sound.beep();
              if(angleB2!=0) {
                angleB = (angleB+angleB2)/2.0;
              } else if(angleB == 0) {
                angleB = odometer.getTheta();
              }
              
              rightMotor.stop(true);
              leftMotor.stop(true);
              
              //Sound.beep();
              //get the angle from the odometer
              //angleB = odo.getTheta();
              //if our angle A is larger than B, it means we passed the 0 point, and that angle A is "negative".
              if(angleA > angleB){
                  angleA -= 360;
              }
              
              double averageAngle = (angleA + angleB)/2.0;
              double ZeroPoint =  averageAngle - 45 - odometer.getTheta();

              //System.out.println("A" + angleA);
              //System.out.println("B:" + angleB);
              //System.out.println("Average" + averageAngle);
              //System.out.println("To Turn" + (FortyFiveDegPastNorth + 45));
              //rotate to the diagonal + 45 (to the horizontal x axis)
              //leftMotor.rotate(convertAngle( ZeroPoint), true);
              //rightMotor.rotate(-convertAngle(ZeroPoint), false);
              turnTo(ZeroPoint);
              System.out.println("\n\n\n"+(int)angleA+" "+(int)angleB+" "+(int)averageAngle);
              System.out.println((int)angleA2+" "+(int)angleB2);
              // update the odometer position to 0 0 0 (that's how we are facing. Position (x and y) will
              //be wrong but that will be fixed by the LightLocalizer
              //odo.setPosition(new double [] {0.0, 0.0, 0}, new boolean [] {true, true, true});
      
            }
              /*
               * The robot should turn until it sees the wall, then look for the
               * "rising edges:" the points where it no longer sees the wall.
               * This is very similar to the FALLING_EDGE routine, but the robot
               * will face toward the wall for most of it.
               */
              
            /*leftMotor.setSpeed(MOTOR_SPEED);
            rightMotor.setSpeed(MOTOR_SPEED);
            leftMotor.forward();
            rightMotor.backward();
              
              while (true) {
                  if (getFilteredData()<=WALL_DISTANCE) {
                      break;
                  }
              }
              while (true) {
                  if (!noiseZone && getFilteredData()>EDGE_DISTANCE_R-MARGIN_DISTANCE) {
                      angleB = odo.getTheta();
                      noiseZone = true;
                      Sound.beep();
                  } else if ( getFilteredData()>EDGE_DISTANCE_R+MARGIN_DISTANCE){
                      angleB = (angleB + odo.getTheta())/2;
                      noiseZone = false;
                      Sound.beep();
                      break;
                  }
              }
              
              leftMotor.setSpeed(MOTOR_SPEED);
              rightMotor.setSpeed(MOTOR_SPEED);
              leftMotor.backward();
              rightMotor.forward();
              
              while (true) {
                  if (getFilteredData()<=WALL_DISTANCE) {
                      break;
                  }
              }
              while (true) {
                  if (!noiseZone && getFilteredData()>EDGE_DISTANCE_R-MARGIN_DISTANCE) {
                      angleA = odo.getTheta();
                      noiseZone = true;
                      Sound.beep();
                  } else if ( getFilteredData()>EDGE_DISTANCE_R+MARGIN_DISTANCE){
                      angleA = (angleA + odo.getTheta())/2;
                      noiseZone = false;
                      Sound.beep();
                      break;
                  }
              }
              
              double endAngle = getEndAngle(angleA,angleB);
              if (endAngle<0) {
                  turnTo(endAngle+2*Math.PI);
              } else if (endAngle>2*Math.PI){
                  turnTo(endAngle-2*Math.PI);
              } else {
                  turnTo(endAngle);
              }
              
              odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});*/

          
      }
      
      public float getFilteredData2() {
          usSensor.fetchSample(usData, 0);
          float distance = usData[0]*100;
          
          if (distance > MAX_DISTANCE) distance = MAX_DISTANCE;
                  
          return distance;
      }
      
      private int filterbig() {
        int firstdistance;
        usDistance.fetchSample(usData,0);           // acquire data from the ultrasonic sensor
        firstdistance=(int)(usData[0]*100.0);  // extract from buffer, cast to int
        
        for (int i = 0; i<5; i++) {
         
          usDistance.fetchSample(usData,0);           // acquire data from the ultrasonic sensor
          int distance=(int)(usData[0]*100.0);  // extract from buffer, cast to int
          
          if(distance < 30) {
            return 0;
          }
        }
        double angle = odometer.getTheta();
        return firstdistance;
      }

      private int filtersmall() {
        int firstdistance;
        usDistance.fetchSample(usData,0);           // acquire data from the ultrasonic sensor
        firstdistance=(int)(usData[0]*100.0);  // extract from buffer, cast to int
        
        for (int i = 0; i<5; i++) {
         
          usDistance.fetchSample(usData,0);           // acquire data from the ultrasonic sensor
          int distance=(int)(usData[0]*100.0);  // extract from buffer, cast to int
          
          if(distance > 50) {
            return 255;
          }
        }
        double angle = odometer.getTheta();
        return firstdistance;
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

      
     /* public void Simplefilter(int distance){
        int FILTER_OUT = 10;
        int filterControl = 0;
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
     }*/

      
      private double getEndAngle(double a, double b) {
          if (a > b) {
              return ((a+b)/2 - 225);
          }
          return ((a+b)/2 - 45);
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
  

