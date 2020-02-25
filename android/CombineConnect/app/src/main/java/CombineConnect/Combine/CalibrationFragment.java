package CombineConnect.Combine;
/*
Fragment class for the calibration fragment.
For documentation of fragments, see: https://developer.android.com/guide/components/fragments
Contain subclass Tunebar, which holds all data for the existing tunebars.
 */


import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

public class CalibrationFragment extends Fragment implements Serializable, AddTunebarDialog.TunebarFragmentInterface, AddModeStateDialog.AddModeStateFragmentInterface,SaveLayoutDialog.SaveLayoutInterface, LoadLayoutDialog.LoadLayoutInterface {
    private String TAG = "MainActivity_debug";

    //Key for import of bluetooth service class object.
    private static final String BLUETOOTHSERVICE_KEY = "bluetooth_service";
    //Create the bluetooth service class object.
    private BluetoothService bluetoothService;

    //Create ButtonLayout object.
    private ModeStateLayout modeStateLayout;

    //Factor that controls the sensitivity of the seekbar via res/values/strings -> p_max_value
    private double global_factor;

    private static ArrayList<String> factorTextList;
    private static ArrayList<Integer> potensList;
    private static ArrayList<String> variableNameList;
    private static ArrayList<String> offsetList;
    private static Bundle buttonBundle;

    private enum sourceEnum {
        SEEKBAR,
        FACTOR,
        FINAL
    }

    //Create run/stop buttons
    private ImageButton addButton;

    //Create the view for global access.
    private View root;

    private ArrayList<TuneBar> tuneBarArrayList;
    private int lastLayoutAddedId;
    private boolean created = false;

    private int layout_offset;
    private String init_offset = "0";
    private String init_val = "0.00";
    private int init_potens = 2;

    /*
        Should be called every time one want to initialize a new object of this fragment.
     */
    public static CalibrationFragment newInstance(BluetoothService bluetoothService, Bundle fragmentState){
        CalibrationFragment calibrationFragment = new CalibrationFragment();
        Bundle args = new Bundle();
        args.putSerializable(BLUETOOTHSERVICE_KEY,bluetoothService);
        args.putBundle("fragmentState",fragmentState);
        if(fragmentState != null){
            factorTextList = fragmentState.getStringArrayList("factorTextList");
            potensList = fragmentState.getIntegerArrayList("potensList");
            variableNameList = fragmentState.getStringArrayList("variableNameList");
            offsetList = fragmentState.getStringArrayList("offsetList");
            buttonBundle = fragmentState.getBundle("buttonBundle");
        }
        calibrationFragment.setArguments(args);
        return calibrationFragment;
    }

    /*
        Used to save the state of the fragment when it's not active.
        Everything that is not saved here will be reset on e.g. fragment switch or orientation switch.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        factorTextList = new ArrayList<String>();
        potensList = new ArrayList<Integer>();
        offsetList = new ArrayList<String>();
        if(tuneBarArrayList!=null){
            for(TuneBar tuneBar : tuneBarArrayList){
                Bundle tuneBarBundle = tuneBar.getTuneBarState();
                factorTextList.add(tuneBarBundle.getString("factor_text"));
                potensList.add(tuneBarBundle.getInt("selected_potens"));
                offsetList.add(tuneBarBundle.getString("offset"));
            }
        }

        buttonBundle = modeStateLayout.getButtonBundle();

        outState.putBundle("buttonBundle",buttonBundle);
        outState.putStringArrayList("factorTextList",factorTextList);
        outState.putIntegerArrayList("potensList",potensList);
        outState.putStringArrayList("variableNameList", variableNameList);
        outState.putStringArrayList("offsetList",offsetList);
    }

    /*
        Used when the activity is reset to save the fragments state in the activity (not in fragment)
        Should basically be same as onSaveInstanceState.
     */
    public Bundle getFragmentState(){
        Bundle outState = new Bundle();
        factorTextList = new ArrayList<String>();
        potensList = new ArrayList<Integer>();
        offsetList = new ArrayList<String>();
        buttonBundle = new Bundle();
        if(tuneBarArrayList!=null){
            for(TuneBar tuneBar : tuneBarArrayList){
                Bundle tuneBarBundle = tuneBar.getTuneBarState();
                factorTextList.add(tuneBarBundle.getString("factor_text"));
                potensList.add(tuneBarBundle.getInt("selected_potens"));
                offsetList.add(tuneBarBundle.getString("offset"));
            }
        }

        buttonBundle = modeStateLayout.getButtonBundle();

        outState.putBundle("buttonBundle",buttonBundle);
        outState.putStringArrayList("factorTextList",factorTextList);
        outState.putIntegerArrayList("potensList",potensList);
        outState.putStringArrayList("variableNameList", variableNameList);
        outState.putStringArrayList("offsetList",offsetList);

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
        global_factor = 100.0/Double.parseDouble(getString(R.string.p_max_value));
        if(getArguments() != null){
            Bundle bundle = getArguments();
            bluetoothService = (BluetoothService) bundle.getSerializable(BLUETOOTHSERVICE_KEY);
        }
    }


