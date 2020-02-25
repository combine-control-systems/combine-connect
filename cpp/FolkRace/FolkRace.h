#define FILTERCONSTANT 0.6
#define blueTooth Serial2
#define wireSerial Serial

#define LEFTSENSOR A13
#define RIGHTSENSOR A14
#define FORWARDSENSOR A15

#define MOTORFORWARD 2
#define MOTORREVERSE 3

#define SERVOPIN 6

#define Ts 0.001

enum MainState{
    RUN = 'r',
    STOP = 's',
    FORWARD = 'f',
    BACKWARD = 'b'
};

float Kp,Ki,Kd;
float vel;
int currentMode = 1;
char currentMainState = 's';

unsigned long lastSampleTime;
unsigned long TsMicroseconds;

float rightSensorReading;
float leftSensorReading;
float rightDistance;
float leftDistance;
float debug;
float steeringAngleDeg;
float deltaPos;
