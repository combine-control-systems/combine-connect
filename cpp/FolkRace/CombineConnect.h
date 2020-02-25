#ifndef _COMBINECONNECT_H
#define _COMBINECONNECT_H

#include <Arduino.h>
#include <stdlib.h>
#include <stdio.h>
#include <vector>

//#include "Supervisor.h"

using namespace std;

class CombineConnect
{	
public:
/*
* Variable declarations
*/

/*
* Function declarations
*/
CombineConnect(Stream* bluetoothPort, Stream* wirePort);
CombineConnect(Stream* bluetoothPort);
void addFloatPlotData(float* variable,String varName);
void addIntPlotData(int* variable,String varName);
void transmitDebugMessage(String message);
bool transmitPlotData();
void addMainState(char identifier,String stateName,bool isEnabled = false);
void addMode(char identifier,String modeName,bool isEnabled = false);
bool update();
float getCalibrationData(String varName);
float getAngle();
float getPower();

//Supervisor::mainMode getCurrentMainModeRequest();
char getCurrentMode(char identifier);
char getCurrentMainState();

String getDebugMessage();

	
private:
/*
* Variable declarations
*/
Stream* bluetoothPort;
Stream* wirePort;
enum Type {FLOAT,INT};
String plotTransactionRules = "!%:";
String mainStateTransactionRules = "!@:";
String modeTransactionRules = "!*:";
bool readyToTransmitPlotData = false;
bool readyToReceiveMainState = false;
bool readyToReceiveMode = false;
bool newDebugMessage = false;

union TransmitTypes{
    int* i;
    float* f;
};

vector<TransmitTypes> transmitVector;
vector<int> typeVector;
vector<String> calibrationVariableNameVector;
vector<float> calibrationValues;
vector<String> modeNameVector;
vector<int> modeIdentifierVector;
vector<int> modeValueVector;
vector<String> mainStateNameVector;
vector<int> mainStateIdentifierVector;
char currentMainState;

float power = 0;
float angle = 0;

int nbrSignals = 0;

String settingsMessage = "";
String incomingMessage = "";
String modeInitiationString;
String mainStateInitiationString;
String debugMessage = "";
int calibrationIdx = 0;

enum keywords
{
	SETTINGS = '!',
	CALIBRATION = '$',
	PLOT = '%',
	MAINSTATE = '@',
	MODE = '*',
	ENDLINE = '|',
	EQUALS = '=',
	MANUAL = ';',
	DEBUG = '<',
	BLUETOOTHSTATUS = '>'
};

enum settings
{
	APPROVED = '#',
	UPDATERULE = '?',
	MAP = ':'
};
//Supervisor::mainMode currentMainModeRequest = Supervisor::mainMode::IDLE;
/*
* Function declarations
*/
void transmitPlotTransactionRules();
void transmitMainStateTransactionRules();
void transmitModeTransactionRules();
void transmit(String out);
String formatIntData(int data);
String formatFloatData(float data);
bool pollSerial(Stream& busToPoll);
bool pollSerial2(Stream& busToPoll);
void clearSerial(Stream& busToPoll);
void setCalibrationVariables(String message);
void setCalibrationValue(String val,int idx);
void transmitApproveTransactionRules(keywords key);
void transmitRequestNewTransactionRules(keywords key);
void buildModeInitiationString();
void buildMainStateInitiationString();

bool new_message = true;
int bytes_read = 0;
char keyword;
char settingsKeyword;
int modeIdx = -1;

bool wireEnabled = false;
//bool isConnected = false;

unsigned long lastTransmittedPlotDataTime = 0;
unsigned long lastTransmittedDebugMessageTime = 0;
};



#endif
