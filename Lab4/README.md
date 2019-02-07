## Lab4 - Localization

### Design objectives

Design a system to localize the robot using ultrasonic and light sensors, where the robot should move to a known starting position

Devaluate the design and determine how well the system localizes the robot

### Design requirements

The system must localize the robot to the origin of the tiles grid system.

Localization must use the ultrasonic and light sensors.

The robot must localize approximately to the 0 degree direction using the ultrasonic sensor.

The robot must provide input to select Rising Edge or Falling Edge. 

Two versions of the localization routine must be available: 

1. Rising Edge

2. Falling Edge

The robot must localize to the (0,0) grid and 0° using the light sensor.

The robot must wait for input once completing ultrasonic localization and orienting to 0°.
