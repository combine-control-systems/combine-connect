package CombineConnect.Combine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;

/*
    Class that show the popup window on create Modestate event.
 */
public class AddModeStateDialog extends AppCompatDialogFragment {

    private String TAG = "MainActivity_debug";

    //EditText for userinput
    private EditText editTextVariableName;
    private ImageButton runButton;
    private ImageButton stopButton;
    private ImageButton modeButton;

    private RadioGroup mainStateModeGroup;
    private RadioGroup mainStateGroup;
    private RadioGroup modeGroup;

    private CheckBox runCheckBox;
    private CheckBox stopCheckBox;
    private CheckBox modeCheckBox;

    private ArrayList<MainState> mainStateArrayList;
    private ArrayList<Mode> modeArrayList;
    private ArrayList<RadioButton> mainStateRadioButtonList;
    private ArrayList<RadioButton> modeRadioButtonList;

    private MainState selectedMainState;
    private Mode selectedMode;

    public enum MainStateModeButton{
        RUNBUTTON(0),
        STOPBUTTON(1),
        MODEBUTTON(2);

        private final int value;
        MainStateModeButton(int value){
            this.value = value;
        }
        public int getValue(){
            return value;
        }
    }

    private MainActivity.ModeState modeStateChosen;

    private MainStateModeButton mainStateModeButton;

    private MainActivity activity;