    /*
        Runs once every time the view is created.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.calibration_fragment,container,false);
        created = false;

        addButton = root.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"ADDBUTTON");
                PopupMenu popup = new PopupMenu(getActivity(),addButton);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.id_add_modestate:
                                createModeStateView();
                                return true;
                            case R.id.id_add_tunebar:
                                createTuneBarView();
                                return true;
                        }
                        return false;
                    }
                });
                MenuInflater inflater1 = popup.getMenuInflater();
                inflater1.inflate(R.menu.menu_calibration_add,popup.getMenu());
                popup.show();
            }
        });

        //Recreate the saved state or initialize to standard state.
        if (savedInstanceState != null){
            factorTextList = savedInstanceState.getStringArrayList("factorTextList");
            potensList = savedInstanceState.getIntegerArrayList("potensList");
            variableNameList = savedInstanceState.getStringArrayList("variableNameList");
            offsetList = savedInstanceState.getStringArrayList("offsetList");

            buttonBundle = savedInstanceState.getBundle("buttonBundle");

        }

        modeStateLayout = new ModeStateLayout(buttonBundle,(RelativeLayout)root.findViewById(R.id.id_mainmode_button_layout));

        lastLayoutAddedId = R.id.id_main_calibration_text;
        tuneBarArrayList = new ArrayList<TuneBar>();
        if(factorTextList == null){
            factorTextList = new ArrayList<String>(Arrays.asList(init_val,init_val,init_val));
            potensList = new ArrayList<Integer>(Arrays.asList(init_potens,init_potens,init_potens));
            offsetList = new ArrayList<String>(Arrays.asList(init_offset,init_offset,init_offset));
            variableNameList = new ArrayList<String>(Arrays.asList("P","I","D"));
        }

        return root;

    }

    @Override
    public void onStart() {
        super.onStart();
        if(!created){
            RelativeLayout mainLayout = getActivity().findViewById(R.id.fragment_calibration);
            layout_offset = mainLayout.getChildCount();
            created = true;
            createAllTuneBarsFromLists();
        }
    }

    @Override
    public void onPause() {
        getFragmentState();
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_calibration, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings_reset:
                reset();
                return true;
            case R.id.settings_transmit_variables:
                updateTransactionRules(true);
                return true;
            case R.id.settings_calibration_save_layout:
                SaveLayoutDialog saveLayoutDialog = SaveLayoutDialog.newInstance("CalibrationFragment");
                saveLayoutDialog.setTargetFragment(this,0);
                saveLayoutDialog.show(getActivity().getSupportFragmentManager(),"saveLayout");
                return true;
            case R.id.settings_calibration_load_layout:
                //loadLayout("tst");
                LoadLayoutDialog loadLayoutDialog = LoadLayoutDialog.newInstance("CalibrationFragment");
                loadLayoutDialog.setTargetFragment(this,0);
                loadLayoutDialog.show(getActivity().getSupportFragmentManager(),"loadLayout");
                return true;
            default:
                break;
        }
        return false;
    }

    private void createTuneBarView(){
        AddTunebarDialog addTunebarDialog = new AddTunebarDialog();
        addTunebarDialog.setTargetFragment(this,0);
        addTunebarDialog.show(getActivity().getSupportFragmentManager(),"dialog");
    }

    private void createModeStateView(){
        AddModeStateDialog addModeStateDialog = new AddModeStateDialog();
        addModeStateDialog.setTargetFragment(this,0);
        addModeStateDialog.show(getActivity().getSupportFragmentManager(),"addModeStatedialog");
    }

    private void reset(){
        Log.d(TAG,"Attempting to reset view");

        eraseAllTuneBars();
        variableNameList.clear();
        String[] init = {"P","I","D"};
        for(String variableName : init){
            applyNewTunebar(variableName);
        }
        modeStateLayout.reset();
        modeStateLayout.redrawButtonLayout((MainActivity)getActivity());
    }

    private void removeTuneBar(String variableName){
        RelativeLayout mainLayout = getActivity().findViewById(R.id.fragment_calibration);
        int idx = variableNameList.indexOf(variableName);
        if(lastLayoutAddedId == tuneBarArrayList.get(idx).getLayoutId()){
            if(tuneBarArrayList.size()>1){
                lastLayoutAddedId = tuneBarArrayList.get(idx-1).getLayoutId();
            }else{
                lastLayoutAddedId = R.id.id_main_calibration_text;
            }
        }else{
            //Got to change the layout reference placement of next layout.
            RelativeLayout nextTuneBarLayout = getActivity().findViewById(tuneBarArrayList.get(idx+1).getLayoutId());
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)nextTuneBarLayout.getLayoutParams();
            if(idx == 0){
                rlp.addRule(RelativeLayout.BELOW,R.id.id_main_calibration_text);
            }else{
                rlp.addRule(RelativeLayout.BELOW,tuneBarArrayList.get(idx-1).getLayoutId());
            }

            nextTuneBarLayout.setLayoutParams(rlp);
        }
        mainLayout.removeViewAt(idx+layout_offset);
        tuneBarArrayList.remove(idx);
        variableNameList.remove(idx);

        updateButtonLocation();
        updateTransactionRules(true);
    }

    private void redrawTunebarLayout(){
        eraseAllTuneBars();
        createAllTuneBarsFromLists();
    }

    private void eraseAllTuneBars(){
        //Start with making sure we reset the values by sending zeros according to the transaction rules.
        for(TuneBar tuneBar : tuneBarArrayList){
            bluetoothService.transmitCalibrationData(String.valueOf(tuneBar.getIdentifier()),"0.00");
        }
        RelativeLayout mainLayout = getActivity().findViewById(R.id.fragment_calibration);
        int idx_last_added_view = mainLayout.getChildCount()-1;
        for(int i = idx_last_added_view; i>=layout_offset; i--){
            mainLayout.removeViewAt(i);
        }
        lastLayoutAddedId = R.id.id_main_calibration_text;
        tuneBarArrayList.clear();
    }

    private void createAllTuneBarsFromLists(){
        for(int i = 0; i < factorTextList.size();i++){
            TuneBar tuneBar = createNewTunebarLayout(variableNameList.get(i),false);
            tuneBar.setDropdown(potensList.get(i));
            tuneBar.setFactor(factorTextList.get(i));
            tuneBar.setOffset(offsetList.get(i));
        }
        updateTransactionRules(true);
        //redrawButtonLayout();
        modeStateLayout.redrawButtonLayout((MainActivity)getActivity());
    }

    /*
        Method that is run when the user have selected a variable name to add as a tunebar.
     */
    @Override
    public void applyNewTunebar(String variable_name) {
        variableNameList.add(variable_name);
        createNewTunebarLayout(variable_name,true);
    }

