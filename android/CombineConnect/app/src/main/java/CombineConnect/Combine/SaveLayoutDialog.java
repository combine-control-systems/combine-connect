package CombineConnect.Combine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

/*
    Class that show the popup window for save layout event.
 */
public class SaveLayoutDialog extends AppCompatDialogFragment {

    //EditText for userinput
    private EditText editTextVariableName;
    //interface between popup window and underlying fragment.
    private SaveLayoutInterface saveLayoutInterface;
    private String source;

    public static SaveLayoutDialog newInstance(String source){
        SaveLayoutDialog saveLayoutDialog = new SaveLayoutDialog();

        Bundle args = new Bundle();
        args.putString("source",source);
        saveLayoutDialog.setArguments(args);

        return saveLayoutDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        this.source= getArguments().getString("source");
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create the interface. Note that the target fragment need to be set.
        try {
            saveLayoutInterface = (SaveLayoutInterface) getTargetFragment();
        } catch (Exception e) {
            throw new ClassCastException("Target Fragment must implement SaveLayoutInterface");
        }
        // Create and inflate the popup window
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_save_layout,null);

        builder.setView(view).setTitle("Save Layout")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Overwritten below.
                    }
                })
                .setIcon(R.drawable.ic_combine_logo);
        // create the edit text object
        editTextVariableName = view.findViewById(R.id.id_popup_save_layout);
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
                        String instanceName = editTextVariableName.getText().toString();
                        String errorMessage = "";
                        if(!instanceName.contains(",")){
                            instanceName = source + "," + instanceName;
                            SharedPreferences sharedPreferencesNames = getActivity().getSharedPreferences("sharedPreferencesNames",getActivity().MODE_PRIVATE);
                            SharedPreferences.Editor namesEditor = sharedPreferencesNames.edit();
                            namesEditor.putString(instanceName,instanceName);
                            namesEditor.apply();
                            saveLayoutInterface.saveLayout(instanceName);
                            dismiss();
                            dismissed = true;
                        }else{
                            errorMessage = "The name may not contain \",\"!";
                        }

                        if(!dismissed){
                            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }catch (NullPointerException e){
                        //Handles the case the user rotates the landscape of the phone in the dialog
                        Toast.makeText(getActivity(), "Couldn't save layout", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
            });

        }
    }

    public interface SaveLayoutInterface {
        void saveLayout(String variable_name);
    }
}
