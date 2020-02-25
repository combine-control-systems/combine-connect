package CombineConnect.Combine;
/*
Class that creates the layout of the Tunebars.
 */
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class TuneBarLayout {
    private RelativeLayout relativeLayout;

    private int layoutId;
    private int id_text;
    private int id_offset_edit_text;
    private int id_text_plus;
    private int id_factor_edit_text;
    private int id_spinner;
    private int id_text_equal;
    private int id_final_edit_text;
    private int id_button;
    private int id_seekbar;
    private String variableName;

    TuneBarLayout(Activity activity,int LayoutBelow,String variableName){
        this.variableName = variableName;
        //----------Create Main Relative Layout view for the tunebar:-------------------------
        relativeLayout = new RelativeLayout(activity);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        rlp.addRule(RelativeLayout.BELOW,LayoutBelow);
        //Convert px to dp
        Resources r = activity.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                50,//50dp
                r.getDisplayMetrics()
        );
        rlp.setMargins(0,px,0,0);
        layoutId = View.generateViewId();
        relativeLayout.setLayoutParams(rlp);
        relativeLayout.setId(layoutId);

        //----------Create Text view for the tunebar:-------------------------
        TextView textView = new TextView(activity);
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        int px_left = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,//10dp
                r.getDisplayMetrics()
        );
        int px_top = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                11,//11dp
                r.getDisplayMetrics()
        );
        tvParams.setMargins(px_left,px_top,0,0);

        textView.setLayoutParams(tvParams);
        String text = variableName + " = ";
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,activity.getResources().getDimension(R.dimen.tune_text_size));
        textView.setTextColor(activity.getResources().getColor(R.color.colorAccent));
        id_text = View.generateViewId();
        textView.setId(id_text);

        relativeLayout.addView(textView);

        //----------Create OFFSET Edit Text view for the tunebar:-------------------------
        EditText offsetEditText = new EditText(activity);
        RelativeLayout.LayoutParams offsetEditTextParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        offsetEditTextParams.addRule(RelativeLayout.RIGHT_OF,id_text);
        offsetEditTextParams.addRule(RelativeLayout.END_OF,id_text);
        offsetEditTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        offsetEditText.setLayoutParams(offsetEditTextParams);
        offsetEditText.setTextAlignment(EditText.TEXT_ALIGNMENT_CENTER);
        offsetEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,activity.getResources().getDimension(R.dimen.tune_text_size));
        offsetEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        id_offset_edit_text = View.generateViewId();
        offsetEditText.setId(id_offset_edit_text);

        relativeLayout.addView(offsetEditText);

        //----------Create Textview (plus sign) for the tunebar:-------------------------
        TextView textViewPlus = new TextView(activity);
        RelativeLayout.LayoutParams tvPlusParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        tvPlusParams.addRule(RelativeLayout.RIGHT_OF,id_offset_edit_text);
        tvPlusParams.addRule(RelativeLayout.END_OF,id_offset_edit_text);
        tvPlusParams.addRule(RelativeLayout.ALIGN_TOP,id_text);
        tvPlusParams.addRule(RelativeLayout.ALIGN_BOTTOM,id_text);
        textViewPlus.setLayoutParams(tvPlusParams);
        String text_plus = " + ";
        textViewPlus.setText(text_plus);
        textViewPlus.setTextSize(TypedValue.COMPLEX_UNIT_PX,activity.getResources().getDimension(R.dimen.tune_text_size));
        textViewPlus.setTextColor(activity.getResources().getColor(R.color.colorAccent));
        id_text_plus = View.generateViewId();
        textViewPlus.setId(id_text_plus);

        relativeLayout.addView(textViewPlus);

        //----------Create Edit Text view for the tunebar:-------------------------
        EditText factorEditText = new EditText(activity);
        RelativeLayout.LayoutParams factorEditTextParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        factorEditTextParams.addRule(RelativeLayout.RIGHT_OF,id_text_plus);
        factorEditTextParams.addRule(RelativeLayout.END_OF,id_text_plus);
        factorEditTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        factorEditText.setLayoutParams(factorEditTextParams);
        factorEditText.setTextAlignment(EditText.TEXT_ALIGNMENT_CENTER);
        factorEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,activity.getResources().getDimension(R.dimen.tune_text_size));
        factorEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        id_factor_edit_text = View.generateViewId();
        factorEditText.setId(id_factor_edit_text);

        relativeLayout.addView(factorEditText);

        //----------Create Spinner view for the tunebar:-------------------------
        Spinner spinner = new Spinner(activity);
        RelativeLayout.LayoutParams spinnerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.addRule(RelativeLayout.RIGHT_OF,id_factor_edit_text);
        spinnerParams.addRule(RelativeLayout.END_OF,id_factor_edit_text);
        spinnerParams.addRule(RelativeLayout.ALIGN_TOP,id_factor_edit_text);
        spinnerParams.addRule(RelativeLayout.ALIGN_BOTTOM,id_factor_edit_text);
        spinner.setLayoutParams(spinnerParams);

        id_spinner = View.generateViewId();
        spinner.setId(id_spinner);

        relativeLayout.addView(spinner);

        //----------Create Textview (equal sign) for the tunebar:-------------------------
        TextView textViewEqual = new TextView(activity);
        RelativeLayout.LayoutParams tvEqualParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        tvEqualParams.addRule(RelativeLayout.RIGHT_OF,id_spinner);
        tvEqualParams.addRule(RelativeLayout.END_OF,id_spinner);
        tvEqualParams.addRule(RelativeLayout.ALIGN_TOP,id_text);
        tvEqualParams.addRule(RelativeLayout.ALIGN_BOTTOM,id_text);
        textViewEqual.setLayoutParams(tvEqualParams);
        String text_equal = " = ";
        textViewEqual.setText(text_equal);
        textViewEqual.setTextSize(TypedValue.COMPLEX_UNIT_PX,activity.getResources().getDimension(R.dimen.tune_text_size));
        textViewEqual.setTextColor(activity.getResources().getColor(R.color.colorAccent));
        id_text_equal = View.generateViewId();
        textViewEqual.setId(id_text_equal);

        relativeLayout.addView(textViewEqual);

        //----------Create final Edit Text view for the tunebar:-------------------------
        EditText finalEditText = new EditText(activity);
        RelativeLayout.LayoutParams finalEditTextParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        finalEditTextParams.addRule(RelativeLayout.RIGHT_OF,id_text_equal);
        finalEditTextParams.addRule(RelativeLayout.END_OF,id_text_equal);
        finalEditTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        finalEditText.setLayoutParams(finalEditTextParams);
        finalEditText.setTextAlignment(EditText.TEXT_ALIGNMENT_CENTER);
        finalEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,activity.getResources().getDimension(R.dimen.tune_text_size));
        finalEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        id_final_edit_text = View.generateViewId();
        finalEditText.setId(id_final_edit_text);

        relativeLayout.addView(finalEditText);

        //----------Create Close Button view for the tunebar:-------------------------
        ImageButton imageButton = new ImageButton(activity);
        RelativeLayout.LayoutParams imageButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        imageButtonParams.addRule(RelativeLayout.ALIGN_BOTTOM,id_text);
        imageButtonParams.addRule(RelativeLayout.ALIGN_TOP,id_text);
        imageButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int px_right = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,//10dp
                r.getDisplayMetrics()
        );
        imageButtonParams.setMargins(0,0,px_right,0);
        imageButton.setLayoutParams(imageButtonParams);
        imageButton.setImageResource(R.drawable.ic_remove_black_24dp);
        imageButton.setBackgroundColor(Color.TRANSPARENT);
        id_button = View.generateViewId();
        imageButton.setId(id_button);

        relativeLayout.addView(imageButton);


        //----------Create seekbar view for the tunebar:-------------------------
        SeekBar seekBar = new SeekBar(activity);
        RelativeLayout.LayoutParams seekbarParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        seekbarParams.addRule(RelativeLayout.BELOW,id_factor_edit_text);
        seekBar.setLayoutParams(seekbarParams);
        int maxval = Integer.parseInt(activity.getResources().getString(R.string.p_max_value));
        seekBar.setMax(maxval);
        id_seekbar = View.generateViewId();
        seekBar.setId(id_seekbar);

        relativeLayout.addView(seekBar);
    }

    public RelativeLayout getLayout(){
        return relativeLayout;
    }

    public int getDropdownId(){
        return id_spinner;
    }

    public int getOffsetEditTextId(){
        return id_offset_edit_text;
    }

    public int getFinalEditTextId(){
        return id_final_edit_text;
    }

    public int getFactorEditTextId(){
        return id_factor_edit_text;
    }

    public int getButtonId(){
        return id_button;
    }

    public int getSeekbarId(){
        return id_seekbar;
    }

    public int getLayoutId(){
        return layoutId;
    }

    public String getVariableName(){
        return variableName;
    }

}
