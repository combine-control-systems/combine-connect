package CombineConnect.Combine;
/*
Fragment class for the manual fragment.
For documentation of fragments, see: https://developer.android.com/guide/components/fragments
 */
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;


public class ManualFragment extends Fragment implements Serializable,AddModeStateDialog.AddModeStateFragmentInterface,SaveLayoutDialog.SaveLayoutInterface, LoadLayoutDialog.LoadLayoutInterface, DebugConsole.TransmitUserMessageInterface, JoystickSettingsDialog.JoystickSettingsInterface {
    private String TAG = "MainActivity_debug";

    //Key for import of bluetooth service class object.
    private static final String BLUETOOTHSERVICE_KEY = "bluetooth_service";
    //Create the bluetooth service class object.
    private BluetoothService bluetoothService;
    //Bluetoothlistener
    private BluetoothService.BluetoothReceiveListener bluetoothReceiveListener;

    private View root;

    private boolean isPotrait = true;

    private static Bundle buttonBundle;
    private static Bundle joystickSettingsBundle;

    //joystick stuff
    private JoyStickView joyStickView;
    private final int JOYSTICKSIZE = 650;
    private long sampleTimeMs = 10;
    private Handler timerHandler = new Handler();
    private TextView angleTextView;
    private TextView powerTextView;
    private ImageButton joystickSettingsButton;
    private float maxAngle = 90;
    private float powerFactor = 1;
    private int angleQuadrantDefinition = 2;

    //Create ButtonLayout object.
    private ModeStateLayout modeStateLayout;
    private ImageButton addModeStateButton;