    //interface between popup window and underlying fragment.
    private AddModeStateFragmentInterface addModeStateFragmentInterface;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create the interface. Note that the target fragment need to be set.
        try {
            addModeStateFragmentInterface= (AddModeStateFragmentInterface) getTargetFragment();
        } catch (Exception e) {
            throw new ClassCastException("Target Fragment must implement AddModeStateFragmentInterface");
        }
        // Create and inflate the popup window
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_add_mode_mainstate,null);

        activity = (MainActivity)getActivity();

        runCheckBox = view.findViewById(R.id.id_checkbox_run_button);
        runCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    stopCheckBox.setChecked(false);
                    modeCheckBox.setChecked(false);
                    mainStateModeButton = MainStateModeButton.RUNBUTTON;
                    runCheckBox.setClickable(false);
                    runButton.setClickable(false);
                }else{
                    runCheckBox.setClickable(true);
                    runButton.setClickable(true);
                }
            }
        });

        stopCheckBox = view.findViewById(R.id.id_checkbox_stop_button);
        stopCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    runCheckBox.setChecked(false);
                    modeCheckBox.setChecked(false);
                    mainStateModeButton = MainStateModeButton.STOPBUTTON;
                    stopCheckBox.setClickable(false);
                    stopButton.setClickable(false);
                }else{
                    stopCheckBox.setClickable(true);
                    stopButton.setClickable(true);
                }
            }
        });

        modeCheckBox = view.findViewById(R.id.id_checkbox_mode_button);
        modeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    runCheckBox.setChecked(false);
                    stopCheckBox.setChecked(false);
                    mainStateModeButton = MainStateModeButton.MODEBUTTON;
                    modeCheckBox.setClickable(false);
                    modeButton.setClickable(false);
                }else{
                    modeCheckBox.setClickable(true);
                    modeButton.setClickable(true);
                }
            }
        });

        runButton = view.findViewById(R.id.id_calibration_run_button);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runCheckBox.setChecked(!runCheckBox.isChecked());
            }
        });

        stopButton = view.findViewById(R.id.id_calibration_stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCheckBox.setChecked(!stopCheckBox.isChecked());
            }
        });

        modeButton = view.findViewById(R.id.id_calibration_mode_button);
        modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modeCheckBox.setChecked(!modeCheckBox.isChecked());
            }
        });

        mainStateGroup = view.findViewById(R.id.id_addMainStateRadioGroup);
        mainStateGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int i = 0;
                for(RadioButton radioButton : mainStateRadioButtonList){
                    if(radioButton.getId() == checkedId){
                        selectedMainState = mainStateArrayList.get(i);
                        break;
                    }
                    i++;
                }
            }
        });
        modeGroup = view.findViewById(R.id.id_addModeRadioGroup);
        modeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int i = 0;
                for(RadioButton radioButton : modeRadioButtonList){
                    if(radioButton.getId() == checkedId){
                        selectedMode = modeArrayList.get(i);
                        break;
                    }
                    i++;
                }
            }
        });

        mainStateModeGroup = view.findViewById(R.id.id_addModeMainStateRadioGroup);
        mainStateModeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.id_main_state_radiobutton:
                        Log.d(TAG,"Main state radiobutton checked");
                        modeStateChosen = MainActivity.ModeState.MAINSTATE;
                        mainStateRadioButtonList = new ArrayList<RadioButton>();
                        mainStateArrayList = activity.getMainStateArrayList();
                        mainStateGroup.removeAllViews();
                        for(MainState mainState : mainStateArrayList){
                            //Add a radiobutton for each mainstate. Save each identifier to get the checked radiobutton.
                            RadioButton radioButton = new RadioButton(activity);
                            radioButton.setId(View.generateViewId());
                            radioButton.setText(mainState.getMainStateName());
                            mainStateRadioButtonList.add(radioButton);
                            mainStateGroup.addView(radioButton);
                            modeGroup.setVisibility(RadioGroup.INVISIBLE);
                            mainStateGroup.setVisibility(RadioGroup.VISIBLE);
                        }
                        break;
                    case R.id.id_mode_radiobutton:
                        Log.d(TAG,"Mode radiobutton checked");
                        modeStateChosen = MainActivity.ModeState.MODE;
                        modeRadioButtonList = new ArrayList<RadioButton>();
                        modeArrayList = activity.getModeArrayList();
                        modeGroup.removeAllViews();
                        for(Mode mode : modeArrayList){
                            RadioButton radioButton = new RadioButton(activity);
                            radioButton.setId(View.generateViewId());
                            radioButton.setText(mode.getModeName());
                            modeRadioButtonList.add(radioButton);
                            modeGroup.addView(radioButton);
                            modeGroup.setVisibility(RadioGroup.VISIBLE);
                            mainStateGroup.setVisibility(RadioGroup.INVISIBLE);
                        }
                        break;
                }
            }
        });


        builder.setView(view).setTitle("Add Button")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //see positivebutton implementation in onStart.
                        // Implemented there to be able to prevent close of menu in case of insufficient userinputs.
                    }
                })
                .setIcon(R.drawable.ic_combine_logo);
        // create the edit text object
        //editTextVariableName = view.findViewById(R.id.id_popup_tunebar_variablename);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog)getDialog();
        if(d!=null){
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        boolean dismissed = false;
                        if(modeStateChosen!=null){
                            switch (modeStateChosen){
                                case MAINSTATE:
                                    if(selectedMainState!=null && mainStateModeButton!=null){
                                        addModeStateFragmentInterface.applyNewButton(MainActivity.ModeState.MAINSTATE,selectedMainState.getIdentifier(),selectedMainState.getMainStateName(),mainStateModeButton);
                                        dismiss();
                                        dismissed = true;
                                    }
                                    break;
                                case MODE:
                                    if(selectedMode!=null && mainStateModeButton!=null){
                                        addModeStateFragmentInterface.applyNewButton(MainActivity.ModeState.MODE,selectedMode.getIdentifier(),selectedMode.getModeName(),mainStateModeButton);
                                        dismiss();
                                        dismissed = true;
                                    }
                                    break;
                            }
                        }
                        if(!dismissed){
                            Toast.makeText(getActivity(), "Please select all options", Toast.LENGTH_SHORT).show();
                        }
                    }catch (NullPointerException e){
                        //Handles the case the user rotates the landscape of the phone in the dialog
                        Toast.makeText(getActivity(), "Couldn't add button", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
            });
        }
    }

    public interface AddModeStateFragmentInterface {
        void applyNewButton(MainActivity.ModeState modeState, char identifier, String name, AddModeStateDialog.MainStateModeButton button);
    }
}
