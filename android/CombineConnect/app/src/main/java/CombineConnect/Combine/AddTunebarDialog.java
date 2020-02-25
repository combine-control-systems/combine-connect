package CombineConnect.Combine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

/*
    Class that show the popup window on create tunebar event.
 */
public class AddTunebarDialog extends AppCompatDialogFragment {

    //EditText for userinput
    private EditText editTextVariableName;
    //interface between popup window and underlying fragment.
    private TunebarFragmentInterface tunebarFragmentInterface;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create the interface. Note that the target fragment need to be set.
        try {
            tunebarFragmentInterface = (TunebarFragmentInterface) getTargetFragment();
        } catch (Exception e) {
            throw new ClassCastException("Target Fragment must implement addTuneBarDialogListener");
        }
        // Create and inflate the popup window
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_add_tunebar,null);

        builder.setView(view).setTitle("Create Custom Tunebar")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            String variable_name = editTextVariableName.getText().toString();
                            tunebarFragmentInterface.applyNewTunebar(variable_name);
                        }catch (NullPointerException e){
                            Toast.makeText(getActivity(), "Couldn't create tunebar", Toast.LENGTH_SHORT).show();
                            //Dismisses on the case user rotates phone
                        }
                    }
                })
                .setIcon(R.drawable.ic_combine_logo);
        // create the edit text object
        editTextVariableName = view.findViewById(R.id.id_popup_tunebar_variablename);
        return builder.create();
    }

    public interface TunebarFragmentInterface {
        void applyNewTunebar(String variable_name);
    }
}
