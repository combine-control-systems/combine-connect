package CombineConnect.Combine;
/*
Class containing the data of the main states.
 */
import android.view.View;

import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;




public class MainState {

    private RadioButton radioButton;
    private char identifier;
    private String mainStateName;

    MainState(final MainActivity activity, final char identifier, String mainStateName){
        this.identifier = identifier;
        this.mainStateName = mainStateName;


        radioButton = new RadioButton(activity);
        RelativeLayout.LayoutParams radioParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        radioButton.setLayoutParams(radioParams);
        int radioButtonId = View.generateViewId();
        radioButton.setId(radioButtonId);
        radioButton.setText(mainStateName);
        RadioGroup radioGroup = activity.findViewById(R.id.main_states_group);
        radioGroup.addView(radioButton);
        radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    activity.onMainStateChange(identifier,isChecked);
                }
            }
        });

    }

    public RadioButton getRadioButton(){
        return radioButton;
    }

    public String getMainStateName(){
        return mainStateName;
    }

    public char getIdentifier(){
        return identifier;
    }

}
