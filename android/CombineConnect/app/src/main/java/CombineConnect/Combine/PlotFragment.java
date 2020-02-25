package CombineConnect.Combine;

/*
Fragment class for the calibration fragment.
For documentation of fragments, see: https://developer.android.com/guide/components/fragments
 */

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.androidplot.Plot;

import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeMetric;
import com.androidplot.ui.SizeMode;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;


import java.io.Serializable;
import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;

public class PlotFragment extends Fragment implements Serializable,AddPlotDialog.PlotFragmentInterface{
    private String TAG = "MainActivity_debug";

    //Key for import of bluetooth service class object.
    private static final String BLUETOOTHSERVICE_KEY = "bluetooth_service";
    //Create the bluetooth service class object.
    private BluetoothService bluetoothService;

    //private ImageButton addButton;

    //Create the view for global access.
    private View root;

    private enum Type{
        FLOAT,
        INT
    }

    private ImageButton addButton;
    private int lastLayoutAddedId;

    private static ArrayList<String> identifierList;
    private static ArrayList<Bundle> optionsList;
    private static ArrayList<Bundle> transactionRuleList;
    private ArrayList<Plotter> plotterArrayList;

    private ArrayList<String> variableNameOrder;
    private ArrayList<Type> typeOrder;

    private boolean created = false;

    private int layout_offset;

    private BluetoothService.BluetoothReceiveListener bluetoothReceiveListener;

    /*
        Should be called every time one want to initialize a new object of this fragment.
     */
    public static PlotFragment newInstance(BluetoothService bluetoothService, Bundle fragmentState){
        PlotFragment plotFragment = new PlotFragment();
        Bundle args = new Bundle();
        args.putSerializable(BLUETOOTHSERVICE_KEY,bluetoothService);
        args.putBundle("fragmentState",fragmentState);
        if(fragmentState != null){
            identifierList = fragmentState.getStringArrayList("identifierList");
            optionsList = (ArrayList<Bundle>) fragmentState.getSerializable("optionsList");
            transactionRuleList = (ArrayList<Bundle>) fragmentState.getSerializable("transactionRuleList");
        }
        plotFragment.setArguments(args);
        return plotFragment;
    }

    /*
        Used to save the state of the fragment when it's not active.
        Everything that is not saved here will be reset on e.g. fragment switch or orientation switch.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("identifierList",identifierList);
        outState.putSerializable("optionsList",optionsList);
        outState.putSerializable("transactionRuleList",transactionRuleList);
    }

    /*
        Used when the activity is reset to save the fragments state in the activity (not in fragment)
        Should basically be same as onSaveInstanceState.
     */
    public Bundle getFragmentState(){

        Bundle outState = new Bundle();
        outState.putStringArrayList("identifierList",identifierList);
        outState.putSerializable("optionsList",optionsList);
        outState.putSerializable("transactionRuleList",transactionRuleList);

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

        bluetoothReceiveListener = new BluetoothService.BluetoothReceiveListener() {
            @Override
            public void onPlotDataReceived(String message) {
                //add data to plot here..

                //If message starts with !, we got new transaction rules.
                boolean failed = false;
                Float[] vals = new Float[0];
                try {
                    String[] string_vals = message.split(",");
                    int len = string_vals.length;

                    vals = new Float[len];
                    for(int i = 0;i<len; i++){
                        vals[i] = Float.parseFloat(string_vals[i]);
                    }

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    failed = true;
                }


                if(!failed ){
                    try {
                        int idx = 0;
                        for(Plotter plotter : plotterArrayList){
                            SimpleXYSeries simpleXYSeries = plotter.getSimpleXYSeries();
                            if(simpleXYSeries.size() > plotter.getSampleSize()){
                                simpleXYSeries.removeFirst();
                            }
                            simpleXYSeries.addLast(null,vals[variableNameOrder.indexOf(plotter.getIdentifier())]);
                        }
                    } catch (ArrayIndexOutOfBoundsException|ConcurrentModificationException|NullPointerException e) {
                        //Handle the case we have received a bad message or if we are currently modifying the plotter(resetting)
                        e.printStackTrace();
                        failed = true;
                    }
                }

            }

            @Override
            public void onPlotTransactionRulesReceived(String message) {
                //Log.d(TAG,"new transaction rules!");
                confirmTransactionRules(message);
            }

            @Override
            public void onDebugMessageReceived(String message) {
                //To be implemented..
            }
        };
    }



