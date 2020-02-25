package CombineConnect.Combine;
/*
Class handling the bluetooth communication to the users ECU.
Contains two subclasses. One that is creating a thread that lives during the connection phase and one that is running while connected.
The object of this class is passed around to the different fragments.
 */

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;


public class BluetoothService implements Serializable {
    //Tag for debugging
    private String TAG = "MainActivity_debug";

    //Create the adapter, device and socket. This will be the users chosen connection.
    private BluetoothAdapter adapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;

    private final int req_code_enablebt = 1;

    //The standard UUID for serial devices
    private final String serial_uuid = "00001101-0000-1000-8000-00805F9B34FB";
    UUID connected_devic_UUID;

    //Create the two objects for the threads:
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private MainActivity activity;

    private BluetoothReceiveListener bluetoothReceiveListener;

    private boolean readyToCalibrate = false;
    private String transactionRule = "";

    private boolean approvedDisconnect = false;

    public enum Keywords{
        SETTINGS('!'),
        CALIBRATION('$'),
        PLOT('%'),
        MAINSTATE('@'),
        MANUAL(';'),
        DEBUG('<'),
        MODE('*'),
        ENDLINE('|'),
        EQUALS('='),
        BLUETOOTHSTATUS('>');

        private final char value;
        Keywords(char value){
            this.value = value;
        }
        public char getValue(){
            return value;
        }
    }

    private enum Settings {
        APPROVED('#'),
        UPDATERULE('?'),
        MAP(':');

        private final char value;
        Settings(char value){
            this.value = value;
        }
        public char getValue(){
            return value;
        }
    }


    /*
    Constructor
     */
    BluetoothService(MainActivity act){
        adapter = BluetoothAdapter.getDefaultAdapter();
        activity = act;
        bluetoothReceiveListener = null;
    }

    public void setDevice(BluetoothDevice device){
        this.device = device;
    }
    public BluetoothAdapter getAdapter(){
        return adapter;
    }

    public BluetoothDevice getDevice(){
        return device;
    }

    public BluetoothSocket getSocket(){
        return socket;
    }


    public Set<BluetoothDevice> getBondedDevices(){
        return adapter.getBondedDevices();
    }
    public boolean initiate(){
        if(adapter == null){
            //Device does not support Bluetooth.
            return false;
        }
        //Check if bluetooth is enabled on device.
        if (!adapter.isEnabled()) {
            return false;
        }
        checkBTPermissions();
        return true;
    }

    public void start_discovery(){
        
        //If it is discovering already, restart the discovery.
        if (adapter.isDiscovering()) {
            Log.d(TAG, "BTS_start_discovery: cancelling discovery");
            adapter.cancelDiscovery();
        }
        //Start the discovery.
        adapter.startDiscovery();
        Toast.makeText(activity,"Searching..",Toast.LENGTH_SHORT).show();
        Log.d(TAG, "BTS_start_discovery: discovery started");
    }
    public void connect(){
        //Check that the socket is not already connected.
        if(socket!=null && socket.isConnected()){
            Log.d(TAG,"BTS_connect: already connected!");
        }else{
            //Assign the UUID to be used and try to connect by starting the connect thread.
            connected_devic_UUID = UUID.fromString(serial_uuid);
            connectThread = new ConnectThread(device,connected_devic_UUID);
            connectThread.start();
        }
    }

    public void disconnect(){

        //transmitDisconnectRequest();
        if(connectThread!=null){
            connectThread.cancel();
        }
    }

