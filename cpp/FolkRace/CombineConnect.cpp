#include "CombineConnect.h"

CombineConnect::CombineConnect(Stream* bluetoothPort,Stream* wirePort){
    this->bluetoothPort = bluetoothPort;
    this->wirePort = wirePort;
    wireEnabled = true;
	addMainState(keywords::MANUAL,"Manual",true);
}

CombineConnect::CombineConnect(Stream* bluetoothPort){
    this->bluetoothPort = bluetoothPort;
    wireEnabled = false;
  addMainState(keywords::MANUAL,"Manual",true);
}

void CombineConnect::addFloatPlotData(float* variable,String varName){
    plotTransactionRules = plotTransactionRules + "," + varName + "," + (String)(FLOAT);
    typeVector.push_back(FLOAT);
    TransmitTypes v;
    v.f = variable;
    transmitVector.push_back(v);
    nbrSignals++;
    transmitPlotTransactionRules();
}


void CombineConnect::addIntPlotData(int* variable, String varName){
    plotTransactionRules = plotTransactionRules + "," + varName + "," + (String)(INT);
    typeVector.push_back(INT);
    TransmitTypes v;
    v.i = variable;
    transmitVector.push_back(v);
    nbrSignals++;
    transmitPlotTransactionRules();
}

void CombineConnect::addMainState(char identifier,String stateName,bool isEnabled /*=false*/){
	mainStateTransactionRules = mainStateTransactionRules + "," + stateName + "," + String(identifier);
	mainStateIdentifierVector.push_back(identifier);
	mainStateNameVector.push_back(stateName);
	//since only one can be enabled at once, set current main state to the identifier
	currentMainState = isEnabled ? identifier : currentMainState;
	transmitMainStateTransactionRules();
}

void CombineConnect::addMode(char identifier, String modeName, bool isEnabled /*=false*/){
	modeTransactionRules = modeTransactionRules + "," + modeName + "," + String(identifier);
	modeNameVector.push_back(modeName);
	modeIdentifierVector.push_back(identifier);
	modeValueVector.push_back(isEnabled);
	transmitModeTransactionRules();
}

void CombineConnect::transmitPlotTransactionRules(){
    readyToTransmitPlotData = false;
    transmit(plotTransactionRules);
}

void CombineConnect::transmitMainStateTransactionRules(){
	readyToReceiveMainState = false;
	buildMainStateInitiationString();
	transmit(mainStateTransactionRules + mainStateInitiationString);
}

void CombineConnect::transmitModeTransactionRules(){
	readyToReceiveMode = false;
	buildModeInitiationString();
	transmit(modeTransactionRules + modeInitiationString);
}

void CombineConnect::transmit(String out){
	// if(isConnected){
	// 	bluetoothPort->println(out);
 	// 	if(wireEnabled){
 	//   		wirePort ->println(out);  
 	// 	}
	// }

	
	bluetoothPort->println(out);
 	if(wireEnabled){
 		wirePort ->println(out);  
 	}
	
	

}

bool CombineConnect::transmitPlotData(){
    //If app is ready to receive messages: 
    if(millis()-lastTransmittedPlotDataTime > 100){ 
      if(readyToTransmitPlotData){
          String data = "";
          for(int i = 0; i < nbrSignals; i++){
              if(typeVector[i] == FLOAT){
                  data += String(*transmitVector[i].f) + ",";
              }else{
                  data += String(*transmitVector[i].i) + ",";
              }      
          }
  
          //If length != 0, remove last ','
          int len = data.length();
          if(len > 0){
              data[len-1] = '\0';    
          }
  
          //Print data. 
          transmit(data);
          lastTransmittedPlotDataTime = millis();
  
  		return true;
      }
	  //If we're not ready to transmit, send another plotTransactionRules and wait for confirmation
	  transmitPlotTransactionRules();
    lastTransmittedPlotDataTime = millis();
   }
	return false;
    
}
String CombineConnect::formatIntData(int data){
    return String(data);
}
String CombineConnect::formatFloatData(float data){  
    return String(data);
}

/*
	returns true if message was received.
*/
bool CombineConnect::update(){
	bool returnVal = false;
	if(pollSerial(*bluetoothPort)){
		returnVal = true;
	}
 if(wireEnabled){
  if(pollSerial(*wirePort)){
    returnVal = true;
  } 
 }
	return returnVal;
}

