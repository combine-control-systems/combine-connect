package CombineConnect.Combine;
/*
This is the main class from where the activity is controlled.
The combine Connect only have one activity running in different fragments, this could be optimized.
For documentation of android activity, see https://developer.android.com/reference/android/app/Activity.html
 */
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
//TODO: Add step response functionality to the calibration fragment
//TODO: Optimize performance through multiple activities
//TODO: Check "android service" and see if we should switch bluetoothservice class to that (to prevent disconnection upon screen rotation)
//TODO: Add possibility to add/remove joysticks in manual fragment
//TODO: Make it possible for the user to add more stuff to the plot on creation. E.g. multiple signals, change name of labels, adjustable range.. This should be saved during entire session.
/*
This class is always active when the app is active.
This is the only activity that the app is using.
For more info on activities and its lifecycles, see https://developer.android.com/reference/android/app/Activity
 */
public class MainActivity extends AppCompatActivity {
    //Tag to use in logcat
    private String TAG = "MainActivity_debug";

    private static final String BLUETOOTHSERVICE_KEY = "bluetooth_service";

    //---- objects used for the navigation view----
    //The view in which useful information can be found:
    private DrawerLayout drawerLayout;
    //The toggle button:
    private ActionBarDrawerToggle actionbar_toggle;
    //The buttons in the navigation panel:
    private Button nav_connect_b;
    private Button nav_calibration_b;
    private Button nav_manual_b;
    private Button nav_plot_b;
    private Button nav_debug_console_b;
    private ImageButton updateMainStatesButton;
    private ImageButton updateModesButton;

    //---- objects used for fragments----
    private ConnectFragment connectFragment;
    private CalibrationFragment calibrationFragment;
    private ModeFragment modeFragment;
    private ManualFragment manualFragment;
    private PlotFragment plotFragment;
    private DebugConsoleFragment debugConsoleFragment;

    private Bundle connectFragmentState;
    private Bundle calibrationFragmentState;
    private Bundle modeFragmentState;
    private Bundle manualFragmentState;
    private Bundle plotFragmentState;
    private Bundle debugConsoleFragmentState;


    public enum Fragments {
        CONNECT,
        CALIBRATION,
        MODE,
        MANUAL,
        PLOT,
        DEBUGCONSOLE
    }
    public Fragments activated_frag;
    public Fragments last_frag;

    //---- objects used for modes and states----
    private Bundle modes;
    private Bundle mainStates;
    private ArrayList<String> mainStateIdentifiersList;
    private ArrayList<String> mainStateVariableNamesList;
    private ArrayList<Integer> enabledMainStates;
    private ArrayList<String> modeIdentifiersList;
    private ArrayList<String> modeVariableNamesList;
    private ArrayList<Integer> enabledModes;
    private ArrayList<MainState> mainStateArrayList;
    private ArrayList<Mode> modeArrayList;

    public enum ModeState{
        MAINSTATE(0),
        MODE(1);

        private final int value;
        ModeState(int value){
            this.value = value;
        }
        public int getValue(){
            return value;
        }
    }

    public final char MANUAL = ';';

    //---- Bluetooth service objects----
    private Bundle BTS_bundle;
    private BluetoothService bluetoothService;


    //---- Global variables ----
    private boolean connect_fragment_watcher = false;
    private boolean calibration_fragment_watcher = false;
    private boolean mode_fragment_watcher = false;
    private boolean manual_fragment_watcher = false;
    private boolean plot_fragment_watcher = false;
    private boolean debug_console_fragment_watcher = false;

    private boolean mainStateRequested = false;
    private boolean modeRequested = false;


