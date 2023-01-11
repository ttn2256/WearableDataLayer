package edu.cs4730.wearabledatalayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * this is the main activity on the device/phone  (just to kept everything straight)
 *
 * The device will setup a listener to receive messages from the wear device and display them to the screen.
 *
 * It also setups up a button, so it can send a message to the wear device, the wear device will auto
 * response to the message.   This code does not auto response, otherwise we would get caught in a loop.
 *
 * This is all down via the datalayer in the googleApiClient that requires om.google.android.gms:play-services-wearable
 * in the gradle (both wear and mobile).  Also the applicationId MUST be the same in both files as well
 * both use a the "/message_path" to send/receive messages.
 *
 * debuging over bluetooth.
 * https://developer.android.com/training/wearables/apps/debugging.html
 */

public class MainActivity extends AppCompatActivity {

    TextView mxyz;
    protected Handler handler;
    String TAG = "Mobile MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mxyz = findViewById(R.id.XYZ);

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

    }

    //setup a broadcast receiver to receive the messages from the wear device via the listenerService.
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.v(TAG, "Main activity received message: " + message);
            // Display message in UI
            mxyz.setText(message);
        }
    }
}