    /*
        Method that is run when the user have selected a mainstate/mode and a button
     */

    @Override
    public void applyNewButton(MainActivity.ModeState modeState, char identifier, String name, AddModeStateDialog.MainStateModeButton button){
        modeStateLayout.applyNewButton((MainActivity)getActivity(),modeState,identifier,name,button);
    }


    public TuneBar createNewTunebarLayout(String variableName, boolean transmit){
        TuneBarLayout tuneBarLayout = new TuneBarLayout(getActivity(),lastLayoutAddedId,variableName);
        lastLayoutAddedId = tuneBarLayout.getLayoutId();
        RelativeLayout mainLayout = getActivity().findViewById(R.id.fragment_calibration);
        mainLayout.addView(tuneBarLayout.getLayout());

        TuneBar vTuneBar = new TuneBar(tuneBarLayout.getDropdownId(),tuneBarLayout.getOffsetEditTextId(),tuneBarLayout.getFactorEditTextId(),tuneBarLayout.getFinalEditTextId(),tuneBarLayout.getButtonId(),tuneBarLayout.getSeekbarId(),tuneBarLayout.getLayoutId(),tuneBarLayout.getVariableName());

        tuneBarArrayList.add(vTuneBar);
        //Make sure to update the transaction rules before setting values as we set the identifier there.
        updateTransactionRules(transmit);

        vTuneBar.onCreateView();
        vTuneBar.setDropdown(init_potens);
        vTuneBar.setFactor(init_val);
        vTuneBar.setOffset(init_offset);

        updateButtonLocation();
        return vTuneBar;
    }