    /*
    See activity lifecycle
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"MainActivty: onDestroy");
    }

    /*
    Save the states of the fragments and the bluetoothservice object.
    Note that the bluetooth service object can not be destroyed and sent here as this method can be executed without the activity being destroyed and recreated.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle bluetoothServiceBundle = bluetoothService.getState();

        if(calibration_fragment_watcher){
            calibrationFragmentState = calibrationFragment.getFragmentState();
        }
        if(connect_fragment_watcher){
            connectFragmentState = connectFragment.getFragmentState();
        }
        if(mode_fragment_watcher){
            modeFragmentState = modeFragment.getFragmentState();
        }
        if(manual_fragment_watcher){
            manualFragmentState = manualFragment.getFragmentState();
        }
        if(plot_fragment_watcher){
            plotFragmentState = plotFragment.getFragmentState();
        }
        if(debug_console_fragment_watcher){
            debugConsoleFragmentState = debugConsoleFragment.getFragmentState();
        }

        outState.putBundle(BLUETOOTHSERVICE_KEY,bluetoothServiceBundle);
        outState.putBundle("calibrationFragmentState", calibrationFragmentState);
        outState.putBundle("connectFragmentState",connectFragmentState);
        outState.putBundle("modeFragmentState",modeFragmentState);
        outState.putBundle("manualFragmentState",manualFragmentState);
        outState.putBundle("plotFragmentState",plotFragmentState);
        outState.putBundle("debugConsoleFragmentState",debugConsoleFragmentState);

        enabledMainStates = new ArrayList<Integer>();
        for(MainState mainState : mainStateArrayList){
            int isEnabled = mainState.getRadioButton().isChecked() ? 1:0;
            enabledMainStates.add(isEnabled);
        }
        mainStates.putIntegerArrayList("enabledMainStates",enabledMainStates);
        mainStates.putStringArrayList("mainStateIdentifiersList",mainStateIdentifiersList);
        mainStates.putStringArrayList("mainStateVariableNamesList",mainStateVariableNamesList);
        enabledModes = new ArrayList<Integer>();
        for(Mode mode : modeArrayList){
            int isEnabled = mode.getCheckBox().isChecked() ? 1:0;
            Log.d(TAG,String.valueOf(isEnabled));
            enabledModes.add(isEnabled);
        }
        modes.putIntegerArrayList("enabledModes",enabledModes);
        modes.putStringArrayList("modeIdentifiersList",modeIdentifiersList);
        modes.putStringArrayList("modeVariableNamesList",modeVariableNamesList);

        outState.putBundle("mainStates",mainStates);
        outState.putBundle("modes",modes);

        //Save the information of which fragment that is activated.
        outState.putSerializable("activated_frag",activated_frag);
        outState.putSerializable("last_frag",last_frag);
        bluetoothService.onDestroy();
        outState.putSerializable("bluetoothService",bluetoothService);

        //Set to BTS_bundle in the case that activity is not destroyed.
        BTS_bundle = bluetoothServiceBundle;
        Log.d(TAG,"MainActivity: Activity state saved");
    }

    /*
    See activity lifecycle
     */
    @Override
    protected void onStart() {
        Log.d(TAG,"MainActivity: onStart");
        bluetoothService.open(this,BTS_bundle);
        super.onStart();
    }

    /*
    See activity lifecycle
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"MainActivity_onCreate: Start");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Make the navigation button work:
        drawerLayout = findViewById(R.id.id_drawer_layout);
        actionbar_toggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open,R.string.close){
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if(slideOffset == 0 && activated_frag == Fragments.MANUAL){
                    manualFragment.addJoystickView();
                }
                else if(slideOffset != 0 && activated_frag == Fragments.MANUAL){
                    manualFragment.removeJoystickView();
                }
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        drawerLayout.addDrawerListener(actionbar_toggle);
        actionbar_toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //--------Create the update buttons in the navigation views------
        updateMainStatesButton = findViewById(R.id.id_refresh_main_states_button);
        updateMainStatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothService != null){
                    bluetoothService.transmitRequestNewTransactionRules(BluetoothService.Keywords.MAINSTATE);
                }
            }
        });

        updateModesButton = findViewById(R.id.id_refresh_modes_button);
        updateModesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothService!=null){
                    bluetoothService.transmitRequestNewTransactionRules(BluetoothService.Keywords.MODE);
                }
            }
        });

        //---------Initiate watchers ------------------------------
        connect_fragment_watcher = false;
        calibration_fragment_watcher = false;
        mode_fragment_watcher = false;
        manual_fragment_watcher = false;
        plot_fragment_watcher = false;
        debug_console_fragment_watcher = false;
        //uneFilterFragment= new ModeFragment();

        //---------Put the app in the correct state-------------------
        //If savedInstanceState == null, then the app has just started.
        if(savedInstanceState == null){
            bluetoothService = new BluetoothService(this);

            //Initialize all fragments and bluetooth service, switch to connect.
            calibrationFragment = CalibrationFragment.newInstance(bluetoothService,null);
            connectFragment = ConnectFragment.newInstance(bluetoothService,null);
            modeFragment = ModeFragment.newInstance(bluetoothService,null);
            manualFragment = ManualFragment.newInstance(bluetoothService,null);
            plotFragment = plotFragment.newInstance(bluetoothService,null);
            debugConsoleFragment = debugConsoleFragment.newInstance(bluetoothService,null);
            activated_frag = Fragments.CONNECT;
            executeFragmentTransaction(Fragments.CONNECT);

            //Initiate state and mode objects
            mainStates = new Bundle();
            modes = new Bundle();
            mainStateIdentifiersList = new ArrayList<String>();
            modeIdentifiersList = new ArrayList<String>();
            mainStateVariableNamesList = new ArrayList<String>();
            modeVariableNamesList = new ArrayList<String>();
            modeArrayList = new ArrayList<Mode>();
            mainStateArrayList= new ArrayList<MainState>();

            Log.d(TAG,"MainActivity: No saved instance state!");

        }else{
            /*
                Create new instances of the fragments.
                Note that FragmentManager will automatically recreate the last activated fragment.
                However, as the BTS object of the old fragment is nulled and a different object than the other fragments,
                this fragment will be created and then destroyed to be replaced with the new object created here.
             */
            bluetoothService = (BluetoothService)(savedInstanceState.getSerializable("bluetoothService"));
            connectFragmentState = savedInstanceState.getBundle("connectFragmentState");
            calibrationFragmentState = savedInstanceState.getBundle("calibrationFragmentState");
            modeFragmentState = savedInstanceState.getBundle("modeFragmentState");
            manualFragmentState = savedInstanceState.getBundle("manualFragmentState");
            plotFragmentState = savedInstanceState.getBundle("plotFragmentState");
            debugConsoleFragmentState = savedInstanceState.getBundle("debugConsoleFragmentState");

