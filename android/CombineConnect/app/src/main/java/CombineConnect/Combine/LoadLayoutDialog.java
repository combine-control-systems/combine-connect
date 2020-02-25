package CombineConnect.Combine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.Map;

/*
    Class that show the popup window for load layout event.
 */
public class LoadLayoutDialog extends AppCompatDialogFragment {

    //interface between popup window and underlying fragment.
    private LoadLayoutInterface loadLayoutInterface;
    private String source;

    private ListView savedLayoutsListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> savedLayoutsArrrayList;

    private String clickedLayout;

    private View lastSelectedItem;

    private SharedPreferences sharedPreferences;

    public static LoadLayoutDialog newInstance(String source){
        LoadLayoutDialog loadLayoutDialog = new LoadLayoutDialog();
        Bundle args = new Bundle();
        args.putString("source",source);
        loadLayoutDialog.setArguments(args);
        return loadLayoutDialog;
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
            loadLayoutInterface = (LoadLayoutInterface) getTargetFragment();
        } catch (Exception e) {
            throw new ClassCastException("Target Fragment must implement LoadLayoutInterface");
        }
        // Create and inflate the popup window
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_load_layout,null);

        builder.setView(view).setTitle("Load Layout")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(R.drawable.ic_combine_logo);
        // create the edit text object
        savedLayoutsListView = view.findViewById(R.id.id_saved_layouts_list_view);
        savedLayoutsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickedLayout = (String)savedLayoutsListView.getItemAtPosition(position);
                if(lastSelectedItem != null){
                    lastSelectedItem.setBackgroundColor(getResources().getColor(R.color.ap_transparent));
                }
                view.setBackgroundColor(getResources().getColor(R.color.shadedBackground));
                lastSelectedItem = view;
            }
        });

        savedLayoutsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String layoutToRemove = source + ","+ savedLayoutsListView.getItemAtPosition(position);
                PopupMenu popup = new PopupMenu(getActivity(),view);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.id_remove_saved_layout:
                                SharedPreferences deleteSharedPreferences = getActivity().getSharedPreferences(layoutToRemove,getActivity().MODE_PRIVATE);
                                SharedPreferences.Editor editor = deleteSharedPreferences.edit();
                                editor.clear();
                                editor.commit();
                                editor = sharedPreferences.edit();
                                editor.remove(layoutToRemove);
                                editor.commit();
                                dismiss();
                                return true;
                        }
                        return false;
                    }
                });
                MenuInflater inflater1 = popup.getMenuInflater();
                inflater1.inflate(R.menu.menu_delete_saved_layout,popup.getMenu());
                popup.show();
                return false;

            }
        });
        sharedPreferences = getActivity().getSharedPreferences("sharedPreferencesNames",getActivity().MODE_PRIVATE);
        savedLayoutsArrrayList = new ArrayList<String>();
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String[] entryStringArray = entry.getValue().toString().split(",");
            if(entryStringArray[0].equals(source)){
                savedLayoutsArrrayList.add(entryStringArray[1]);
            }
        }
        adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,android.R.id.text1,savedLayoutsArrrayList);
        savedLayoutsListView.setAdapter(adapter);

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
                        if(clickedLayout!=null){
                            loadLayoutInterface.loadLayout(source+","+clickedLayout);
                            dismiss();
                            dismissed = true;
                        }

                        if(!dismissed){
                            Toast.makeText(getActivity(), "Select a layout to load", Toast.LENGTH_SHORT).show();
                        }
                    }catch (NullPointerException e){
                        //Handles the case the user rotates the landscape of the phone in the dialog
                        Toast.makeText(getActivity(), "Couldn't load layout", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
            });

        }
    }

    public interface LoadLayoutInterface {
        void loadLayout(String selectedLayout);
    }

}
