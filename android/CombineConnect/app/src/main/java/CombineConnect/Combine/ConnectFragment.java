package CombineConnect.Combine;
/*
Fragment class for the Connect fragment.
For documentation of fragments, see: https://developer.android.com/guide/components/fragments
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.snackbar.Snackbar;

//import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

public class ConnectFragment extends Fragment{
    private String TAG = "MainActivity_debug";

    // The bond Switch
    private Switch bondSwitch;

    //Constant to detect bluetooth enable state changes
    private final int req_code_enablebt = 1;

    //bool to check if the BT was actiavated after the user checked the bondswitch
    private boolean activate_bt_req_sent = false;

    //The refresh discover button
    private ImageButton refreshDiscoverButton;


    Set<BluetoothDevice> pairedDevices;

    //Create the arraylist of available bluetooth devices. (empty to begin with).
    private ArrayList<BluetoothDevice> discovered_devices = new ArrayList<BluetoothDevice>();
    private ArrayList<String> discovered_devices_string = new ArrayList<String>();

    //Create an adapter between the listview and arraylist.
    //private ArrayAdapter<BluetoothDevice> adapter;
    private ArrayAdapter<String> discoveredAdapter;

    //Create the listview.
    private ListView listViewDiscoveredDevices;

    //Create the arraylist of paired bluetooth devices. (empty to begin with).
    private ArrayList<BluetoothDevice> paired_devices = new ArrayList<BluetoothDevice>();
    private ArrayList<String> paired_devices_string = new ArrayList<String>();

    //Create an adapter between the listview and arraylist.
    private ArrayAdapter<String> pairedAdapter;

    //Create the listview.
    private ListView listViewPairedDevices;
    
    private BluetoothService bluetoothService;

    private static String selected_device_text = "Select a Device";
    private static Boolean switch_on = false;
    private Boolean switch_set_by_user = true;

    // The current fragment view
    private View root;
    
    public static ConnectFragment newInstance(BluetoothService bluetoothService,Bundle fragmentState){
        ConnectFragment connectFragment = new ConnectFragment();
        Bundle args = new Bundle();
        args.putSerializable("bluetooth_service",bluetoothService);
        args.putBundle("fragmentState",fragmentState);
        connectFragment.setArguments(args);
        if(fragmentState != null){
            selected_device_text = fragmentState.getString("selected_device_text");
            switch_on = fragmentState.getBoolean("switch_on");
        }
        return connectFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            Bundle bundle = getArguments();
            bluetoothService = (BluetoothService) bundle.getSerializable("bluetooth_service");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Create the current view, call it root.
        root = inflater.inflate(R.layout.connect_layout,container,false);
        Log.d(TAG,"CF_onCreateView: Started");
        //Create an intentfilter for when the bond/pair state changes
        IntentFilter bond_filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        getActivity().registerReceiver(mBroadcastReceiver2,bond_filter);
        //Create the Bond switch listener
        bondSwitch = (Switch) root.findViewById(R.id.id_bond_switch);
        bondSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bondSwitchListener(root,isChecked);
            }
        });

        //Create the refresh discover button
        refreshDiscoverButton = root.findViewById(R.id.id_refresh_discover_button);
        refreshDiscoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverDevices();
            }
        });

        //Create the refresh paired button
        refreshDiscoverButton = root.findViewById(R.id.id_refresh_paired_button);
        refreshDiscoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePairedDevices();
            }
        });

        //Initialize the listview and set the onclicklistener for when the user picks an object
        listViewDiscoveredDevices = (ListView) root.findViewById(R.id.id_bluetooth_list_available);
        listViewDiscoveredDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleDiscoverClick(parent,view,position,id);
            }
        });

        //Initialize the paired listview and set the onclicklistener for when the user picks an object
        listViewPairedDevices = (ListView) root.findViewById(R.id.id_bluetooth_list_paired);
        listViewPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handlePairedClick(parent,view,position,id);
            }
        });

        switch_set_by_user = false;
        bondSwitch.setChecked(switch_on);
        switch_set_by_user = true;
        TextView selected_device = (TextView) root.findViewById(R.id.id_selected_device);
        selected_device.setText(selected_device_text);

        updatePairedDevices();

        return root;
    }

    public void setSwitchState(boolean on){
        switch_on = on;
    }

    private void bondSwitchListener(View root, boolean on){
        if(switch_set_by_user) {
            //Check whether the switch is activated or deactivated.
            setSwitchState(on);
            if (on) {
                Log.d(TAG, "CF_bond_switch_listener: switch on");
                //First check that Bluetooth is available and active for the device.
                if (!initiateBt()) {
                    //Bluetooth isn't available.
                    Snackbar.make(root, "Please activate Bluetooth", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    bondSwitch.setChecked(false);
                    return;
                }
                //Bluetooth activated, lets see if the user have selected a device to bond to.
                TextView selected_device = (TextView) root.findViewById(R.id.id_selected_device);
                if (selected_device.getText().toString().matches(getString(R.string.select_device))) {
                    //The user havn't selected a device to bond to.
                    Log.d(TAG, "CF_bond_switch_listener: please select device");
                    Toast.makeText(getActivity(), "Please select a device.", Toast.LENGTH_SHORT).show();
                    bondSwitch.setChecked(false);
                    return;
                }
                bluetoothService.connect();

            } else {
                Log.d(TAG, "CF_bond_switch_listener: switch off");
                bluetoothService.disconnect();
            }
        }
    }

    private boolean initiateBt(){
        if(!bluetoothService.initiate()){
            //Bluetooth not enabled. Send a request to activate it.
            activate_bt_req_sent = true;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,req_code_enablebt);
            IntentFilter enableBtIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getActivity().registerReceiver(mBroadcastReceiver,enableBtIntentFilter);
            return false;
        }

        return true;
    }

    // Create a BroadcastReceiver for Changed states.
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //Check if the action is a change in units bluetooth state.
            if (action.equals(bluetoothService.getAdapter().ACTION_STATE_CHANGED)) {
                //Check if the state changed to activated.
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){
                    Log.d(TAG,"CF_mBroadCastReceiver: BT enabled");
                    if(activate_bt_req_sent){
                        //Assume that this was triggered by the request from the program to the user.
                        //Activate the bondSwitch so the user don't need to check it again.
                        activate_bt_req_sent = false;
                        bondSwitch.setChecked(true);
                        //bondSwitchListener(root,true);
                    }
                }
                else{
                    Log.d(TAG,"CF_mBroadCastReceiver: BT deactivated");
                    activate_bt_req_sent = false;
                    bondSwitch.setChecked(false);
                }
            }
        }
    };

    //Discover unpaired devices and view in listview.
    private void discoverDevices(){
        //Make sure Bluetooth is initiated.
        if(initiateBt()) {
            discovered_devices.clear();
            discovered_devices_string.clear();
            bluetoothService.start_discovery();

            //Create an intentfilter to show new discovered bluetoothdevices
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getActivity().registerReceiver(mBroadcastReceiver1, discoverDevicesIntent);
        }
    }

    // Create a BroadcastReceiver for new bluetoothdevices
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //Check if the action is a new device found.
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                //Create the new device.
                BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String dev_name = newDevice.getName();
                if(dev_name != null){
                    if(!discovered_devices_string.contains(dev_name)){
                        discovered_devices.add(newDevice);
                        discovered_devices_string.add(dev_name);
                        //Initiate the adapter.
                        discoveredAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,android.R.id.text1,discovered_devices_string);
                        listViewDiscoveredDevices.setAdapter(discoveredAdapter);
                        Log.d(TAG,"CF_mBroadCastReceiver1: New Device found");
                        Log.d(TAG,"CF_mBroadCastReceiver1: onReceive: "+dev_name + ": "+ newDevice.getAddress());
                    }
                }
            }
        }
    };

    // Create a BroadcastReceiver for Changed states.
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //Check if the action is a change in bound state.
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //Case 1: Already bonded.
                if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                    Toast.makeText(getActivity(),"Bonded with "+device.getName(),Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"CF_onReceive: Bonded with "+device.getName());
                    updatePairedDevices();
                }
                //Case 2: Creating a bond.
                if(device.getBondState() == BluetoothDevice.BOND_BONDING){
                    Toast.makeText(getActivity(),"Bonding with "+device.getName(),Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"CF_onReceive: Bonding with "+device.getName());
                }
                //Case 3: Breaking a bond.
                if(device.getBondState() == BluetoothDevice.BOND_NONE){
                    //Toast.makeText(getActivity(),"Breaking bond with "+device.getName(),Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"CF_onReceive: Breaking bond with "+device.getName());
                }
            }
        }
    };

    private void handleDiscoverClick(AdapterView<?> parent, View view, int position, long id){
        //cancel discovery to prolong battery life.
        bluetoothService.getAdapter().cancelDiscovery();
        //Get the clicked item (note its the string list we display)
        String clicked_device_string = (String) listViewDiscoveredDevices.getItemAtPosition(position);
        //Assume the lists are in same order
        BluetoothDevice clicked_device = discovered_devices.get(position);

        //Make insanity check to make sure they are in the same order.
        if (!clicked_device_string.equals(clicked_device.getName())) {
            //Not same name on BT device as the string list. Should not reach this state.
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        //Check if the selected device already is bonded. In that case, we dont need to do anything.
        pairedDevices = bluetoothService.getBondedDevices();
        if(pairedDevices.contains(clicked_device)){
            Toast.makeText(getActivity(),"Already paired",Toast.LENGTH_SHORT).show();
            return;
        }

        //In this state, we should bond the device.
        String device_name = clicked_device.getName();
        String device_address = clicked_device.getAddress();
        Log.d(TAG,"CF_onDiscoverClick: Device "+ device_name + " with address " + device_address + " clicked.");
        //create bond only supported jellybeanmr2+
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG,"CF_onDiscoverClick: Trying to bond with "+ device_name);
            clicked_device.createBond();
        }
    }

    //Discover unpaired devices and view in listview.
    private void updatePairedDevices(){
        //Make sure Bluetooth is initiated.
        if(initiateBt()) {
            Log.d(TAG,"CF_onPairedClick: updating paired devices");

            //get all bonded devices
            pairedDevices = bluetoothService.getBondedDevices();
            //Clear the list to rewrite devices.
            paired_devices.clear();
            paired_devices_string.clear();
            //Go through all paired devices and add them to the listviews
            for(BluetoothDevice b_d : pairedDevices){
                paired_devices.add(b_d);
                paired_devices_string.add(b_d.getName());
            }
            //create the adapter and set it to the paired string listview
            pairedAdapter = new ArrayAdapter<String>(root.getContext(),android.R.layout.simple_list_item_1,android.R.id.text1,paired_devices_string);
            listViewPairedDevices.setAdapter(pairedAdapter);
        }
    }

    private void handlePairedClick(AdapterView<?> parent, View view, int position, long id){
        //Assume string and device list is in same order.
        String clicked_device_string = (String) listViewPairedDevices.getItemAtPosition(position);
        BluetoothDevice clicked_device = paired_devices.get(position);

        if(!initiateBt()){
            //Make sure BT is initiated.
            return;
        }
        //Make sure to cancel to decrease battery drain.
        bluetoothService.getAdapter().cancelDiscovery();

        //Make insanity check.
        if (!clicked_device_string.equals(clicked_device.getName())) {
            //Should never reach this state.
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        String device_name = clicked_device.getName();
        String device_address = clicked_device.getAddress();
        Log.d(TAG,"CF_handlePairedClick: Paired Device "+ device_name + " with address " + device_address + " clicked.");
        //Make sure that another device isn't already in use.
        if(!bondSwitch.isChecked()) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Log.d(TAG, "CF_handlePairedClick: Switched to paired device " + device_name);
                //Update the textview
                TextView selected_device = (TextView) root.findViewById(R.id.id_selected_device);

                //Update the global selected device.
                selected_device.setText(device_name);
                selected_device_text = device_name;
                bluetoothService.setDevice(clicked_device);
                //connected_device = clicked_device;
            }
        }else{
            Toast.makeText(getActivity(),"Please disconnect the connected device.",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG,"CF_onSaveInstanceState");
        outState.putString("selected_device_text",selected_device_text);
        outState.putBoolean("switch_on",switch_on);
        super.onSaveInstanceState(outState);

        //Save the fragment's state here
    }

    /*
        Used when the activity is reset to save the fragments state in the activity (not in fragment)
        Should basically be same as onSaveInstanceState.
     */
    public Bundle getFragmentState(){
        Bundle outState = new Bundle();
        outState.putString("selected_device_text",selected_device_text);
        outState.putBoolean("switch_on",switch_on);
        return outState;
    }

    @Override
    public void onDestroy(){
        Log.d(TAG,"CF_onDestroy");
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver1);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver2);
        super.onDestroy();
    }
}