    //Create DebugConsole
    private DebugConsole debugConsole;
    private int debugHeight;
    private int debugWindowBelow;


    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateJoystickState();
            timerHandler.postDelayed(this,sampleTimeMs);
        }
    };


    /*
        Should be called every time one want to initialize a new object of this fragment.
     */
    public static ManualFragment newInstance(BluetoothService bluetoothService,Bundle fragmentState){
        ManualFragment manualFragment = new ManualFragment();
        Bundle args = new Bundle();
        args.putSerializable(BLUETOOTHSERVICE_KEY,bluetoothService);
        args.putBundle("fragmentState",fragmentState);
        if(fragmentState != null){
            buttonBundle = fragmentState.getBundle("buttonBundle");
            joystickSettingsBundle = fragmentState.getBundle("joystickSettingsBundle");
        }
        manualFragment.setArguments(args);
        return manualFragment;
    }

    /*
        Used to save the state of the fragment when it's not active.
        Everything that is not saved here will be reset on e.g. fragment switch or orientation switch.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        buttonBundle = modeStateLayout.getButtonBundle();
        outState.putBundle("buttonBundle",buttonBundle);
        outState.putBundle("joystickSettingsBundle",joystickSettingsBundle);
    }

    /*
        Used when the activity is reset to save the fragments state in the activity (not in fragment)
        Should basically be same as onSaveInstanceState.
     */
    public Bundle getFragmentState(){

        Bundle outState = new Bundle();
        buttonBundle = modeStateLayout.getButtonBundle();
        outState.putBundle("buttonBundle",buttonBundle);
        outState.putBundle("joystickSettingsBundle",buttonBundle);

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
                debugConsole.displayMessageReceived(message.substring(1,message.length()-1));
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
            root = inflater.inflate(R.layout.manual_fragment_landscape,container,false);
            debugHeight = 150;
            debugWindowBelow = R.id.id_manual_landscape_helper_view;
            isPotrait = false;
        } else {
            root = inflater.inflate(R.layout.manual_fragment_potrait,container,false);
            debugHeight = 600;
            debugWindowBelow = R.id.manual_add_button_layout;
            isPotrait = true;
        }

        addModeStateButton = root.findViewById(R.id.manual_add_button);
        addModeStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(modeStateLayout.getButtonCounter() < 2){
                    createModeStateView();
                }else {
                    Toast.makeText(getActivity(),"Max number of buttons in this view is 2!", Toast.LENGTH_LONG).show();
                }
            }
        });

        joyStickView = new JoyStickView(getContext(),JOYSTICKSIZE);
        timerHandler.postDelayed(timerRunnable,sampleTimeMs);
        angleTextView = root.findViewById(R.id.id_manual_steering_angle_value);
        powerTextView = root.findViewById(R.id.id_manual_power_value);

        //Recreate the saved state or initialize to standard state.
        if (savedInstanceState != null){
            buttonBundle = savedInstanceState.getBundle("buttonBundle");
            joystickSettingsBundle = savedInstanceState.getBundle("joystickSettingsBundle");
        }

        if(joystickSettingsBundle == null){
            joystickSettingsBundle = new Bundle();
            joystickSettingsBundle.putFloat("maxAngle",maxAngle);
            joystickSettingsBundle.putFloat("powerFactor",powerFactor);
            joystickSettingsBundle.putInt("angleQuadrantDefinition",angleQuadrantDefinition);
        }

        debugConsole = new DebugConsole((RelativeLayout) root.findViewById(R.id.fragment_manual),(MainActivity)getActivity(),debugHeight, debugWindowBelow,this);
        modeStateLayout = new ModeStateLayout(buttonBundle,(RelativeLayout)root.findViewById(R.id.id_manual_modestate_layout));

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        //does nothing if first time we start fragment, removes the old view if we restart.
        //removeJoystickView();
        //Add the new one.
        if(!joyStickView.isDrawn()){
            addJoystickView();
        }

        //updateButtonLocation();
        modeStateLayout.redrawButtonLayout((MainActivity)getActivity());
        debugConsole.applyDebugConsole((MainActivity)getActivity());
        debugConsole.displayMessageReceived("Debug Console Initiated!\nUse function CombineConnect::transmitDebugMessage to receive it here, or write message using the message box below to transmit to the unit!");
    }

    @Override
    public void onPause() {
        super.onPause();
        getFragmentState();
        joyStickView.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        joyStickView.onResume();
        MainActivity activity = (MainActivity)getActivity();
        activity.setMainState(activity.MANUAL);
        bluetoothService.setBluetoothReceiveListener(bluetoothReceiveListener);
        timerHandler.postDelayed(timerRunnable,sampleTimeMs);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_manual, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
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
        }
        return false;
    }

    @Override
    public void applyNewButton(MainActivity.ModeState modeState, char identifier, String name, AddModeStateDialog.MainStateModeButton button){
        modeStateLayout.applyNewButton((MainActivity)getActivity(),modeState,identifier,name,button);
    }

    private void createModeStateView(){
        AddModeStateDialog addModeStateDialog = new AddModeStateDialog();
        addModeStateDialog.setTargetFragment(this,0);
        addModeStateDialog.show(getActivity().getSupportFragmentManager(),"addModeStatedialogManual");
    }

    private void createJoystickSettingsView(){
        JoystickSettingsDialog joystickSettingsDialog = JoystickSettingsDialog.newInstance(joystickSettingsBundle);
        joystickSettingsDialog.setTargetFragment(this,0);
        joystickSettingsDialog.show(getActivity().getSupportFragmentManager(),"joystickSettingsDialog");
    }

    private void reset(){
        Log.d(TAG,"Attempting to reset view");
        modeStateLayout.reset();
        modeStateLayout.redrawButtonLayout((MainActivity)getActivity());
    }

    public void addJoystickView(){
        RelativeLayout relativeLayout = root.findViewById(R.id.fragment_manual);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                JOYSTICKSIZE,
                JOYSTICKSIZE
        );
        //layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,R.id.id_manual_power_text);

        if(isPotrait){
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            layoutParams.addRule(RelativeLayout.BELOW,debugConsole.getConsoleId());
            layoutParams.setMargins(0,40,0,0);
        }else {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            layoutParams.addRule(RelativeLayout.ABOVE,debugConsole.getConsoleId());
            layoutParams.setMargins(0,0,20,0);
        }

        joyStickView.setLayoutParams(layoutParams);
        int joystickId = View.generateViewId();
        joyStickView.setId(joystickId);

        relativeLayout.addView(joyStickView);

        joystickSettingsButton = new ImageButton(getActivity());
        RelativeLayout.LayoutParams settingsParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        settingsParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        settingsParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int px_right = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,//10dp
                getActivity().getResources().getDisplayMetrics()
        );
        settingsParams.setMargins(0,px_right,px_right,0);
        joystickSettingsButton.setLayoutParams(settingsParams);

        joystickSettingsButton.setImageResource(R.drawable.ic_settings_black_24dp);
        joystickSettingsButton.setBackgroundColor(Color.TRANSPARENT);
        TypedValue outValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,outValue,true);
        joystickSettingsButton.setBackgroundResource(outValue.resourceId);

        relativeLayout.addView(joystickSettingsButton);

        joystickSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createJoystickSettingsView();
            }
        });

        joyStickView.onResume();
    }

    public void removeJoystickView(){
        try {
            joyStickView.onPause();
            RelativeLayout relativeLayout = root.findViewById(R.id.fragment_manual);
            relativeLayout.removeView(joyStickView);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void updateJoystickState(){
        if(joyStickView.isStateUpdated()){
            float angle = joyStickView.getJoyStickAngle();
            float power = joyStickView.getPower();
            power = power*powerFactor;
            if(Math.abs(angle)>90){
                power = -power;
            }
            if(angleQuadrantDefinition==4){
                if(angle < 0){
                    angle = 180+(180+angle);
                }
            }else {
                angle = maxAngle*angle/(90*angleQuadrantDefinition);

                if(angle > maxAngle){
                    angle = maxAngle - (angle - maxAngle);
                }else if(angle < -maxAngle){
                    angle = -maxAngle - (angle + maxAngle);
                }
            }


            angleTextView.setText(String.format(Locale.ROOT,"%.3f",angle));


            powerTextView.setText(String.format(Locale.ROOT,"%.3f",power));
            joyStickView.setStateUpdated(false);

            if(bluetoothService != null){
                bluetoothService.transmitManualData(String.format(Locale.ROOT,"%.3f",angle),String.format(Locale.ROOT,"%.3f",power));
            }
        }
    }

    @Override
    public void loadLayout(String instanceToLoad) {
        Activity activity = getActivity();
        SharedPreferences sharedPreferences = activity.getSharedPreferences(instanceToLoad,activity.MODE_PRIVATE);
        Gson gson = new Gson();

        String modeStateListJson = sharedPreferences.getString("modeStateList",null);
        String buttonIdentifierListJson= sharedPreferences.getString("buttonIdentifierList",null);
        String butonNameListJson= sharedPreferences.getString("buttonNameList",null);
        String buttonIconListJson= sharedPreferences.getString("buttonIconList",null);
        String maxAngleJson = sharedPreferences.getString("maxAngle",null);
        String powerFactorJson = sharedPreferences.getString("powerFactor",null);
        String angleQuadrantDefinitionJson = sharedPreferences.getString("angleQuadrantDefinition",null);


        Type stringArrayListType = new TypeToken<ArrayList<String>>() {}.getType();
        Type intArrayListType = new TypeToken<ArrayList<Integer>>() {}.getType();
        Type floatType = new TypeToken<Float>() {}.getType();
        Type intType = new TypeToken<Integer>() {}.getType();

        buttonBundle = new Bundle();

        ArrayList<String> buttonIdentifierList = gson.fromJson(buttonIdentifierListJson,stringArrayListType);
        ArrayList<String> buttonNameList = gson.fromJson(butonNameListJson,stringArrayListType);
        ArrayList<Integer> modeStateList = gson.fromJson(modeStateListJson,intArrayListType);
        ArrayList<Integer> buttonIconList = gson.fromJson(buttonIconListJson,intArrayListType);

        applyJoystickSettings((float)gson.fromJson(maxAngleJson,floatType),(float)gson.fromJson(powerFactorJson,floatType),(int)gson.fromJson(angleQuadrantDefinitionJson,intType));

        buttonBundle.putIntegerArrayList("modeStateList",modeStateList);
        buttonBundle.putStringArrayList("buttonIdentifierList",buttonIdentifierList);
        buttonBundle.putStringArrayList("buttonNameList",buttonNameList);
        buttonBundle.putIntegerArrayList("buttonIconList",buttonIconList);
        modeStateLayout.setButtonBundle(buttonBundle);
        modeStateLayout.redrawButtonLayout((MainActivity)getActivity());
    }

    @Override
    public void saveLayout(String instanceName) {
        Activity activity = getActivity();
        //First check if the instanceName is already existing.
        SharedPreferences sharedPreferencesNames = activity.getSharedPreferences("sharedPreferencesNames",activity.MODE_PRIVATE);
        SharedPreferences.Editor namesEditor = sharedPreferencesNames.edit();
        namesEditor.putString(instanceName,instanceName);
        namesEditor.apply();

        SharedPreferences sharedPreferences = activity.getSharedPreferences(instanceName,activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Update all paramters by calling getFragmentState.
        getFragmentState();

        ArrayList<String> buttonIdentifierList = buttonBundle.getStringArrayList("buttonIdentifierList");
        ArrayList<String> buttonNameList = buttonBundle.getStringArrayList("buttonNameList");
        ArrayList<Integer> modeStateList = buttonBundle.getIntegerArrayList("modeStateList");
        ArrayList<Integer> buttonIconList = buttonBundle.getIntegerArrayList("buttonIconList");

        Gson gson = new Gson();
        String modeStateListJson = gson.toJson(modeStateList);
        String buttonIdentifierListJson= gson.toJson(buttonIdentifierList);
        String butonNameListJson= gson.toJson(buttonNameList);
        String buttonIconListJson= gson.toJson(buttonIconList);
        String maxAngleJson = gson.toJson(maxAngle);
        String powerFactorJson = gson.toJson(powerFactor);
        String angleQuadrantDefinitionJson = gson.toJson(angleQuadrantDefinition);

        editor.putString("modeStateList",modeStateListJson);
        editor.putString("buttonIdentifierList",buttonIdentifierListJson);
        editor.putString("buttonNameList",butonNameListJson);
        editor.putString("buttonIconList",buttonIconListJson);
        editor.putString("maxAngle",maxAngleJson);
        editor.putString("powerFactor",powerFactorJson);
        editor.putString("angleQuadrantDefinition",angleQuadrantDefinitionJson);

        editor.apply();
    }

    @Override
    public void transmitUserMessage(String message) {
        bluetoothService.transmitUserMessage(message);
    }

    @Override
    public void applyJoystickSettings(float maxAngle,float powerFactor,int angleQuadrantDefinition) {
        this.maxAngle = maxAngle;
        this.powerFactor = powerFactor;
        this.angleQuadrantDefinition = angleQuadrantDefinition;
        joystickSettingsBundle.putFloat("maxAngle",maxAngle);
        joystickSettingsBundle.putFloat("powerFactor",powerFactor);
        joystickSettingsBundle.putInt("angleQuadrantDefinition",angleQuadrantDefinition);

    }

    public class JoyStickView extends SurfaceView implements Runnable, View.OnTouchListener {

        Thread joyStickThread = null;
        public SurfaceHolder holder;

        boolean isRunning = false;
        final float joyStickSize;
        final float outerCircleRadius;
        final float innerCircleRadius;
        final float origin;

        private Paint joyStickPaint;

        private float x;
        private float y;

        //x expressed in center coordinates.
        private float xc;
        //y expressed in center coordinates.
        private float yc;

        private float angle;
        private float power;

        private final float maxPower;

        private boolean stateUpdated = false;
        private boolean drawn = false;

        public JoyStickView(Context context, float joyStickSize) {
            super(context);
            holder = getHolder();
            setZOrderOnTop(true);
            holder.setFormat(PixelFormat.TRANSPARENT);
            setOnTouchListener(this);
            //holder.setFixedSize((int)(2*joyStickRadius),(int)(2*joyStickRadius));
            final JoyStickView joyStickView = this;
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    onResume();
                    drawn = true;
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    onPause();
                }
            });

            this.joyStickSize = joyStickSize;
            innerCircleRadius = joyStickSize/8;
            outerCircleRadius = (joyStickSize - 2*innerCircleRadius)/2;
            origin = joyStickSize/2;

            maxPower = outerCircleRadius;
            x = origin;
            y = origin;
            xc = 0;
            yc = 0;
            joyStickPaint = new Paint();
            joyStickPaint.setColor(getResources().getColor(R.color.colorAccent));
            joyStickPaint.setStyle(Paint.Style.STROKE);
        }

        @Override
        public void run() {
            while (isRunning){
                //Wait until surface is valid
                if(!holder.getSurface().isValid()){
                    continue;
                }

                try {
                    Canvas c = holder.lockCanvas();
                    c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    c.drawLine(innerCircleRadius, origin, joyStickSize-innerCircleRadius, origin,joyStickPaint);
                    c.drawLine(origin,innerCircleRadius, origin, joyStickSize-innerCircleRadius,joyStickPaint);
                    c.drawCircle(origin, origin, outerCircleRadius,joyStickPaint);
                    c.drawCircle(x, y, innerCircleRadius,joyStickPaint);
                    holder.unlockCanvasAndPost(c);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //Add a sleep to save some CPU..
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(event.getAction() == MotionEvent.ACTION_MOVE){
                x = event.getX();
                y = event.getY();
            }else {
                x = origin;
                y = origin;
            }
            /* Transformation matrix from event surfaceview coordinates -> center coordinates:
                0   -1  0   origin
                1   0   0   -origin
                0   0   1   0
                0   0   0   1
                 */
            xc = -y + origin;
            yc = x - origin;

            //Calculate angle
            angle = (float)Math.atan2(yc, xc);

            //Saturate
            float xcMax = (float)Math.abs(outerCircleRadius*Math.cos(angle));
            float ycMax = (float)Math.abs(outerCircleRadius*Math.sin(angle));
            xc = xc > xcMax ? xcMax : xc;
            yc = yc > ycMax ? ycMax: yc;
            xc = xc < -xcMax ?-xcMax: xc;
            yc = yc < -ycMax ?-ycMax: yc;

            //Calcluate power
            power = (float)Math.sqrt(Math.pow(yc,2) + Math.pow(xc,2));


            //Transform back to get saturated version of x,y:
            x = origin + yc;
            y = origin - xc;
            stateUpdated = true;
            return true;
        }

        public void onPause(){
            isRunning = false;
            //stop thread, sometimes it can take some time, so try until it succeeds.
            while (true){
                try {
                    joyStickThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        public void onResume(){
            isRunning = true;
            joyStickThread = new Thread(joyStickView);
            joyStickThread.start();
            Log.d(TAG,"Surface created!");
        }

        private boolean isStateUpdated(){
            return stateUpdated;
        }

        private void setStateUpdated(boolean isStateUpdated){
            stateUpdated = isStateUpdated;
        }

        private float getJoyStickAngle(){
            return angle*180/(float)Math.PI;
        }

        private float getPower(){
            return power/maxPower;
        }

        private boolean isDrawn(){
            return drawn;
        }
    }

}

