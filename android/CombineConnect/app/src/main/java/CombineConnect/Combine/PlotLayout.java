package CombineConnect.Combine;
/*
Class that creates the layout of the plot object.
 */
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.androidplot.xy.XYPlot;

public class PlotLayout {
    private RelativeLayout relativeLayout;
    private FrameLayout frameLayout;

    private int layoutId;
    private int frameLayoutId;
    private int id_plot;
    private int buttonId;

    private String identifier;

    PlotLayout(Activity activity, int LayoutBelow, String identifer){
        this.identifier = identifer;
        Resources r = activity.getResources();
        //----------Create Main Relative Layout :-------------------------
        relativeLayout = new RelativeLayout(activity);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        rlp.addRule(RelativeLayout.BELOW,LayoutBelow);
        //Convert px to dp
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                50,//50dp
                r.getDisplayMetrics()
        );
        rlp.setMargins(0,px,0,0);
        layoutId = View.generateViewId();
        relativeLayout.setLayoutParams(rlp);
        relativeLayout.setId(layoutId);

        //----------Create Main FrameLayout view:-------------------------
        frameLayout = new FrameLayout(activity);
        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        frameLayout.setLayoutParams(fp);
        frameLayoutId = View.generateViewId();
        frameLayout.setId(frameLayoutId);

        //----------Create Plot:-------------------------
        LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        XYPlot xyPlot = (XYPlot) inflater.inflate(R.layout.xy_plot,frameLayout,false);

        id_plot = View.generateViewId();
        xyPlot.setId(id_plot);
        frameLayout.addView(xyPlot);

        //----------Create remove Button:-------------------------
        ImageButton imageButton = new ImageButton(activity);
        RelativeLayout.LayoutParams imageButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        imageButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int px_right = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,//10dp
                r.getDisplayMetrics()
        );
        imageButtonParams.setMargins(0,-30,px_right,0);
        imageButton.setLayoutParams(imageButtonParams);
        imageButton.setImageResource(R.drawable.ic_remove_black_24dp);
        imageButton.setBackgroundColor(Color.TRANSPARENT);
        buttonId = View.generateViewId();
        imageButton.setId(buttonId);

        relativeLayout.addView(imageButton);

        //Add framelayout to rel. layout.
        relativeLayout.addView(frameLayout);

    }

    public RelativeLayout getLayout(){
        return relativeLayout;
    }

    public int getLayoutId(){
        return layoutId;
    }

    public int getPlotId(){
        return id_plot;
    }

    public int getButtonId(){
        return buttonId;
    }

    public String getIdentifier(){
        return identifier;
    }

}