    public void write(byte[] bytes){
        //Make sure we are connected:
        if(socket!=null && socket.isConnected() && connectedThread != null){
            connectedThread.write(bytes);
        }
        else{
            //Log.d(TAG,"BTS_disconnect: Not connected!");
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    //Toast.makeText(activity, "Not connected!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void transmitApproveTransactionRules(Keywords source){
        char command = Settings.APPROVED.getValue();
        switch(source){
            case PLOT:
                write((String.valueOf(Keywords.SETTINGS.getValue())+Keywords.PLOT.getValue()+command+Keywords.ENDLINE.getValue()).getBytes());
                break;
            case MAINSTATE:
                write((String.valueOf(Keywords.SETTINGS.getValue())+Keywords.MAINSTATE.getValue()+command+Keywords.ENDLINE.getValue()).getBytes());
                break;
            case MODE:
                write((String.valueOf(Keywords.SETTINGS.getValue())+Keywords.MODE.getValue()+command+Keywords.ENDLINE.getValue()).getBytes());
                break;
        }
    }

    public void transmitRequestNewTransactionRules(Keywords source){
        char command = Settings.UPDATERULE.getValue();
        switch (source){
            case PLOT:
                write((String.valueOf(Keywords.SETTINGS.getValue())+Keywords.PLOT.getValue()+command+Keywords.ENDLINE.getValue()).getBytes());
                break;
            case MAINSTATE:
                write((String.valueOf(Keywords.SETTINGS.getValue())+Keywords.MAINSTATE.getValue()+command+Keywords.ENDLINE.getValue()).getBytes());
                break;
            case MODE:
                write((String.valueOf(Keywords.SETTINGS.getValue())+Keywords.MODE.getValue()+command+Keywords.ENDLINE.getValue()).getBytes());
                break;
        }
    }

    public void transmitCalibrationTransactionRules(String transactionRule){
        this.transactionRule = transactionRule;
        readyToCalibrate = false;
        write((String.valueOf(Keywords.SETTINGS.getValue())+Keywords.CALIBRATION.getValue()+Settings.MAP.getValue()+transactionRule+Keywords.ENDLINE.getValue()).getBytes());
    }

    public void transmitCalibrationData(String identifier,String value){
        if(readyToCalibrate){
            write((Keywords.CALIBRATION.getValue()+identifier+Keywords.EQUALS.getValue()+value+Keywords.ENDLINE.getValue()).getBytes());
        }else{
            transmitCalibrationTransactionRules(transactionRule);
        }
    }

    public void transmitManualData(String angle,String power){
        write((Keywords.MANUAL.getValue()+angle+","+power+Keywords.ENDLINE.getValue()).getBytes());
    }

    public void transmitUserMessage(String message){
        write((Keywords.DEBUG.getValue()+message+Keywords.ENDLINE.getValue()).getBytes());
    }

    public void transmitMainState(char identifier){
        write((String.valueOf(Keywords.MAINSTATE.getValue())+identifier+Keywords.ENDLINE.getValue()).getBytes());
    }

    public void transmitMode(char identifier,int value){
        write((String.valueOf(Keywords.MODE.getValue())+identifier+value+Keywords.ENDLINE.getValue()).getBytes());
    }

    public void transmitDisconnectRequest(){
        while(!approvedDisconnect){
            write((String.valueOf(Keywords.BLUETOOTHSTATUS.getValue())+"end"+Keywords.ENDLINE.getValue()).getBytes());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void transmitConnectedStatus(){
        write((String.valueOf(Keywords.BLUETOOTHSTATUS.getValue())+"start"+Keywords.ENDLINE.getValue()).getBytes());
        while(approvedDisconnect){
            write((String.valueOf(Keywords.BLUETOOTHSTATUS.getValue())+"start"+Keywords.ENDLINE.getValue()).getBytes());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public Bundle getState(){
        Log.d(TAG,"BTS_getState: Bluetooth Service get state");
        Bundle bundle = new Bundle();
        String device_address = null;
        Boolean connected = false;
        //Save the necessary information to connect again when opened.
        if(device != null){
            device_address = device.getAddress();
        }
        if(socket != null && connectThread != null){
            connected = socket.isConnected();
            connectThread.cancel();
        }
        bundle.putString("device_address",device_address);
        bundle.putBoolean("connected",connected);

        return bundle;
    }

    public void onDestroy(){

        /*transmitDisconnectRequest();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        if(connectThread!=null){
            connectThread.unregisterReceiver();
        }
        adapter = null;
        activity = null;
        device = null;
        socket = null;
        connectThread = null;
        connectedThread = null;
        bluetoothReceiveListener = null;
    }

    public void open(MainActivity act, Bundle bundle){
        adapter = BluetoothAdapter.getDefaultAdapter();
        activity = act;
        if(bundle != null){
            if(bundle.getString("device_address") != null){
                String device_address = bundle.getString("device_address");
                device = adapter.getRemoteDevice(device_address);
            }
            Boolean connected = bundle.getBoolean("connected");
            if(connected){
                //Connect again.
                connect();
            }

        }
    }

    //Its required to check for permissions (GPS) to find devices for devices > API23 .
    private void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = activity.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += activity.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if(permissionCheck!= 0){
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},131);
            }else{
                Log.d(TAG,"BTS_checkBTPermissions: No need to check permissions. SDK version < Lollipop.");
            }
        }
    }


    public interface BluetoothReceiveListener{
        public void onPlotDataReceived(String message);
        public void onPlotTransactionRulesReceived(String message);
        public void onDebugMessageReceived(String message);
    }

    public void setBluetoothReceiveListener(BluetoothReceiveListener bluetoothReceiveListener){
        this.bluetoothReceiveListener = bluetoothReceiveListener;
        Log.d(TAG,"Bluetooth receive listener changed");
    }
    /*
        Thread that will be run while trying to connect to a device.
        A new thread is needed as the connect is a blocking method.
     */
    public class ConnectThread extends Thread implements Serializable{
        //Create the BT socket
        private final BluetoothSocket bluetoothSocket;

        public ConnectThread(BluetoothDevice btdevice, UUID uuid){
            Log.d(TAG,"BTS_ConnectThread: Connect Thread Constructor");
            device = btdevice;
            connected_devic_UUID = uuid;

            //Create a tmp socket as our bluetoothsocket is final.
            BluetoothSocket tmp_socket = null;
            Log.d(TAG,"BTS_ConnectThread: Connect Thread run");

            try{
                //Set the socket
                tmp_socket = device.createRfcommSocketToServiceRecord(connected_devic_UUID);
            }catch (IOException e){
                Log.d(TAG,"BTS_ConnectThread: Could not create RFcommSocket: "+e.getMessage());
            }
            //set the main socket to tmp
            bluetoothSocket = tmp_socket;
        }

        //Will be automatically run after initialization. No need to call the method.
        public void run(){
            //Save power.
            adapter.cancelDiscovery();
            boolean succeded = false;
            for(int i = 0; i<10; i++){
                //Try 10 times if not succeded to connect.
                try{
                    //Tell the user its trying to connect.
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, "Attempting to connect...", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //Blocking call.
                    bluetoothSocket.connect();
                    //If we reach this state, we are connected.
                    IntentFilter disconnectIntent = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                    activity.registerReceiver(mBroadcastReceiver3,disconnectIntent);
                    succeded = true;
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, "Connected!", Toast.LENGTH_SHORT).show();
                            ImageView imageView = (ImageView)activity.findViewById(R.id.id_toolbar_bluetooth_icon);
                            imageView.setImageResource(R.drawable.ic_bluetooth_blue_24dp);

                        }
                    });
                    Log.d(TAG,"BTS_ConnectThread: Connection successful :D");
                    //set the main socket to the one that just connected.
                    socket = bluetoothSocket;
                    //Start up the connected thread.
                    connectedThread = new ConnectedThread(socket);
                    connectedThread.start();
                    //transmitConnectedStatus();
                    transmitRequestNewTransactionRules(Keywords.MAINSTATE);
                    transmitRequestNewTransactionRules(Keywords.MODE);
                    break;
                    //Toast.makeText(getActivity(),"Connected!",Toast.LENGTH_SHORT).show();
                }catch (IOException e){
                    //Error while trying to connect.

                    Log.e(TAG,"BTS_ConnectThread: could not connect to UUID :" + connected_devic_UUID);
                }
            }
            if(!succeded){
                try{
                    bluetoothSocket.close();
                    Log.d(TAG,"BTS_ConnectThread: closed socket");
                }catch (IOException e1){
                    Log.e(TAG,"BTS_ConnectThread: Unable to close connection in socket: "+e1.getMessage());
                }
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activity, "Could not connect to "+ device.getName(), Toast.LENGTH_SHORT).show();
                        //uncheck switch
                        if(activity.activated_frag == MainActivity.Fragments.CONNECT){
                            ((Switch) activity.findViewById(R.id.id_bond_switch)).setChecked(false);
                        }
                    }
                });
            }

        }

        private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                //Check if the action is disconnected
                if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            activity.onBluetoothDisconnect();
                            connectThread.cancel();
                            Toast.makeText(activity, "Disconnected", Toast.LENGTH_SHORT).show();

                        }
                    });
                    Log.d(TAG,"disconnected receiver.");
                }

            }
        };

        public void cancel(){
            try{
                Log.d(TAG,"BTS_ConnectThread: cancel: Closing client socket.");
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        ImageView imageView = (ImageView)activity.findViewById(R.id.id_toolbar_bluetooth_icon);
                        imageView.setImageResource(R.drawable.ic_bluetooth_disabled);
                    }
                });
                bluetoothSocket.close();
            }catch (IOException e){
                Log.e(TAG,"cancel close of socket in ConnectThread failed: "+e.getMessage());
            }
        }

        public void unregisterReceiver(){
            try {
                activity.unregisterReceiver(mBroadcastReceiver3);
            } catch (IllegalArgumentException e) {
                //Not registered, do nothing.
            }
        }
    }

    public class ConnectedThread extends Thread implements Serializable {
        private final BluetoothSocket bluetoothSocket;
        //private final InputStream inputStream;
        private final OutputStream outputStream;
        private final BufferedReader inputStream;

        public ConnectedThread(BluetoothSocket bluetoothSocket){
            Log.d(TAG,"BTS_ConnectedThread: ConnectedThread starting");
            //set the local bluetoothsocket to the global one
            this.bluetoothSocket = bluetoothSocket;
            BufferedReader tmpIn = null;
            OutputStream tmpOut = null;


            try{
                tmpIn = new BufferedReader(new InputStreamReader(bluetoothSocket.getInputStream()));
                tmpOut = bluetoothSocket.getOutputStream();
            }catch (IOException e){
                Log.e(TAG,"BTS_ConnectedThread: couldn't fetch input or output stream: "+e.getMessage());
            }
            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            Log.d(TAG,"BTS_ConnectedThread::run");

            //byte [] buffer = new byte[2048]; //Buffer store for the stream
            //int bytes;
            while (true){
                try {
                    final String incomingMessage = inputStream.readLine();
                    if(incomingMessage.length()>0){
                        if(incomingMessage.charAt(0) == Keywords.SETTINGS.getValue()){
                            Log.d(TAG,"incoming settings: "+incomingMessage);
                            if(incomingMessage.substring(1).equals(String.valueOf(Keywords.CALIBRATION.getValue()) + Settings.APPROVED.getValue()+Keywords.ENDLINE.getValue())){
                                readyToCalibrate = true;
                            }
                            else if(incomingMessage.substring(1).equals(String.valueOf(Keywords.CALIBRATION.getValue()) + Settings.UPDATERULE.getValue()+Keywords.ENDLINE.getValue())){
                                readyToCalibrate = false;
                            }
                            else if(incomingMessage.substring(1,3).equals(String.valueOf(Keywords.PLOT.getValue())+Settings.MAP.getValue())){
                                //New transaction rules for plotter.
                                if(bluetoothReceiveListener!=null){
                                    bluetoothReceiveListener.onPlotTransactionRulesReceived(incomingMessage);
                                }
                            }
                            else if(incomingMessage.substring(1,3).equals(String.valueOf(Keywords.MAINSTATE.getValue())+Settings.MAP.getValue())){
                                //New transaction rules for main state.
                                activity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        activity.onMainStateTransactionRulesReceived(incomingMessage);
                                    }
                                });
                            }
                            else if(incomingMessage.substring(1,3).equals(String.valueOf(Keywords.MODE.getValue())+Settings.MAP.getValue())){
                                //New transaction rules for main state.
                                activity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        activity.onModeTransactionRulesReceived(incomingMessage);
                                    }
                                });
                            }
                        }else if(incomingMessage.charAt(0) == Keywords.DEBUG.getValue()){
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(bluetoothReceiveListener!= null){
                                        bluetoothReceiveListener.onDebugMessageReceived(incomingMessage);
                                    }
                                }
                            });
                        }
                        else if(incomingMessage.charAt(0) == Keywords.BLUETOOTHSTATUS.getValue()){
                            if(incomingMessage.charAt(1)=='d'){
                                approvedDisconnect = true;
                            } else if(incomingMessage.substring(1,2).equals("c")){
                                approvedDisconnect = false;
                            }
                        }
                        else{
                            Log.d(TAG,incomingMessage);
                            if(bluetoothReceiveListener!= null){
                                bluetoothReceiveListener.onPlotDataReceived(incomingMessage);
                            }
                        }
                    }

                }catch (IOException e){
                    Log.e(TAG,"BTS_ConnectedThread: couldnt read incoming message: "+e.getMessage());
                    break;
                }

            }
        }

        public void write(byte[] bytes){
            //The text that we will write
            String text = new String(bytes, Charset.defaultCharset());
            //Log.d(TAG,"BTS_ConnectedThread:Write: writing to outputstream: "+text);
            try {
                //Try to write the bytes.
                outputStream.write(bytes);
                Log.d(TAG,"BTS_ConnectedThread: successfully wrote: "+ text);
            }catch (IOException e){
                Log.e(TAG,"BTS_ConnectedThread: couldnt write message: "+e.getMessage());
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activity, "Could not write message to "+ device.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        public BluetoothSocket get_bluetoothsocket(){
            return bluetoothSocket;
        }

        public void cancel(){
            try{
                Log.d(TAG,"BTS_ConnectedThread: cancel: Closing client socket.");
                bluetoothSocket.close();
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        ImageView imageView = (ImageView)activity.findViewById(R.id.id_toolbar_bluetooth_icon);
                        imageView.setImageResource(R.drawable.ic_bluetooth_disabled);
                    }
                });
                //bondSwitch.setChecked(false);
            }catch (IOException e){
                Log.e(TAG,"BTS_ConnectedThread: cancel close of socket in ConnectedThread failed: "+e.getMessage());
            }
        }

    }

}



