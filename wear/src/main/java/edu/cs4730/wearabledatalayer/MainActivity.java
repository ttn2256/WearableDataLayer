package edu.cs4730.wearabledatalayer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Wear Device code  so I can kept this straight.
 *
 * This will receive messages (from a device/phone) via the datalayer (through the listener code)
 * and display them to the wear device.  There is also a button to send a message
 * to the device/phone as well.
 *
 * if the wear device receives a message from the phone/device it will then send a message back
 * via the button on the wear device, it can also send a message to the device/phone as well.
 *    There is no auto response from the phone/device otherwise we would get caught in a loop!
 *
 * debuging over bluetooth.
 * https://developer.android.com/training/wearables/apps/debugging.html
 */


public class MainActivity extends WearableActivity implements SensorEventListener {

    private final static String TAG = "Wear MainActivity";
    private TextView mX, mY, mZ;
    String datapath = "/data_path";
    private Sensor sensor;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mX = findViewById(R.id.X);
        mY = findViewById(R.id.Y);
        mZ = findViewById(R.id.Z);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Enables Always-on
        setAmbientEnabled();
    }

    //add listener.
    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    //remove listener
    @Override
    public void onPause() {
        super.onPause();
//        sensorManager.unregisterListener((SensorEventListener) this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        mX.setText(String.valueOf(sensorEvent.values[0]));
        mY.setText(String.valueOf(sensorEvent.values[1]));
        mZ.setText(String.valueOf(sensorEvent.values[2]));

        String message = sensorEvent.values[0] + "," + sensorEvent.values[1] + "," + sensorEvent.values[2];
        new SendThread(datapath, message).start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //This actually sends the message to the wearable device.
    class SendThread extends Thread {
        String path;
        String message;

        //constructor
        SendThread(String p, String msg) {
            path = p;
            message = msg;
        }

        //sends the message via the thread.  this will send to all wearables connected, but
        //since there is (should only?) be one, so no problem.
        public void run() {
            //first get all the nodes, ie connected wearable devices.
            Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                List<Node> nodes = Tasks.await(nodeListTask);

                //Now send the message to each device.
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                        Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());

                    try {
                        // Block on a task and get the result synchronously (because this is on a background
                        // thread).
                        Integer result = Tasks.await(sendMessageTask);
                        Log.v(TAG, "SendThread: message send to " + node.getDisplayName());

                    } catch (ExecutionException exception) {
                        Log.e(TAG, "Task failed: " + exception);

                    } catch (InterruptedException exception) {
                        Log.e(TAG, "Interrupt occurred: " + exception);
                    }

                }

            } catch (ExecutionException exception) {
                Log.e(TAG, "Task failed: " + exception);

            } catch (InterruptedException exception) {
                Log.e(TAG, "Interrupt occurred: " + exception);
            }
        }
    }
}