bool CombineConnect::pollSerial(Stream& busToPoll){
	bool message_read = false;
	while(busToPoll.available()){
		char polled_char = busToPoll.read();
		if(new_message){
			incomingMessage = "";
			keyword = polled_char;
			new_message = false;
			bytes_read = 0;
		}
		bytes_read++;

		switch (keyword)
		{
		case keywords::MAINSTATE:
			//Handle the mode command 
			if(polled_char != keywords::MAINSTATE){
				for(int i = 0; i < mainStateIdentifierVector.size(); i++){
					if(polled_char == mainStateIdentifierVector[i]){
						currentMainState = polled_char;
						clearSerial(busToPoll);
						new_message = true;
						message_read = true;
						break;
					}
				}
				//default (if the receieved state not found in mainStateIdentifierVector, do nothing.)
				break;
			} 
      
			break;
			
		case keywords::MODE:
			//Handle the mode command 
			if(polled_char != keywords::MODE){
				if(modeIdx == -1){
					for(int i = 0; i < modeIdentifierVector.size(); i++){
						if(polled_char == modeIdentifierVector[i]){
							modeIdx = i;
							break;
						}
					}
				}else{
					modeValueVector[modeIdx] = polled_char!='0' ? 1 : 0;
					modeIdx = -1;
					clearSerial(busToPoll);
					new_message = true;
					message_read = true;
				}
				//default (if the receieved state not found in mainStateIdentifierVector, do nothing.)
				break;
			}   
			break;	
		case keywords::SETTINGS:
			switch(polled_char){
				case keywords::ENDLINE:
					if(incomingMessage.equals(String(char(settings::APPROVED)))){
						if(settingsKeyword == keywords::PLOT){
							readyToTransmitPlotData = true;
						}
						else if(settingsKeyword == keywords::MAINSTATE){
							readyToReceiveMainState = true;
						}
						else if(settingsKeyword == keywords::MODE){
							readyToReceiveMode = true;
						}
					}
					else if (incomingMessage.equals(String(char(settings::UPDATERULE))))
					{
						if(settingsKeyword == keywords::PLOT){
							transmitPlotTransactionRules();
						}
						else if(settingsKeyword == keywords::MAINSTATE){
							transmitMainStateTransactionRules();
						}
						else if(settingsKeyword == keywords::MODE){
							transmitModeTransactionRules();
						}
					}
					else{
						if(settingsKeyword == keywords::CALIBRATION){
							setCalibrationVariables(incomingMessage);
							transmitApproveTransactionRules(keywords::CALIBRATION);
						}
					}
					//clearSerial(busToPoll);
					new_message = true;
					message_read = true;
					break;
				case keywords::SETTINGS:
					//Do nothing, we want to start at next variable. 
					break;
				case keywords::CALIBRATION:
					//Settings are valid for calibration. 
					settingsKeyword = keywords::CALIBRATION;
					break;
				case settings::MAP:
					//This is added for consistency, for easier further development.. 
					//Serial.print("Map received for ");
					//Serial.println(settingsKeyword);
					break;
				case keywords::PLOT:
					//Settings are valid for calibration. 
					settingsKeyword = keywords::PLOT;
					break;
				case keywords::MAINSTATE:
					//Settings are valid for calibration. 
					settingsKeyword = keywords::MAINSTATE;
					break;
				case keywords::MODE:
					//Settings are valid for calibration. 
					settingsKeyword = keywords::MODE;
					break;
				default: 
					//add to incomingMessage given its not a \n. 
					if(polled_char != '\n'){
						incomingMessage += polled_char;
					}			
					break;
			}
			break;
			
		case keywords::CALIBRATION:
			switch(polled_char){
				case keywords::CALIBRATION:
					//If we received message without having the transaction rules:
					if(calibrationValues.size()==0){
						transmitRequestNewTransactionRules(keywords::CALIBRATION);
						clearSerial(busToPoll);
						new_message = true;
						message_read = true;
					}
					break;
				case keywords::ENDLINE:
					setCalibrationValue(incomingMessage,calibrationIdx);
					clearSerial(busToPoll);
					new_message = true;
					message_read = true;
					break;
				case keywords::EQUALS:
					calibrationIdx = incomingMessage.toInt();
					incomingMessage = "";
					break;
				default:
					//add to incomingMessage given its not a \n. 
					if(polled_char != '\n'){
						incomingMessage += polled_char;
					}			
					break;
			}
			break;
		case keywords::MANUAL:
			switch (polled_char){
				case keywords::MANUAL:
					incomingMessage = "";
					break;
				case ',':
					angle = incomingMessage.toFloat();
					incomingMessage = "";
          			break;
				case keywords::ENDLINE:	
					power = incomingMessage.toFloat();
					incomingMessage = "";
					clearSerial(busToPoll);
					new_message = true;
					message_read = true;	
         			break;
				default:
					if(polled_char != '\n'){
						incomingMessage += polled_char;
					}			
					break;
			}
      		break;
		case keywords::DEBUG:
			switch (polled_char){
				case keywords::DEBUG:
					incomingMessage = "";
					break;
				case keywords::ENDLINE:	
					debugMessage = incomingMessage;
          			Serial.println(debugMessage);
					newDebugMessage = true;
					incomingMessage = "";
					clearSerial(busToPoll);
					new_message = true;
					message_read = true;	
         			break;
				default:
					if(polled_char != '\n'){
						incomingMessage += polled_char;
					}			
					break;
			}
      		break;	  	
		
		default:
			clearSerial(busToPoll);
			new_message = true;
			break;
		}
	}
	return message_read;	
}

