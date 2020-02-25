package CombineConnect.Combine;

/*
Layout that contains the buttons representing a mainstate or a mode.
This class should be instantiated in each fragment where a button layout is used.
 */

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class ModeStateLayout {

    private ArrayList<String> buttonIdentifierList;
    private ArrayList<String> buttonNameList;
    private ArrayList<Integer> modeStateList;
    private ArrayList<Integer> buttonIconList;

    private Bundle buttonBundle;

    private RelativeLayout relativeLayout;

    private int buttonCounter = 0;

    ModeStateLayout(Bundle buttonBundle, RelativeLayout relativeLayout){
        setButtonBundle(buttonBundle);
        this.relativeLayout = relativeLayout;
    }

    public Bundle getButtonBundle(){
        zipButtonBundle();
        return buttonBundle;
    }

    public void setButtonBundle(Bundle buttonBundle){
        this.buttonBundle = buttonBundle;
        unzipButtonBundle();
    }

    private void unzipButtonBundle(){
        if(buttonBundle != null){
            modeStateList = buttonBundle.getIntegerArrayList("modeStateList");
            buttonIdentifierList = buttonBundle.getStringArrayList("buttonIdentifierList");
            buttonNameList = buttonBundle.getStringArrayList("buttonNameList");
            buttonIconList = buttonBundle.getIntegerArrayList("buttonIconList");
        }
        else{
            reset();
        }
    }

    private void zipButtonBundle(){
        buttonBundle = new Bundle();
        buttonBundle.putIntegerArrayList("modeStateList",modeStateList);
        buttonBundle.putStringArrayList("buttonIdentifierList",buttonIdentifierList);
        buttonBundle.putStringArrayList("buttonNameList",buttonNameList);
        buttonBundle.putIntegerArrayList("buttonIconList",buttonIconList);
    }

    public void reset(){
        modeStateList = new ArrayList<Integer>();
        buttonIconList = new ArrayList<Integer>();
        buttonNameList= new ArrayList<String>();
        buttonIdentifierList= new ArrayList<String>();
    }

    public void applyNewButton(MainActivity mainActivity,MainActivity.ModeState modeState, char identifier, String name, AddModeStateDialog.MainStateModeButton button){
        //final MainActivity activity = (MainActivity)getActivity();
        modeStateList.add(modeState.getValue());
        buttonNameList.add(name);
        buttonIdentifierList.add(String.valueOf(identifier));
        buttonIconList.add(button.getValue());
        redrawButtonLayout(mainActivity);
    }

    public void redrawButtonLayout(MainActivity mainActivity ){
        final MainActivity activity = mainActivity;
        int len = modeStateList.size();
        //RelativeLayout relativeLayout = activity.findViewById(R.id.id_mainmode_button_layout);
        relativeLayout.removeAllViews();
        View helperview = new View(activity);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                0,
                0
        );
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        helperview.setLayoutParams(params);
        helperview.setId(View.generateViewId());
        relativeLayout.addView(helperview);

        Resources r = activity.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                50,//150dp
                r.getDisplayMetrics()
        );

        int below = 0;
        int alignId = 0;
        for(int  i = 0; i < len; i++){
            final ImageButton imageButton = new ImageButton(activity);
            int id = View.generateViewId();
            imageButton.setId(id);
            params = new RelativeLayout.LayoutParams(
                    (int)activity.getResources().getDimension(R.dimen.run_button_size_big),
                    (int)activity.getResources().getDimension(R.dimen.run_button_size_big)
            );
            if(i%2 == 0){
                params.addRule(RelativeLayout.LEFT_OF,helperview.getId());
                params.setMarginEnd(px);
                alignId = id;
            }else{
                params.addRule(RelativeLayout.RIGHT_OF,helperview.getId());
                params.setMarginStart(px);
                params.addRule(RelativeLayout.ALIGN_TOP,alignId);
                params.addRule(RelativeLayout.ALIGN_BOTTOM,alignId);
                below = id;
            }
            if(below != 0){
                params.addRule(RelativeLayout.BELOW,below);
            }
            imageButton.setLayoutParams(params);

            final int buttonIcon = buttonIconList.get(i);
            if(buttonIcon == AddModeStateDialog.MainStateModeButton.RUNBUTTON.getValue()){
                imageButton.setImageResource(R.drawable.ic_play_arrow_green_big);
            }
            else if(buttonIcon == AddModeStateDialog.MainStateModeButton.STOPBUTTON.getValue()){
                imageButton.setImageResource(R.drawable.ic_stop_red_big);
            }else{
                imageButton.setImageResource(R.drawable.ic_mode_big);
            }
            imageButton.setBackgroundColor(Color.TRANSPARENT);
            TypedValue outValue = new TypedValue();
            activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground,outValue,true);
            imageButton.setBackgroundResource(outValue.resourceId);
            int buttonModeState = modeStateList.get(i);
            final char buttonIdentifier = buttonIdentifierList.get(i).charAt(0);
            if(buttonModeState == MainActivity.ModeState.MAINSTATE.getValue()){
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int checked = activity.setMainState(buttonIdentifier);
                    }
                });
            }else{
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int checked = activity.setMode(buttonIdentifier);
                    }
                });
            }
            final int idx = i;
            imageButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(activity,imageButton);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.id_remove_modestate:
                                    modeStateList.remove(idx);
                                    buttonNameList.remove(idx);
                                    buttonIdentifierList.remove(idx);
                                    buttonIconList.remove(idx);
                                    redrawButtonLayout(activity);
                                    return true;
                                case R.id.id_get_variablename:
                                    Toast.makeText(activity, buttonNameList.get(idx), Toast.LENGTH_SHORT).show();
                                    return true;
                                case R.id.id_get_identifier:
                                    Toast.makeText(activity, buttonIdentifierList.get(idx), Toast.LENGTH_SHORT).show();
                                    return true;
                            }
                            return false;
                        }
                    });
                    MenuInflater inflater1 = popup.getMenuInflater();
                    inflater1.inflate(R.menu.menu_calibration_button,popup.getMenu());
                    popup.show();
                    return false;
                }
            });
            relativeLayout.addView(imageButton);
        }
    }

    public int getButtonCounter() {
        buttonCounter = 0;
        if(buttonIdentifierList != null){
            buttonCounter = buttonIdentifierList.size();
        }
        return buttonCounter;
    }
}
