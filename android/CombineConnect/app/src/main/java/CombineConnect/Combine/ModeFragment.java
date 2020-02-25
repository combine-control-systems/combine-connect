package CombineConnect.Combine;
/*
Not used. Consider it as a code skeleton of a fragment.
 */
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.Serializable;


public class ModeFragment extends Fragment implements Serializable{
    private String TAG = "MainActivity_debug";

    //Key for import of bluetooth service class object.
    private static final String BLUETOOTHSERVICE_KEY = "bluetooth_service";
    //Create the bluetooth service class object.
    private BluetoothService bluetoothService;


    //Create the view for global access.
    private View root;





    /*
        Should be called every time one want to initialize a new object of this fragment.
     */
    public static ModeFragment newInstance(BluetoothService bluetoothService,Bundle fragmentState){
        ModeFragment modeFragment = new ModeFragment();
        Bundle args = new Bundle();
        args.putSerializable(BLUETOOTHSERVICE_KEY,bluetoothService);
        args.putBundle("fragmentState",fragmentState);
        if(fragmentState != null){

        }
        modeFragment.setArguments(args);
        return modeFragment;
    }

    /*
        Used to save the state of the fragment when it's not active.
        Everything that is not saved here will be reset on e.g. fragment switch or orientation switch.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    /*
        Used when the activity is reset to save the fragments state in the activity (not in fragment)
        Should basically be same as onSaveInstanceState.
     */
    public Bundle getFragmentState(){

        Bundle outState = new Bundle();


        return outState;
    }

    /*
        Will initialize the bluetoothservice object if its attached as an argument to fragment transaction.
        Also sets the global factors.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(getArguments() != null){
            Bundle bundle = getArguments();
            bluetoothService = (BluetoothService) bundle.getSerializable(BLUETOOTHSERVICE_KEY);
        }
    }


    /*
        Runs once every time the view is created.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.mode_fragment,container,false);

        return root;

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onPause() {
        getFragmentState();
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_calibration, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings_reset:

                return true;
            default:
                break;
        }
        return false;
    }












}