void CombineConnect::setCalibrationVariables(String message){
	int len = calibrationValues.size();
	String tmpNameArray[len];
	float tmpValArray[len];
	for(int i = 0; i < len; i ++){
		tmpNameArray[i] = calibrationVariableNameVector[i];
		tmpValArray[i] = calibrationValues[i];
	}
	calibrationVariableNameVector.clear();
	calibrationValues.clear();
	char* token = strtok((char*)message.c_str(),",");
	while(token != NULL){
		calibrationVariableNameVector.push_back(String(token));
		bool existing = false;
		for(int i = 0; i < len; i++){
    		if(tmpNameArray[i].equals(token)){
      			calibrationValues.push_back(tmpValArray[i]);
				existing = true;
				break;
    		}
  		}
		if(!existing){
			calibrationValues.push_back(0);
		}
		token = strtok(NULL,",");
	}
  
}

void CombineConnect::transmitApproveTransactionRules(keywords key){
	String messageToTransmit = String(char(keywords::SETTINGS)) + String(char(key)) + char(settings::APPROVED) + char(keywords::ENDLINE);
	transmit(messageToTransmit);
}

void CombineConnect::transmitRequestNewTransactionRules(keywords key){
	String messageToTransmit = String(char(keywords::SETTINGS)) + String(char(key)) +char(settings::UPDATERULE) + char(keywords::ENDLINE);
	transmit(messageToTransmit);
}

void CombineConnect::setCalibrationValue(String val,int idx){
	calibrationValues[idx] = val.toFloat();
}

void CombineConnect::clearSerial(Stream& busToClear){
	char polled_char = 0;
	while(busToClear.available() && polled_char != keywords::ENDLINE){
		polled_char = busToClear.read();
	}
}

float CombineConnect::getCalibrationData(String varName){
  for(int i = 0; i < calibrationVariableNameVector.size();i++){
    if(calibrationVariableNameVector[i] == varName){
      return calibrationValues[i];
    }
  }
  return 0;
}

char CombineConnect::getCurrentMode(char identifier){
	//TODO::Return only mode of identifier, remove current mode.
	for(int i = 0; i < modeValueVector.size(); i++){
		if(modeIdentifierVector[i] == identifier){
			return modeValueVector[i];
		}
	}
	return -1;
}

char CombineConnect::getCurrentMainState(){
	if(readyToReceiveMainState){
		return currentMainState;
	}else{
		transmitMainStateTransactionRules();
		return -1;
	}
	
}

void CombineConnect::buildModeInitiationString(){
	modeInitiationString = ",*";
	for(int i = 0; i < modeValueVector.size(); i++){
		modeInitiationString = modeInitiationString + "," + String(modeValueVector[i]);
	}
}

void CombineConnect::buildMainStateInitiationString(){
	mainStateInitiationString = ",@";
	for(int i = 0; i < mainStateNameVector.size(); i++){
		String isEnabled = mainStateIdentifierVector[i]==currentMainState ? "1":"0";
		mainStateInitiationString = mainStateInitiationString + "," + isEnabled;
	}
}

float CombineConnect::getAngle(){
	return angle;
}

float CombineConnect::getPower(){
	return power;
}

String CombineConnect::getDebugMessage(){
	if(newDebugMessage){
		newDebugMessage = false;
		return debugMessage;
	}
	return "";
}

void CombineConnect::transmitDebugMessage(String message){
  if(millis()-lastTransmittedDebugMessageTime > 100){ 
    String messageToTransmit = '<'+message+'|';
    transmit(messageToTransmit);
    lastTransmittedDebugMessageTime = millis();
  }
	
}