            calibrationFragment = CalibrationFragment.newInstance(bluetoothService, calibrationFragmentState);
            connectFragment = ConnectFragment.newInstance(bluetoothService,connectFragmentState);
            modeFragment = ModeFragment.newInstance(bluetoothService,modeFragmentState);
            manualFragment = ManualFragment.newInstance(bluetoothService,manualFragmentState);
            plotFragment = PlotFragment.newInstance(bluetoothService,plotFragmentState);
            debugConsoleFragment = DebugConsoleFragment.newInstance(bluetoothService,plotFragmentState);

            //Replace the automatically recreated (broken) fragment with the new.
            executeFragmentTransaction((Fragments)savedInstanceState.getSerializable("activated_frag"));
            last_frag = (Fragments) savedInstanceState.getSerializable("last_frag");

            //Get the bluetoothservice state.
            BTS_bundle = savedInstanceState.getBundle(BLUETOOTHSERVICE_KEY);

            //Recreate the main states and modes.
            mainStates = savedInstanceState.getBundle("mainStates");
            modes = savedInstanceState.getBundle("modes");
            mainStateIdentifiersList = mainStates.getStringArrayList("mainStateIdentifiersList");
            mainStateVariableNamesList = mainStates.getStringArrayList("mainStateVariableNamesList");
            enabledMainStates = mainStates.getIntegerArrayList("enabledMainStates");
            modeIdentifiersList = modes.getStringArrayList("modeIdentifiersList");
            modeVariableNamesList = modes.getStringArrayList("modeVariableNamesList");
            enabledModes = modes.getIntegerArrayList("enabledModes");
            recreateMainStateTransactionRules();
            recreateModeTransactionRules();


