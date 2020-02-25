package CombineConnect.Combine;
/*
Fragment class for the Debug fragment.
For documentation of fragments, see: https://developer.android.com/guide/components/fragments
 */
import android.content.res.Configuration;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;



import java.io.Serializable;



public class DebugConsoleFragment extends Fragment implements Serializable, DebugConsole.TransmitUserMessageInterface{
    private String TAG = "MainActivity_debug";

    //Key for import of bluetooth service class object.
    private static final String BLUETOOTHSERVICE_KEY = "bluetooth_service";
    //Create the bluetooth service class object.
    private BluetoothService bluetoothService;
    //Bluetoothlistener
    private BluetoothService.BluetoothReceiveListener bluetoothReceiveListener;

    private View root;

    private boolean isPotrait = true;

    //Create DebugConsole
    private DebugConsole debugConsole;
    private int debugHeight;
    private int debugWindowBelow;


    /*
        Should be called every time one want to initialize a new object of this fragment.
     */
    public static DebugConsoleFragment newInstance(BluetoothService bluetoothService, Bundle fragmentState){
        DebugConsoleFragment debugConsoleFragment = new DebugConsoleFragment();
        Bundle args = new Bundle();
        args.putSerializable(BLUETOOTHSERVICE_KEY,bluetoothService);
        args.putBundle("fragmentState",fragmentState);
        if(fragmentState != null){
            //joystickSettingsBundle = fragmentState.getBundle("joystickSettingsBundle");
        }
        //manualFragment.setArguments(args);
        debugConsoleFragment.setArguments(args);
        return debugConsoleFragment;
    }

    /*
        Used to save the state of the fragment when it's not active.
        Everything that is not saved here will be reset on e.g. fragment switch or orientation switch.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //buttonBundle = modeStateLayout.getButtonBundle();
        //outState.putBundle("buttonBundle",buttonBundle);
    }

    /*
        Used when the activity is reset to save the fragments state in the activity (not in fragment)
        Should basically be same as onSaveInstanceState.
     */
    public Bundle getFragmentState(){

        Bundle outState = new Bundle();
        //buttonBundle = modeStateLayout.getButtonBundle();
        //outState.putBundle("buttonBundle",buttonBundle);
        return outState;
    }

    /*
        Will initialize the bluetoothservice object if its attached as an argument to fragment transaction.
        Also sets the global factors.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if(getArguments() != null){
            Bundle bundle = getArguments();
            bluetoothService = (BluetoothService) bundle.getSerializable(BLUETOOTHSERVICE_KEY);
        }
        bluetoothReceiveListener = new BluetoothService.BluetoothReceiveListener() {
            @Override
            public void onPlotDataReceived(String message) {
                //Do nothing, not interested in that data here..
            }

            @Override
            public void onPlotTransactionRulesReceived(String message) {
                //Do nothing, not interested in that data here..
            }

            @Override
            public void onDebugMessageReceived(String message) {
                try {
                    debugConsole.displayMessageReceived(message.substring(1,message.length()-1));
                } catch (StringIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    //Incorrect message received.
                }
            }
        };
    }


    /*
        Runs once every time the view is created.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isPotrait = false;
        } else {
            isPotrait = true;
        }
        debugHeight = RelativeLayout.LayoutParams.MATCH_PARENT;
        debugWindowBelow = -1;

        root = inflater.inflate(R.layout.debug_console_fragment,container,false);

        //Recreate the saved state or initialize to standard state.
        if (savedInstanceState != null){
            //buttonBundle = savedInstanceState.getBundle("buttonBundle");
        }


        debugConsole = new DebugConsole((RelativeLayout) root.findViewById(R.id.id_debug_console_fragment_layout),(MainActivity)getActivity(),debugHeight, debugWindowBelow,this);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        debugConsole.applyDebugConsole((MainActivity)getActivity());
        debugConsole.displayMessageReceived("Debug Console Initiated!\nUse function CombineConnect::transmitDebugMessage to receive it here, or write message using the message box below to transmit to the unit!");
    }

    @Override
    public void onPause() {
        super.onPause();
        getFragmentState();
    }

    @Override
    public void onResume() {
        super.onResume();
        bluetoothService.setBluetoothReceiveListener(bluetoothReceiveListener);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //inflater.inflate(R.menu.menu_manual, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /*switch (item.getItemId()){
            case R.id.settings_manual_reset:
                reset();
                return true;
            case R.id.settings_manual_save_layout:
                SaveLayoutDialog saveLayoutDialog = SaveLayoutDialog.newInstance("ManualFragment");
                saveLayoutDialog.setTargetFragment(this,0);
                saveLayoutDialog.show(getActivity().getSupportFragmentManager(),"saveManualLayout");
                return true;
            case R.id.settings_manual_load_layout:
                LoadLayoutDialog loadLayoutDialog = LoadLayoutDialog.newInstance("ManualFragment");
                loadLayoutDialog.setTargetFragment(this,0);
                loadLayoutDialog.show(getActivity().getSupportFragmentManager(),"loadManualLayout");
            default:
                break;
        }*/
        return false;
    }

    @Override
    public void transmitUserMessage(String message) {
        bluetoothService.transmitUserMessage(message);
    }


}

