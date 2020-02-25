package CombineConnect.Combine;
/*
Class containing the data of the modes.
 */
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;


public class Mode {

    private CheckBox checkBox;
    private char identifier;
    private String modeName;
    private RelativeLayout relativeLayout;
    private int checkBoxId;

    Mode(final MainActivity activity, final char identifier, String modeName, int layoutBelow){
        this.identifier = identifier;
        this.modeName = modeName;


        relativeLayout = activity.findViewById(R.id.id_modes);
        //----------Create check box view:-------------------------
        checkBox = new CheckBox(activity);
        RelativeLayout.LayoutParams checkboxParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        checkboxParams.addRule(RelativeLayout.BELOW,layoutBelow);
        checkBox.setLayoutParams(checkboxParams);
        checkBoxId = View.generateViewId();
        checkBox.setId(checkBoxId);
        checkBox.setText(modeName);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                activity.onModeChange(identifier,isChecked);
            }
        });
        relativeLayout.addView(checkBox);

    }
    public int getCheckBoxId(){
        return checkBoxId;
    }
    public CheckBox getCheckBox(){
        return checkBox;
    }

    public String getModeName(){
        return modeName;
    }

    public char getIdentifier(){
        return identifier;
    }
}