            Log.d(TAG,"MainActivity: state recreated");
        }


        // -----------Set the navigation buttons:---------------------------
        nav_connect_b = findViewById(R.id.id_connect_button);
        nav_connect_b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                executeFragmentTransaction(Fragments.CONNECT);
                drawerLayout.closeDrawers();

            }
        });

        nav_calibration_b = findViewById(R.id.id_calibration_button);
        nav_calibration_b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                executeFragmentTransaction(Fragments.CALIBRATION);
                drawerLayout.closeDrawers();
            }
        });

        nav_manual_b = findViewById(R.id.id_manual_button);
        nav_manual_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeFragmentTransaction(Fragments.MANUAL);
                drawerLayout.closeDrawers();
            }
        });

        nav_plot_b = findViewById(R.id.id_plot_button);
        nav_plot_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeFragmentTransaction(Fragments.PLOT);
                drawerLayout.closeDrawers();
            }
        });

        nav_debug_console_b = findViewById(R.id.id_debug_console_button);
        nav_debug_console_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeFragmentTransaction(Fragments.DEBUGCONSOLE);
                drawerLayout.closeDrawers();
            }
        });

    }

    /*
    If bluetooth is disconnected (e.g. out of range or user shut off hardware), this method will be run
     */
    public void onBluetoothDisconnect(){
        ImageView imageView = (ImageView)findViewById(R.id.id_toolbar_bluetooth_icon);
        imageView.setImageResource(R.drawable.ic_bluetooth_disabled);

        //Turn switch off if we are in connectFragment.
        if(activated_frag == Fragments.CONNECT){
            ((Switch) findViewById(R.id.id_bond_switch)).setChecked(false);
        }
        //If we are not in connectFragment, this will force an attempt of reconnect when we switch back.
        connectFragment.setSwitchState(false);
    }

    /*
    This method is called when user switches fragments
     */
    private void executeFragmentTransaction(Fragments fragments){
        last_frag = activated_frag;
        activated_frag = fragments;
        switch(fragments){
            case CONNECT:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,connectFragment,"connectFragment").commit();
                connect_fragment_watcher = true;
                break;
            case CALIBRATION:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, calibrationFragment,"calibrationFragment").commit();
                calibration_fragment_watcher = true;
                break;
            case MODE:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,modeFragment,"modeFragment").commit();
                mode_fragment_watcher = true;
                break;
            case MANUAL:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,manualFragment,"manualFragment").commit();
                manual_fragment_watcher = true;
                break;
            case PLOT:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,plotFragment,"plotFragment").commit();
                plot_fragment_watcher = true;
                break;
            case DEBUGCONSOLE:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,debugConsoleFragment,"debugConsoleFragment").commit();
                debug_console_fragment_watcher = true;
                break;
        }
    }

    /*
    Transmits the chosen Main State
     */
    public void transmitCurrentMainState(char identifier){
        if(bluetoothService!=null){
            bluetoothService.transmitMainState(identifier);
        }
    }

    /*
    Transmits the state of the mode that changed
     */
    public void transmitMode(char identifier, int val){
        if(bluetoothService!=null){
            bluetoothService.transmitMode(identifier,val);
        }
    }

    /*
    Is executed whenever a radiobutton corresponding to the main state is changed.
     */
    public void onMainStateChange(char identifier, boolean isChecked){
        if(!mainStateRequested){
            if(isChecked){
                Log.d(TAG,"STATE" + identifier + "checked.");
                transmitCurrentMainState(identifier);
            }else{
                Log.d(TAG,"STATE" + identifier + "unchecked.");
            }
            drawerLayout.closeDrawers();
        }else{
            mainStateRequested = false;
        }

    }

    /*
    Is executed whenever a checkbox corresponding to the modes is changed.
     */
    public void onModeChange(char identifier, boolean isChecked){
        if(!modeRequested){
            if(isChecked){
                Log.d(TAG,"Mode" + identifier + "checked.");
                transmitMode(identifier,1);
            }else{
                Log.d(TAG,"Mode" + identifier + "unchecked.");
                transmitMode(identifier,0);
            }
        }else{
            modeRequested = false;
        }
    }

    public int setMainState(char identifier){
        for(MainState mainState: mainStateArrayList){
            if(identifier == mainState.getIdentifier()){
                mainState.getRadioButton().setChecked(true);
                return 1;
            }
        }
        return -1;
    }

    public int setMode(char identifier){
        for(Mode mode: modeArrayList){
            if(identifier == mode.getIdentifier()){
                boolean checked = !mode.getCheckBox().isChecked();
                mode.getCheckBox().setChecked(checked);
                return checked ? 1 : 0;
            }
        }
        return -1;
    }

    /*
    removes all radiobuttons and clears all objects related to the main state
     */
    public void resetMainStates(){
        RadioGroup mainStateGroup = findViewById(R.id.main_states_group);
        mainStateGroup.removeAllViews();
        mainStateVariableNamesList = new ArrayList<String>();
        mainStateIdentifiersList = new ArrayList<String>();
        mainStateArrayList = new ArrayList<MainState>();
    }

    /*
    removes all checkboxes and clears all objects related to the modes
     */
    public void resetModes(){
        RelativeLayout modeLayout = findViewById(R.id.id_modes);
        for(Mode mode : modeArrayList){
            modeLayout.removeView(mode.getCheckBox());
        }
        modeVariableNamesList = new ArrayList<String>();
        modeIdentifiersList = new ArrayList<String>();
        modeArrayList = new ArrayList<Mode>();
    }

    /*
    Is called upon activity recreation (see activity lifecycle) to make sure we dont temporarily send incorrect state values.
     */
    public void recreateMainStateTransactionRules(){
        mainStateArrayList = new ArrayList<MainState>();
        for(int i = 0;i<mainStateIdentifiersList.size();i++){
            MainState mainState = new MainState(this,mainStateIdentifiersList.get(i).charAt(0),mainStateVariableNamesList.get(i));
            if(enabledMainStates.get(i) == 1){
                mainStateRequested = true;
                mainState.getRadioButton().setChecked(true);
            }
            mainStateArrayList.add(mainState);
        }
    }

    /*
    Is called upon activity recreation (see activity lifecycle) to make sure we dont temporarily send incorrect mode values.
     */
    public void recreateModeTransactionRules(){
        modeArrayList = new ArrayList<Mode>();
        int layoutBelow = R.id.id_modes_separator;
        for(int i = 0;i<modeVariableNamesList.size();i++){
            Mode mode= new Mode(this,modeIdentifiersList.get(i).charAt(0),modeVariableNamesList.get(i),layoutBelow);
            if(enabledModes.get(i)==1){
                modeRequested = true;
                mode.getCheckBox().setChecked(true);
            }
            modeArrayList.add(mode);
            layoutBelow = mode.getCheckBoxId();
        }
    }

    /*
    Is called whenever we receive transactionrules for the main state.
     */
    public void onMainStateTransactionRulesReceived(String rules){
        resetMainStates();

        String[] stringVals = rules.split(",");
        int len = stringVals.length;
        int i = 1;
        //Run until initiation vector starts (but not more than len in case of truncated string).
        while(i < len){
            mainStateVariableNamesList.add(stringVals[i]);
            mainStateIdentifiersList.add(stringVals[i+1]);
            MainState mainState = new MainState(this,stringVals[i+1].charAt(0),stringVals[i]);
            mainStateArrayList.add(mainState);
            i+=2;
            if(stringVals[i].equals(String.valueOf(BluetoothService.Keywords.MAINSTATE.getValue()))){
                break;
            }
        }
        int j = 0;
        i++;
        while(i<len){
            boolean checked = stringVals[i++].equals("1");
            mainStateRequested = checked;
            mainStateArrayList.get(j++).getRadioButton().setChecked(checked);
        }

        bluetoothService.transmitApproveTransactionRules(BluetoothService.Keywords.MAINSTATE);
    }

    /*
    Is called whenever we receive transactionrules for the mdoes
     */
    public void onModeTransactionRulesReceived(String rules){
        resetModes();
        int layoutBelow = R.id.id_modes_separator;
        String[] stringVals = rules.split(",");
        int len = stringVals.length;
        int i = 1;
        //Run until initiation vector starts (but not more than len in case of truncated string).
        while(i<len){
            modeVariableNamesList.add(stringVals[i]);
            modeIdentifiersList.add(stringVals[i+1]);
            Mode mode = new Mode(this,stringVals[i+1].charAt(0),stringVals[i],layoutBelow);
            //mode.getCheckBox().setChecked(stringVals[i+2].equals("1"));
            modeArrayList.add(mode);
            layoutBelow = mode.getCheckBoxId();

            i+=2;
            if(stringVals[i].equals(String.valueOf(BluetoothService.Keywords.MODE.getValue()))){
                break;
            }
        }
        int j = 0;
        i++;
        while(i<len){
            boolean checked = stringVals[i++].equals("1");
            modeRequested = checked;
            modeArrayList.get(j++).getCheckBox().setChecked(checked);
        }
        bluetoothService.transmitApproveTransactionRules(BluetoothService.Keywords.MODE);
    }

    public ArrayList<MainState> getMainStateArrayList(){
        return mainStateArrayList;
    }

    public ArrayList<Mode> getModeArrayList(){
        return modeArrayList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.settings_remove_all_layouts:
                if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                    try {
                        ((ActivityManager)getSystemService(ACTIVITY_SERVICE))
                                .clearApplicationUserData(); // note: it has a return value!
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                break;
        }

        if(actionbar_toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //Go to the last fragment.
        executeFragmentTransaction(last_frag);
    }

}