    /*
            Runs once every time the view is created.
         */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.plot_fragment,container,false);
        created = false;

        addButton = root.findViewById(R.id.add_plot_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewPlotDialog();
            }
        });

        if(savedInstanceState != null){
            identifierList = savedInstanceState.getStringArrayList("identifierList");
            optionsList = (ArrayList<Bundle>) savedInstanceState.getSerializable("optionsList");
            transactionRuleList = (ArrayList<Bundle>) savedInstanceState.getSerializable("transactionRuleList");
        }
        if(identifierList == null){
            identifierList = new ArrayList<String>();
            optionsList = new ArrayList<Bundle>();
            transactionRuleList = new ArrayList<Bundle>();
        }
        extractOrderFromRuleList();

        lastLayoutAddedId = R.id.id_main_plot_text;
        plotterArrayList = new ArrayList<Plotter>();
        updateButtonLocation();

        return root;

    }

    @Override
    public void onStart() {
        super.onStart();

        if(!created){
            RelativeLayout mainLayout = getActivity().findViewById(R.id.fragment_plot);
            layout_offset = mainLayout.getChildCount();
            created = true;
            if(identifierList != null){
                for(int i = 0; i < identifierList.size();i++){
                    Plotter plotter = createPlotLayout(optionsList.get(i));
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bluetoothService.setBluetoothReceiveListener(bluetoothReceiveListener);

        for(Plotter plotter : plotterArrayList) {
            Redrawer redrawer = plotter.getRedrawer();
            try{
                redrawer.start();
            }catch (Exception e){

            }
        }
        //TODO: Decide if we want to receive message and log them even if the plot frag isnt active..
    }

    @Override
    public void onPause() {
        getFragmentState();

        for(Plotter plotter : plotterArrayList) {
            Redrawer redrawer = plotter.getRedrawer();
            redrawer.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(Plotter plotter : plotterArrayList) {
            Redrawer redrawer = plotter.getRedrawer();
            redrawer.finish();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_plot, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings_reset:
                reset();
                return true;
            case R.id.settings_request_new_rule:
                bluetoothService.transmitRequestNewTransactionRules(BluetoothService.Keywords.PLOT);
                return true;
            default:
                break;
        }
        return false;
    }

    /*
    Creates the popup window to make a new plot.
     */
    private void createNewPlotDialog(){
        AddPlotDialog addPlotDialog = AddPlotDialog.newInstance(variableNameOrder);
        addPlotDialog.setTargetFragment(this,0);
        addPlotDialog.show(getActivity().getSupportFragmentManager(),"plotDialog");
    }

    /*
    Resets to the initial view.
     */
    private void reset(){
        Log.d(TAG,"Attempting to reset view");
        RelativeLayout mainLayout = getActivity().findViewById(R.id.fragment_plot);
        int idx_last_added_view = mainLayout.getChildCount()-1;
        for(int i = idx_last_added_view; i>=layout_offset; i--){
            mainLayout.removeViewAt(i);
        }
        plotterArrayList.clear();
        identifierList.clear();
        optionsList.clear();
        lastLayoutAddedId = R.id.id_main_plot_text;
        updateButtonLocation();
    }

    /*
    Removes a specific plot
     */
    private void removePlot(String identifier){
        RelativeLayout mainLayout = getActivity().findViewById(R.id.fragment_plot);
        int idx = identifierList.indexOf(identifier);
        if(lastLayoutAddedId == plotterArrayList.get(idx).getLayoutId()){
            if(plotterArrayList.size()>1){
                lastLayoutAddedId = plotterArrayList.get(idx-1).getLayoutId();
            }else{
                lastLayoutAddedId = R.id.id_main_plot_text;
            }
        }else{
            //Got to change the layout reference placement of next layout.
            RelativeLayout nextTuneBarLayout = getActivity().findViewById(plotterArrayList.get(idx+1).getLayoutId());
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)nextTuneBarLayout.getLayoutParams();
            if(idx == 0){
                rlp.addRule(RelativeLayout.BELOW,R.id.id_main_plot_text);
            }else{
                rlp.addRule(RelativeLayout.BELOW,plotterArrayList.get(idx-1).getLayoutId());
            }

            nextTuneBarLayout.setLayoutParams(rlp);
        }
        mainLayout.removeViewAt(idx+layout_offset);
        plotterArrayList.remove(idx);
        identifierList.remove(idx);
        optionsList.remove(idx);

        updateButtonLocation();

    }

    @Override
    public void applyNewPlot(Bundle options) {
        String plot_title = options.getString("title");
        identifierList.add(plot_title);
        optionsList.add(options);
        createPlotLayout(options);
    }

    /*
    Creates a new PlotLayout and assigns a plotter to it.
     */
    public Plotter createPlotLayout(Bundle options){
        PlotLayout plotLayout = new PlotLayout(getActivity(),lastLayoutAddedId,options.getString("title"));
        RelativeLayout rl = plotLayout.getLayout();
        RelativeLayout mainRelativeLayout = root.findViewById(R.id.fragment_plot);
        mainRelativeLayout.addView(rl);

        lastLayoutAddedId = plotLayout.getLayoutId();

        Plotter plotter = new Plotter(plotLayout.getPlotId(),plotLayout.getButtonId(),plotLayout.getLayoutId(),options);

        XYPlot plot = plotter.getPlot();

        plotterArrayList.add(plotter);

        plotter.onCreateView();
        updateButtonLocation();
        return plotter;
    }

    public void updateButtonLocation(){
        RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams) addButton.getLayoutParams();
        buttonParams.addRule(RelativeLayout.BELOW,lastLayoutAddedId);
        addButton.setLayoutParams(buttonParams);
    }

    private void confirmTransactionRules(String message){
        transactionRuleList = new ArrayList<Bundle>();
        try{
            String[] string_vals = message.split(",");
            int len = string_vals.length;
            for(int i = 1;i<len-1; i+=2){
                Bundle rule = new Bundle();
                rule.putString("variableName",string_vals[i]);
                rule.putSerializable("Type",Type.values()[Integer.valueOf(string_vals[i+1])]);
                transactionRuleList.add(rule);
            }
            extractOrderFromRuleList();
            //Confirm that we have receieved the transaction rules.
            bluetoothService.transmitApproveTransactionRules(BluetoothService.Keywords.PLOT);
        }catch (NumberFormatException e){
            //If received message is truncated, do nothing.
        }

    }

    private void extractOrderFromRuleList(){
        typeOrder = new ArrayList<Type>();
        variableNameOrder = new ArrayList<String>();
        for(Bundle rule : transactionRuleList){
            typeOrder.add((Type)rule.getSerializable("Type"));
            variableNameOrder.add(rule.getString("variableName"));
        }

    }

    public class Plotter{
        private ImageButton imageButton;

        private int layoutId;
        private int plotId;
        private int buttonId;
        private String identifier;
        private Bundle options;

        private XYPlot plot;
        private Redrawer redrawer;
        private SimpleXYSeries simpleXYSeries;

        Plotter(int plotId, int buttonId, int layoutId, Bundle options){
            this.plotId = plotId;
            this.buttonId = buttonId;
            this.layoutId = layoutId;
            //this.identifier = identifier;
            this.identifier = options.getString("title");
            this.options = options;
        }

        public void onCreateView(){

            ImageButton imageButton = getActivity().findViewById(buttonId);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePlot(identifier);
                }
            });

            plot = (XYPlot) root.findViewById(plotId);

            //Set boundaries on data (When is data resetting?):
            BoundaryMode boundaryModeY = BoundaryMode.FIXED;
            if(options.getBoolean("autoy")){
                boundaryModeY = BoundaryMode.AUTO;
            }
            plot.setRangeBoundaries(options.getFloat("limitLow"),options.getFloat("limitHigh"), boundaryModeY);

            BoundaryMode boundaryModeX = BoundaryMode.FIXED;
            if(options.getBoolean("autox")){
                boundaryModeX = BoundaryMode.AUTO;
            }
            plot.setDomainBoundaries(0,options.getInt("samples"),boundaryModeX);

            //Set how the data should be stepping
            plot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
            //Set when the vertical grid bars should occur
            plot.setDomainStepValue(options.getInt("samples")/10);

            //Set number of lines in the plot
            plot.setLinesPerRangeLabel(1);

            //Labels
            plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("#"));
            plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new DecimalFormat("#"));

            //Paints:
            plot.setBorderStyle(Plot.BorderStyle.SQUARE,null,null);
            plot.getBorderPaint().setColor(getResources().getColor(R.color.Transparent));
            plot.getBackgroundPaint().setColor(Color.TRANSPARENT);
            plot.getGraph().getBackgroundPaint().setColor(Color.TRANSPARENT);
            plot.getGraph().getDomainGridLinePaint().setColor(getResources().getColor(R.color.colorAccent));
            plot.getGraph().getRangeGridLinePaint().setColor(getResources().getColor(R.color.colorAccent));
            plot.getGraph().getDomainOriginLinePaint().setColor(getResources().getColor(R.color.colorAccent));
            plot.getGraph().getRangeOriginLinePaint().setColor(getResources().getColor(R.color.colorAccent));

            if(!options.getBoolean("xtick")){
                plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).getPaint().setColor(Color.TRANSPARENT);
            }
            if(!options.getBoolean("ytick")){
                plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).getPaint().setColor(Color.TRANSPARENT);
            }

            plot.getLegend().setSize(new Size(25, SizeMode.ABSOLUTE,400,SizeMode.ABSOLUTE));
            plot.getLegend().position(140, HorizontalPositioning.ABSOLUTE_FROM_LEFT,0, VerticalPositioning.ABSOLUTE_FROM_TOP);
            plot.getLegend().getIconSize().setHeight(new SizeMetric(55,SizeMode.ABSOLUTE));
            plot.getLegend().setVisible(options.getBoolean("legend"));
            if(options.getBoolean("xtitle")){
                plot.getDomainTitle().setText("Sample Number");
            }
            if(options.getBoolean("ytitle")){
                plot.getRangeTitle().setText("Value");
            }
            plot.getDomainTitle().position(-120, HorizontalPositioning.ABSOLUTE_FROM_CENTER,110, VerticalPositioning.ABSOLUTE_FROM_BOTTOM);
            plot.getRangeTitle().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT,-110, VerticalPositioning.ABSOLUTE_FROM_CENTER);

            simpleXYSeries = new SimpleXYSeries(identifier);
            simpleXYSeries.useImplicitXVals();
            //Add the information to the plot:
            plot.addSeries(simpleXYSeries,new LineAndPointFormatter(Color.rgb(100,100,200),null,null,null));

            redrawer = new Redrawer(Arrays.asList(new Plot[]{plot}),100,false);
            redrawer.start();

            //plot1.getGraph().getGridBackgroundPaint().setColor(Color.WHITE);

        }

        public XYPlot getPlot(){
            return plot;
        }

        public SimpleXYSeries getSimpleXYSeries(){
            return simpleXYSeries;
        }

        public Redrawer getRedrawer(){
            return redrawer;
        }

        public int getLayoutId(){
            return layoutId;
        }

        public int getSampleSize(){
            return options.getInt("samples");
        }

        public String getIdentifier(){
            return identifier;
        }


    }












}