    public void updateButtonLocation(){
        RelativeLayout buttonLayout = getActivity().findViewById(R.id.id_linear_button_layout);
        RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams) buttonLayout.getLayoutParams();
        buttonParams.addRule(RelativeLayout.BELOW,lastLayoutAddedId);
        Resources r = getActivity().getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                50,//50dp
                r.getDisplayMetrics()
        );
        buttonParams.setMargins(0,px,0,0);
        buttonLayout.setLayoutParams(buttonParams);
    }

    private void updateTransactionRules(boolean transmit){
        String transactionRule = "";
        int identifier = 0;
        for(TuneBar tuneBar : tuneBarArrayList){
            tuneBar.setIdentifier(identifier);
            transactionRule = transactionRule + tuneBar.getVariableName() + ",";
            identifier ++;
        }
        //Handle the no signal case:
        if(transactionRule.length()>0){
            transactionRule = transactionRule.substring(0,transactionRule.length()-1);
        }

        if(bluetoothService!=null && transmit){
            bluetoothService.transmitCalibrationTransactionRules(transactionRule);
        }
    }


    @Override
    public void saveLayout(String instanceName) {
        Activity activity = getActivity();

        SharedPreferences sharedPreferences = activity.getSharedPreferences(instanceName,activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Update all paramters by calling getFragmentState.
        getFragmentState();

        ArrayList<String> buttonIdentifierList = buttonBundle.getStringArrayList("buttonIdentifierList");
        ArrayList<String> buttonNameList = buttonBundle.getStringArrayList("buttonNameList");
        ArrayList<Integer> modeStateList = buttonBundle.getIntegerArrayList("modeStateList");
        ArrayList<Integer> buttonIconList = buttonBundle.getIntegerArrayList("buttonIconList");

        Gson gson = new Gson();
        String factorTextListJson = gson.toJson(factorTextList);
        String potensListJson = gson.toJson(potensList);
        String variableNameListJson= gson.toJson(variableNameList);
        String offsetListJson= gson.toJson(offsetList);
        String modeStateListJson = gson.toJson(modeStateList);
        String buttonIdentifierListJson= gson.toJson(buttonIdentifierList);
        String butonNameListJson= gson.toJson(buttonNameList);
        String buttonIconListJson= gson.toJson(buttonIconList);
        editor.putString("factorTextList",factorTextListJson);
        editor.putString("potensList",potensListJson);
        editor.putString("variableNameList",variableNameListJson);
        editor.putString("offsetList",offsetListJson);
        editor.putString("modeStateList",modeStateListJson);
        editor.putString("buttonIdentifierList",buttonIdentifierListJson);
        editor.putString("buttonNameList",butonNameListJson);
        editor.putString("buttonIconList",buttonIconListJson);
        editor.apply();
    }

    @Override
    public void loadLayout(String instanceToLoad){
        Activity activity = getActivity();
        SharedPreferences sharedPreferences = activity.getSharedPreferences(instanceToLoad,activity.MODE_PRIVATE);
        Gson gson = new Gson();

        String factorTextListJson = sharedPreferences.getString("factorTextList",null);
        String potensListJson = sharedPreferences.getString("potensList",null);
        String variableNameListJson= sharedPreferences.getString("variableNameList",null);
        String offsetListJson= sharedPreferences.getString("offsetList",null);
        String modeStateListJson = sharedPreferences.getString("modeStateList",null);
        String buttonIdentifierListJson= sharedPreferences.getString("buttonIdentifierList",null);
        String butonNameListJson= sharedPreferences.getString("buttonNameList",null);
        String buttonIconListJson= sharedPreferences.getString("buttonIconList",null);

        Type stringArrayListType = new TypeToken<ArrayList<String>>() {}.getType();
        Type intArrayListType = new TypeToken<ArrayList<Integer>>() {}.getType();

        factorTextList = gson.fromJson(factorTextListJson,stringArrayListType);
        potensList = gson.fromJson(potensListJson,intArrayListType);
        variableNameList = gson.fromJson(variableNameListJson,stringArrayListType);
        offsetList = gson.fromJson(offsetListJson,stringArrayListType);

        buttonBundle = new Bundle();

        ArrayList<String> buttonIdentifierList = gson.fromJson(buttonIdentifierListJson,stringArrayListType);
        ArrayList<String> buttonNameList = gson.fromJson(butonNameListJson,stringArrayListType);
        ArrayList<Integer> modeStateList = gson.fromJson(modeStateListJson,intArrayListType);
        ArrayList<Integer> buttonIconList = gson.fromJson(buttonIconListJson,intArrayListType);
        buttonBundle.putIntegerArrayList("modeStateList",modeStateList);
        buttonBundle.putStringArrayList("buttonIdentifierList",buttonIdentifierList);
        buttonBundle.putStringArrayList("buttonNameList",buttonNameList);
        buttonBundle.putIntegerArrayList("buttonIconList",buttonIconList);
        modeStateLayout.setButtonBundle(buttonBundle);
        modeStateLayout.redrawButtonLayout((MainActivity)getActivity());

        redrawTunebarLayout();
        updateTransactionRules(true);

    }

    public class TuneBar{
        //Unicode characers
        private final static String POWERONE = "\u00B9";
        private final static String POWERTWO = "\u00B2";
        private final static String POWERTHREE = "\u00B3";
        private final static String POWERFOUR = "\u2074";
        private final static String POWERZERO = "\u2070";
        private final static String POWERMINUS = "\u207B";



        //Create the dropdown menu objects
        private Spinner dropdown;
        private String[] items_dropdown;
        private ArrayAdapter<String> adapter;


        //Create the edit text objects
        private EditText offsetEditText;
        private EditText factorEditText;
        private EditText finalEditText;

        //Create the seekbar object
        private SeekBar seekBar;
        private int seekbar_progress = 0;

        //Initialize the potens
        private double potens = 1e-2;
        private String format = "%.2f";

        // simulated binary semaphore. Used for protecting updating parameter state, to avoid loops when updating
        private boolean bin_semaphore = true;

        private String factor_text = init_val;
        private int selected_potens = init_potens;
        private float offset;

        private int dropdownId;
        private int offsetId;
        private int factorId;
        private int finalId;
        private int buttonId;
        private int seekbarId;
        private int layoutId;

        private String variableName;
        private int identifier;

        TuneBar(int dropdownId,int offsetId,int factorId,int finalId,int buttonId,int seekbarId,int layoutId, String variableName){
            this.dropdownId = dropdownId;
            this.offsetId = offsetId;
            this.factorId = factorId;
            this.finalId = finalId;
            this.seekbarId = seekbarId;
            this.variableName = variableName;
            this.layoutId = layoutId;
            this.buttonId = buttonId;
        }




        public void onCreateView(){
            //Initialize the dropdown menu.
            dropdown = root.findViewById(dropdownId);
            //To adjust the height of the dropdown menu:
            try{
                Field popup = Spinner.class.getDeclaredField("mPopup");
                popup.setAccessible(true);
                android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(dropdown);
                popupWindow.setHeight(500);
            }catch (NoClassDefFoundError|ClassCastException|NoSuchFieldException|IllegalAccessException e){

            }

            items_dropdown = new String[]{
                    "x10"+ POWERMINUS + POWERFOUR,
                    "x10"+ POWERMINUS + POWERTHREE,
                    "x10"+ POWERMINUS + POWERTWO,
                    "x10"+ POWERMINUS + POWERONE,
                    "x10"+ POWERZERO,
                    "x10"+ POWERONE,
                    "x10"+ POWERTWO,
                    "x10"+ POWERTHREE,
                    "x10"+ POWERFOUR};
            adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_dropdown_item, items_dropdown);
            dropdown.setAdapter(adapter);
            dropdown.setOnItemSelectedListener(new CustomOnItemSelectedListener());

            //Initialize the edit texts
            offsetEditText = (EditText)root.findViewById(offsetId);
            factorEditText = (EditText)root.findViewById(factorId);
            finalEditText = (EditText)root.findViewById(finalId);

            //Initialize the button
            ImageButton imageButton = root.findViewById(buttonId);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"removing " + variableName);
                    removeTuneBar(variableName);
                }
            });

            //Initialize the seekbar
            seekBar = (SeekBar) root.findViewById(seekbarId);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    seekbar_progress = progress;
                    if(bin_semaphore){
                        bin_semaphore = false;
                        updateFactor(sourceEnum.SEEKBAR);
                        updateFinal(sourceEnum.SEEKBAR);
                        bin_semaphore = true;
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    seekbar_progress = seekBar.getProgress();
                    if(bin_semaphore){
                        bin_semaphore = false;
                        updateFactor(sourceEnum.SEEKBAR);
                        updateFinal(sourceEnum.SEEKBAR);
                        bin_semaphore = true;
                    }
                }
            });

            //initialize the edit text with listeners when the user writes something.
            offsetEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try{
                        offset = Float.parseFloat(offsetEditText.getText().toString());
                    }catch (NumberFormatException nfe){
                        return;
                    }
                    //Update the seekbar to update the others, as that is the only one preserving accuracy.
                    seekBar.setProgress(seekbar_progress +1);
                    seekBar.setProgress(seekbar_progress -1);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            factorEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(bin_semaphore){
                        bin_semaphore = false;
                        updateSeekbar(sourceEnum.FACTOR);
                        updateFinal(sourceEnum.FACTOR);
                        bin_semaphore = true;
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            finalEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(bin_semaphore){
                        bin_semaphore = false;
                        updateFactor(sourceEnum.FINAL);
                        updateSeekbar(sourceEnum.FINAL);
                        updateFinal(sourceEnum.FINAL);
                        bin_semaphore = true;
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        public void setDropdown(int potens){
            dropdown.setSelection(potens);
        }

        public void setFactor(String factor){
            factorEditText.setText(factor);
        }

        public void setOffset(String offset){
            offsetEditText.setText(offset);
        }

        public void setIdentifier(int identifier){
            this.identifier = identifier;
        }

        /*
           updates the edit text with the final p_value according to the other parameters.
        */
        private void updateFinal(sourceEnum source){
            switch (source){
                case FACTOR:
                    double p_val;
                    try{
                        p_val = offset + (Double.parseDouble(factorEditText.getText().toString())* potens);
                    }catch (NumberFormatException nfe){
                        return;
                    }
                    finalEditText.setText(String.format(Locale.ROOT,format,p_val));
                    break;
                case SEEKBAR:
                    finalEditText.setText(String.format(Locale.ROOT,format, offset + (global_factor * seekbar_progress * potens)));
                    break;
                case FINAL:
                    //This case is added in order to write BT value (given its a number)
                    try{
                        //Check if written text is a number:
                        Double.parseDouble(finalEditText.getText().toString());
                        //write the value:
                        if(bluetoothService!=null){
                            //bluetoothService.write((variableName+"="+finalEditText.getText().toString()+"e").getBytes());
                            bluetoothService.transmitCalibrationData(String.valueOf(identifier),finalEditText.getText().toString());
                        }
                        return;
                    }catch (NumberFormatException nfe){
                        return;
                    }
            }
            if(bluetoothService!=null){
                //bluetoothService.write((variableName+"="+finalEditText.getText().toString()+"e").getBytes());
                bluetoothService.transmitCalibrationData(String.valueOf(identifier),finalEditText.getText().toString());
            }
        }

        /*
            updates the edit text with the current potens according to the other parameters.
         */
        private void updateFactor(sourceEnum source){
            switch (source){
                case FINAL:
                    double p_val;
                    try{
                        p_val = (Double.parseDouble(finalEditText.getText().toString())-offset)/ potens;
                    }catch (NumberFormatException nfe){
                        return;
                    }
                    factor_text = String.format(Locale.ROOT,"%.2f",p_val);
                    factorEditText.setText(factor_text);
                    break;
                case SEEKBAR:
                    factor_text = String.format(Locale.ROOT,"%.2f", global_factor * seekbar_progress);
                    factorEditText.setText(factor_text);
                    break;
            }
        }

        /*
            updates the seekbar according to the other parameters.
         */
        private void updateSeekbar(sourceEnum source){
            int val;
            switch (source){
                case FINAL:
                    try{
                        val = (int)((Double.parseDouble(finalEditText.getText().toString())-offset)/ (global_factor * potens));
                    }catch (NumberFormatException nfe){
                        return;
                    }
                    seekBar.setProgress(val);
                    break;
                case FACTOR:
                    try{
                        val = (int)(Double.parseDouble(factorEditText.getText().toString())/ global_factor);
                    }catch (NumberFormatException nfe){
                        return;
                    }
                    seekBar.setProgress(val);
                    break;
            }
        }

        /*
            The listener for the dropdown menu
         */
        public class CustomOnItemSelectedListener implements OnItemSelectedListener {

            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                switch ((String)parent.getItemAtPosition(pos)){
                    case "x10"+ POWERMINUS + POWERFOUR:
                        potens = 1e-4;
                        format = "%.6f";
                        break;
                    case "x10"+ POWERMINUS + POWERTHREE:
                        potens = 1e-3;
                        format = "%.5f";
                        break;
                    case "x10"+ POWERMINUS + POWERTWO:
                        potens = 1e-2;
                        format = "%.4f";
                        break;
                    case "x10"+ POWERMINUS + POWERONE:
                        potens = 1e-1;
                        format = "%.3f";
                        break;
                    case "x10"+ POWERZERO:
                        potens = 1e0;
                        format = "%.2f";
                        break;
                    case "x10"+ POWERONE:
                        potens = 1e1;
                        format = "%.1f";
                        break;
                    case "x10"+ POWERTWO:
                        potens = 1e2;
                        format = "%.0f";
                        break;
                    case "x10"+ POWERTHREE:
                        potens = 1e3;
                        format = "%.0f";
                        break;
                    default:
                        potens = 1e4;
                        format = "%.0f";
                        break;
                }
                selected_potens = pos;
                //Update the seekbar to update the others, as that is the only one preserving accuracy.
                seekBar.setProgress(seekbar_progress +1);
                seekBar.setProgress(seekbar_progress -1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                //Nothing
            }

        }

        public Bundle getTuneBarState(){
            Bundle outState = new Bundle();
            outState.putInt("selected_potens",selected_potens);
            outState.putString("factor_text",factor_text);
            String offset_text = offsetEditText.getText().toString();
            //Make sure its a valid input:
            try{
                offset = Float.parseFloat(offset_text);
                //offset_text is a valid number.
            }catch (NumberFormatException nfe){
                offset_text = String.valueOf(offset);
            }
            outState.putString("offset",offset_text);

            return outState;
        }

        public int getLayoutId(){
            return layoutId;
        }

        public String getVariableName(){
            return variableName;
        }

        public int getIdentifier(){
            return identifier;
        }

    }



}

