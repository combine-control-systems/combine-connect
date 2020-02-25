package CombineConnect.Combine;
/*
Class for the debug console.
Should be instantiated everywhere you want the debug console to be displayed.
 */
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DebugConsole {
    private static final int MAXSIZE = 100;
    private RelativeLayout consoleLayout;
    private RelativeLayout parentLayout;
    private ListView listView;
    private TextView title;
    private MessageAdapter messageAdapter;
    private EditText userInput;
    private int consoleId;
    private ImageButton sendButton;

    private TransmitUserMessageInterface transmitUserMessageInterface;


    DebugConsole(RelativeLayout relativeLayout, final MainActivity activity, int height, int layoutBelow, TransmitUserMessageInterface targetFragment){
        parentLayout = relativeLayout;
        consoleLayout = new RelativeLayout(activity);
        transmitUserMessageInterface = targetFragment;

        Resources r = activity.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,//50dp
                r.getDisplayMetrics()
        );

        title = new TextView(activity);
        RelativeLayout.LayoutParams titleLayout = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        if(layoutBelow != -1){
            titleLayout.addRule(RelativeLayout.BELOW,layoutBelow);
        }
        titleLayout.setMargins(px,0,px,0);
        title.setLayoutParams(titleLayout);
        title.setText("Debug Console");
        int titleId = View.generateViewId();
        title.setId(titleId);

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        rlp.addRule(RelativeLayout.BELOW,titleId);
        rlp.setMargins(px,0,px,0);
        consoleLayout.setLayoutParams(rlp);
        consoleLayout.setBackgroundColor(Color.BLACK);
        consoleId = View.generateViewId();
        consoleLayout.setId(consoleId);

        listView = new ListView(activity);
        //adapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1,android.R.id.text1, messageList);
        messageAdapter = new MessageAdapter(activity);
        listView.setAdapter(messageAdapter);
        RelativeLayout.LayoutParams listViewParams;
        listViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                height
        );
        //listViewParams.addRule(RelativeLayout.BELOW,titleId);

        listView.setLayoutParams(listViewParams);
        listView.setDividerHeight(0);
        int listViewId = View.generateViewId();
        listView.setId(listViewId);
        consoleLayout.addView(listView);

        userInput = new EditText(activity);
        userInput.setHint("Write Message");
        userInput.setBackgroundColor(activity.getResources().getColor(R.color.backgroundColor));
        RelativeLayout.LayoutParams userInputParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        //userInputParams.addRule(RelativeLayout.BELOW,listViewId);
        userInputParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        userInput.setTextSize(14);
        userInputParams.setMargins(px,0,px,0);
        userInput.setLayoutParams(userInputParams);
        int userInputId = View.generateViewId();
        userInput.setId(userInputId);


        sendButton = new ImageButton(activity);
        RelativeLayout.LayoutParams imageButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        imageButtonParams.addRule(RelativeLayout.ALIGN_BOTTOM,userInputId);
        imageButtonParams.addRule(RelativeLayout.ALIGN_TOP,userInputId);
        imageButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int px_right = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,//10dp
                r.getDisplayMetrics()
        );
        imageButtonParams.setMargins(0,0,px_right,0);
        sendButton.setLayoutParams(imageButtonParams);
        sendButton.setImageResource(R.drawable.ic_send_black_24dp);
        sendButton.setBackgroundColor(r.getColor(R.color.backgroundColor));

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transmit(userInput.getText().toString());
                userInput.setText("");
            }
        });

        if(height == RelativeLayout.LayoutParams.MATCH_PARENT){
            listViewParams.setMargins(0,0,0,150);
            listView.setLayoutParams(listViewParams);
            sendButton.setBackgroundColor(r.getColor(R.color.ap_transparent));
            sendButton.setImageResource(R.drawable.ic_send_white_24dp);
            userInput.setBackgroundColor(r.getColor(R.color.ap_transparent));
            userInput.setTextColor(Color.YELLOW);
            userInput.setHintTextColor(Color.LTGRAY);
        }
    }

    public void applyDebugConsole(MainActivity activity){
        parentLayout.removeView(title);
        parentLayout.removeView(consoleLayout);
        parentLayout.removeView(userInput);
        parentLayout.removeView(sendButton);
        parentLayout.addView(title);
        parentLayout.addView(consoleLayout);
        parentLayout.addView(userInput);
        parentLayout.addView(sendButton);
    }

    public void displayMessageReceived(String message){
        //messageList.add(message);
        //adapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1,android.R.id.text1, messageList);
        messageAdapter.addMessage(message,true);
        //listView.setAdapter(adapter);
    }

    public void transmit(String message){
        messageAdapter.addMessage(message,false);
        transmitUserMessageInterface.transmitUserMessage(message);
    }

    public int getConsoleId(){
        return consoleId;
    }


    private class MessageAdapter extends BaseAdapter{
        private Context context;
        ArrayList<Message> messageList;

        MessageAdapter(Context context){
            this.context = context;
            messageList = new ArrayList<Message>();
        }

        @Override
        public int getCount() {
            return messageList.size();
        }

        @Override
        public Object getItem(int position) {
            return messageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater messageInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = messageInflater.inflate(R.layout.debug_console_received_message,null);
            ConsoleMessageHolder consoleMessageHolder = new ConsoleMessageHolder();
            consoleMessageHolder.messageBody = convertView.findViewById(R.id.id_console_received_textview);
            consoleMessageHolder.timestamp = convertView.findViewById(R.id.id_console_timestamp_textview);
            convertView.setTag(consoleMessageHolder);
            Message message = messageList.get(position);
            consoleMessageHolder.messageBody.setText(message.getMessage());
            consoleMessageHolder.timestamp.setText(convertTimeToString(message.getTimestamp()));
            if(!message.isReceived()){
                consoleMessageHolder.messageBody.setTextColor(Color.YELLOW);
            }


            return convertView;
        }

        public void addMessage(String message,boolean receieved){
            Message newMessage = new Message(message,receieved, Calendar.getInstance().getTimeInMillis());
            messageList.add(newMessage);
            if(messageList.size()>MAXSIZE){
                messageList.remove(0);
            }
            notifyDataSetChanged();
            listView.smoothScrollByOffset(messageList.size());
        }

        private String convertTimeToString(long timeMillis){
            long seconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(timeMillis);
            long days = TimeUnit.MILLISECONDS.toDays(timeMillis);
            return String.format(Locale.ROOT,"--%dh,%dm,%ds,%d--",
                    hours - TimeUnit.DAYS.toHours(days)+1,
                    minutes - TimeUnit.HOURS.toMinutes(hours),
                    seconds - TimeUnit.MINUTES.toSeconds(minutes),
                    timeMillis - TimeUnit.SECONDS.toMillis(seconds)
            );
        }
    }

    private class ConsoleMessageHolder{
        public TextView timestamp;
        public TextView messageBody;
    }

    private class Message{
        private String message;
        private long timestamp;
        private boolean received;
        Message(String message,boolean received,long timestamp){
            this.message = message;
            this.received = received;
            this.timestamp = timestamp;
        }

        public String getMessage() {
            return message;
        }

        public boolean isReceived() {
            return received;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public interface TransmitUserMessageInterface{
        void transmitUserMessage(String message);
    }
}
