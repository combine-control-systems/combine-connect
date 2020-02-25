package CombineConnect.Combine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;

/*
    Class that show the popup window on create Plot event.
 */
public class AddPlotDialog extends AppCompatDialogFragment {

    //interface between popup window and underlying fragment.
    private PlotFragmentInterface plotFragmentInterface;

    private ArrayList<String> variableNameOrder;
    private ArrayList<RadioButton> signalArray;

    private EditText editTextPlotName;

    private EditText editTextSamples;
    private CheckBox chk_autox;
    private CheckBox chk_autoy;
    private EditText editTextLimitLow;
    private EditText editTextLimitHigh;
    private CheckBox chk_xtitle;
    private CheckBox chk_ytitle;
    private CheckBox chk_xtick;
    private CheckBox chk_ytick;
    private CheckBox chk_legend;

    public static AddPlotDialog newInstance(ArrayList<String> variableNameOrder){
        AddPlotDialog addPlotDialog = new AddPlotDialog();

        Bundle args = new Bundle();
        args.putStringArrayList("variableNameOrder",variableNameOrder);
        addPlotDialog.setArguments(args);

        return addPlotDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.variableNameOrder = getArguments().getStringArrayList("variableNameOrder");

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create the interface. Note that the target fragment need to be set.
        try {
            plotFragmentInterface = (PlotFragmentInterface) getTargetFragment();
        } catch (Exception e) {
            throw new ClassCastException("Target Fragment must implement PlotFragmentInterface");
        }
        // Create and inflate the popup window
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_add_plot,null);

        RelativeLayout relativeLayout = view.findViewById(R.id.id_popup_plot_signals_layout);
        RadioGroup signalsGroup = new RadioGroup(getActivity());
        signalsGroup.setOrientation(RadioGroup.VERTICAL);
        int len = variableNameOrder.size();
        signalArray = new ArrayList<RadioButton>();
        for(int i = 0; i<len; i++){
            RadioButton signal = new RadioButton(getActivity());
            signal.setText(variableNameOrder.get(i));
            signalArray.add(signal);
            signalsGroup.addView(signalArray.get(i));
        }
        relativeLayout.addView(signalsGroup);

        builder.setView(view).setTitle("Create Custom Plot")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Things that should be included in options:
                        /*
                            -Title (String)
                            -Signals (String arrayList)
                            -#samples (int)(1000 as standard)
                            -Scale settings x(int)(auto = (-1) as standard)
                            -Scale settings y(int)(auto = (-1) as standard)
                            -title_x (false as standard)
                            -title_y (false as standard)
                            -tick_x (false as standard)
                            -tick_y (true as standard)
                            -legend (true as standard)
                        */
                        Bundle options = new Bundle();

                        String plot_title = "";

                        int samples = 1000;
                        try {
                            samples = Integer.parseInt(editTextSamples.getText().toString());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        float limit_high = 1000;
                        try {
                            limit_high = Float.parseFloat(editTextLimitHigh.getText().toString());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        float limit_low = 0;
                        try {
                            limit_low = Float.parseFloat(editTextLimitLow.getText().toString());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        for(int i = 0; i< signalArray.size(); i++){
                            options.putBoolean(variableNameOrder.get(i),signalArray.get(i).isChecked());
                            if(signalArray.get(i).isChecked()){
                                plot_title = variableNameOrder.get(i);
                            }
                        }

                        options.putString("title",plot_title);
                        options.putInt("samples",samples);
                        options.putBoolean("autox",chk_autox.isChecked());
                        options.putBoolean("autoy",chk_autoy.isChecked());
                        options.putFloat("limitLow",limit_low);
                        options.putFloat("limitHigh",limit_high);
                        options.putBoolean("xtitle",chk_xtitle.isChecked());
                        options.putBoolean("ytitle",chk_ytitle.isChecked());
                        options.putBoolean("xtick",chk_xtick.isChecked());
                        options.putBoolean("ytick",chk_ytick.isChecked());
                        options.putBoolean("legend",chk_legend.isChecked());


                        try {
                            plotFragmentInterface.applyNewPlot(options);
                        }catch (NullPointerException e){
                            //Handles the case rotates the phone when dialog is up
                            Toast.makeText(getActivity(), "Couldn't create plot", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setIcon(R.drawable.ic_combine_logo);


        editTextSamples = view.findViewById(R.id.id_popup_nbr_samples_edit_text);
        chk_autox = view.findViewById(R.id.popup_plot_checkbox_autox);
        chk_autoy = view.findViewById(R.id.popup_plot_checkbox_autoy);
        editTextLimitLow = view.findViewById(R.id.id_popup_plot_edittext_ymin);
        editTextLimitHigh = view.findViewById(R.id.id_popup_plot_edittext_ymax);
        chk_xtitle = view.findViewById(R.id.plot_checkbox_enable_x_title);
        chk_ytitle = view.findViewById(R.id.plot_checkbox_enable_y_title);
        chk_xtick = view.findViewById(R.id.plot_checkbox_enable_x_tick);
        chk_ytick = view.findViewById(R.id.plot_checkbox_enable_y_tick);
        chk_legend = view.findViewById(R.id.plot_checkbox_enable_legend);
        return builder.create();
    }

    public interface PlotFragmentInterface {
        void applyNewPlot(Bundle options);
    }
}
