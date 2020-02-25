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
    Class that show the popup window on create joystick event.
 */
public class JoystickSettingsDialog extends AppCompatDialogFragment {

    private String TAG = "MainActivity_debug";

    //EditText for userinput
    private EditText maxAngleEditText;
    private EditText powerEditText;
    private RadioGroup radioGroup;

    private MainActivity activity;

    //interface between popup window and underlying fragment.
    private JoystickSettingsInterface joystickSettingsInterface;
    private int angleQuadrantDefiniton;
    private float maxAngle;
    private float powerFactor;

    public static JoystickSettingsDialog newInstance(Bundle bundle){
        JoystickSettingsDialog joystickSettingsDialog = new JoystickSettingsDialog();

        Bundle args = new Bundle();
        args.putBundle("Bundle",bundle);
        joystickSettingsDialog.setArguments(args);

        return joystickSettingsDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments().getBundle("Bundle");
        maxAngle = bundle.getFloat("maxAngle");
        powerFactor = bundle.getFloat("powerFactor");
        angleQuadrantDefiniton = bundle.getInt("angleQuadrantDefinition");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create the interface. Note that the target fragment need to be set.
        try {
            joystickSettingsInterface= (JoystickSettingsInterface) getTargetFragment();
        } catch (Exception e) {
            throw new ClassCastException("Target Fragment must implement JoystickSettingsInterface");
        }
        // Create and inflate the popup window
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_joystick_settings_dialog,null);

        activity = (MainActivity)getActivity();

        maxAngleEditText = view.findViewById(R.id.id_joysticksettings_angle_edittext);
        maxAngleEditText.setText(String.valueOf(maxAngle));
        powerEditText = view.findViewById(R.id.id_joysticksettings_power_edittext);
        powerEditText.setText(String.valueOf(powerFactor));
        radioGroup = view.findViewById(R.id.id_joysticksettings_angle_radiogroup);
        switch (angleQuadrantDefiniton){
            case 1:
                radioGroup.check(R.id.id_joysticksettings_angle_radiobutton_one);
                break;
            case 2:
                radioGroup.check(R.id.id_joysticksettings_angle_radiobutton_two);
                break;
            case 4:
                radioGroup.check(R.id.id_joysticksettings_angle_radiobutton_four);
                break;
        }

        builder.setView(view).setTitle("Joystick Settings")
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

                        try{
                            maxAngle = Float.parseFloat(maxAngleEditText.getText().toString());
                            powerFactor = Float.parseFloat(powerEditText.getText().toString());
                            switch (radioGroup.getCheckedRadioButtonId()){
                                case R.id.id_joysticksettings_angle_radiobutton_one:
                                    angleQuadrantDefiniton = 1;
                                    break;
                                case R.id.id_joysticksettings_angle_radiobutton_two:
                                    angleQuadrantDefiniton = 2;
                                    break;
                                default:
                                    angleQuadrantDefiniton = 4;
                                    break;
                            }
                            joystickSettingsInterface.applyJoystickSettings(maxAngle,powerFactor,angleQuadrantDefiniton);
                            dismiss();
                            dismissed = true;
                            //input is a valid number.
                        }catch (NumberFormatException nfe){
                            //do nothing, dont allow to dismiss.
                        }

                        if(!dismissed){
                            Toast.makeText(getActivity(), "Please type a valid number", Toast.LENGTH_SHORT).show();
                        }
                    }catch (NullPointerException e){
                        //Handles the case the user rotates the landscape of the phone in the dialog
                        Toast.makeText(getActivity(), "Couldn't set settings", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
            });
        }
    }

    public interface JoystickSettingsInterface {
        void applyJoystickSettings(float maxAngle,float powerOffset,int angleQuadrantDefinition);
    }
}
