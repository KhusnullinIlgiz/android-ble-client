package com.example.zebra_test;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    public static BluetoothSocket mmSocket;
    public static BluetoothDevice mmDevice = null;
    public String process_is_running = "";
    public String fileIsEmpty = "";
    public static BluetoothAdapter mBluetoothAdapter;
    public TextView gatewayNameTextView;
    public TextView statusTextView;
    public static TextView eventTableTextView;
    public static Handler handler;
    public static ListView boxDataListView;
    public static ArrayAdapter<String> arrayAdapter;
    public static ArrayList<String> boxdata;
    public int flag = 0;
    public ImageView greenImageView1;
    public ImageView greenImageView2;
    public ImageView redImageView1;
    public ImageView redImageView2;
    public Button startStopButton;
    public ImageView tempImageView1;
    public ImageView tempImageView2;
    public ImageView lightImageView1;
    public ImageView lightImageView2;
    public ImageView movedImageView1;
    public ImageView movedImageView2;
    public TextView tempTextView1;
    public TextView tempTextView2;
    public TextView lightTextView1;
    public TextView lightTextView2;
    public TextView movedTextView1;
    public TextView movedTextView2;
    public TextView thresholdTemperatureTextView1;
    public TextView thresholdTemperatureTextView2;
    Dialog popupMenu1;
    Dialog popupMenu2;
    public String thresholdTemperature1 = "";
    public String thresholdTemperature2 = "";


    public static void sendBtMsg(String msg2send) {
        UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //Standard SerialPortService ID
        try {

            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            while (!mmSocket.isConnected()) {
                mmSocket.connect();
                if(mmSocket.isConnected()){
                    break;
                }
            }
            String msg = msg2send;
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void generateListView() {
        if(!boxdata.isEmpty()){
            if(flag==1){
                eventTableTextView.setText("Event Table Box1:");
            }else if(flag==2){
                eventTableTextView.setText("Event Table Box2:");
            }
        }else{
            eventTableTextView.setText("Event Table:");
        }
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, boxdata);
        boxDataListView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void checkConnection(){

        if(!boxdata.isEmpty()){
            boxdata.clear();
        }
        flag = 0;
        boxDataListView.setAdapter(null);
        generateListView();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("raspberrypi")) {
                    mmDevice = device;
                    break;
                } else {
                    Toast.makeText(getApplicationContext(), "No device to connect", Toast.LENGTH_SHORT).show();
                }
            }
        }
        (new Thread(new workerThread("test"))).start();
    }

    //get data form box1
    public void box1(View view) {
        if(!boxdata.isEmpty()){
            boxdata.clear();
        }
        flag = 1;
        boxDataListView.setAdapter(null);
        Thread thread = new Thread(new ReadBoxData("box1"));
        thread.start();
        (new Handler()).postDelayed(this::generateListView, 7000);

    }

    public void box2(View view) {
        if(!boxdata.isEmpty()){
            boxdata.clear();
        }
        flag = 2;
        boxDataListView.setAdapter(null);
        Thread thread = new Thread(new ReadBoxData("box2"));
        thread.start();
        (new Handler()).postDelayed(this::generateListView, 7000);
    }

    public void popup1(View v){
        TextView txtclose;
        Button okButton;
        EditText temperatureEditText;
        popupMenu1.setContentView(R.layout.popup_menu);
        okButton = (Button) popupMenu1.findViewById(R.id.okButton);
        txtclose = (TextView) popupMenu1.findViewById(R.id.close_popup);
        temperatureEditText = (EditText) popupMenu1.findViewById(R.id.temperatureEditText);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu1.dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                thresholdTemperatureTextView1.setText(temperatureEditText.getText().toString() + " C");
                thresholdTemperature1 = temperatureEditText.getText().toString();
                    if(!thresholdTemperature1.equals("")){
                        thresholdTemperatureTextView1.setVisibility(View.VISIBLE);
                        popupMenu1.dismiss();
                    }else{
                        Toast.makeText(getApplicationContext(), "Temperature threshold is not set!", Toast.LENGTH_SHORT).show();
                    }


                }
        });
        popupMenu1.show();
    }

    public void popup2(View v){
        TextView txtclose;
        Button okButton;
        EditText temperatureEditText;
        popupMenu2.setContentView(R.layout.popup_menu2);
        okButton = (Button) popupMenu2.findViewById(R.id.okButton);
        txtclose = (TextView) popupMenu2.findViewById(R.id.close_popup);
        temperatureEditText = (EditText) popupMenu2.findViewById(R.id.temperatureEditText);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu2.dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thresholdTemperatureTextView2.setText(temperatureEditText.getText().toString() + " C");
                thresholdTemperature2 = temperatureEditText.getText().toString();
                if(!thresholdTemperature2.equals("")){
                    thresholdTemperatureTextView2.setVisibility(View.VISIBLE);
                    popupMenu2.dismiss();
                }else{
                    Toast.makeText(getApplicationContext(), "Temperature threshold is not set!", Toast.LENGTH_SHORT).show();
                }


            }
        });
        popupMenu2.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void refresh(View view) {
        checkConnection();
    }

    public void checkVisibility(){
        if(!fileIsEmpty.equals("") && !process_is_running.equals("started") && !process_is_running.equals("notActivated")){
            if(fileIsEmpty.equals("0")){
                greenImageView1.setVisibility(View.VISIBLE);
                greenImageView2.setVisibility(View.VISIBLE);
                redImageView1.setVisibility(View.INVISIBLE);
                redImageView2.setVisibility(View.INVISIBLE);

                tempImageView1.setVisibility(View.INVISIBLE);
                tempImageView2.setVisibility(View.INVISIBLE);
                lightImageView1.setVisibility(View.INVISIBLE);
                lightImageView2.setVisibility(View.INVISIBLE);
                movedImageView1.setVisibility(View.INVISIBLE);
                movedImageView2.setVisibility(View.INVISIBLE);
                tempTextView1.setVisibility(View.INVISIBLE);
                tempTextView2.setVisibility(View.INVISIBLE);
                lightTextView1.setVisibility(View.INVISIBLE);
                lightTextView2.setVisibility(View.INVISIBLE);
                movedTextView1.setVisibility(View.INVISIBLE);
                movedTextView2.setVisibility(View.INVISIBLE);


            }else if(fileIsEmpty.equals("1")){
                greenImageView1.setVisibility(View.INVISIBLE);
                greenImageView2.setVisibility(View.VISIBLE);
                redImageView1.setVisibility(View.VISIBLE);
                redImageView2.setVisibility(View.INVISIBLE);

                tempImageView1.setVisibility(View.VISIBLE);
                tempImageView2.setVisibility(View.INVISIBLE);
                lightImageView1.setVisibility(View.VISIBLE);
                lightImageView2.setVisibility(View.INVISIBLE);
                movedImageView1.setVisibility(View.VISIBLE);
                movedImageView2.setVisibility(View.INVISIBLE);
                tempTextView1.setVisibility(View.VISIBLE);
                tempTextView2.setVisibility(View.INVISIBLE);
                lightTextView1.setVisibility(View.VISIBLE);
                lightTextView2.setVisibility(View.INVISIBLE);
                movedTextView1.setVisibility(View.VISIBLE);
                movedTextView2.setVisibility(View.INVISIBLE);
            }else if(fileIsEmpty.equals("2")){
                greenImageView1.setVisibility(View.VISIBLE);
                greenImageView2.setVisibility(View.INVISIBLE);
                redImageView1.setVisibility(View.INVISIBLE);
                redImageView2.setVisibility(View.VISIBLE);

                tempImageView1.setVisibility(View.INVISIBLE);
                tempImageView2.setVisibility(View.VISIBLE);
                lightImageView1.setVisibility(View.INVISIBLE);
                lightImageView2.setVisibility(View.VISIBLE);
                movedImageView1.setVisibility(View.INVISIBLE);
                movedImageView2.setVisibility(View.VISIBLE);
                tempTextView1.setVisibility(View.INVISIBLE);
                tempTextView2.setVisibility(View.VISIBLE);
                lightTextView1.setVisibility(View.INVISIBLE);
                lightTextView2.setVisibility(View.VISIBLE);
                movedTextView1.setVisibility(View.INVISIBLE);
                movedTextView2.setVisibility(View.VISIBLE);
            }else if(fileIsEmpty.equals("3")){
                greenImageView1.setVisibility(View.INVISIBLE);
                greenImageView2.setVisibility(View.INVISIBLE);
                redImageView1.setVisibility(View.VISIBLE);
                redImageView2.setVisibility(View.VISIBLE);

                tempImageView1.setVisibility(View.VISIBLE);
                tempImageView2.setVisibility(View.VISIBLE);
                lightImageView1.setVisibility(View.VISIBLE);
                lightImageView2.setVisibility(View.VISIBLE);
                movedImageView1.setVisibility(View.VISIBLE);
                movedImageView2.setVisibility(View.VISIBLE);
                tempTextView1.setVisibility(View.VISIBLE);
                tempTextView2.setVisibility(View.VISIBLE);
                lightTextView1.setVisibility(View.VISIBLE);
                lightTextView2.setVisibility(View.VISIBLE);
                movedTextView1.setVisibility(View.VISIBLE);
                movedTextView2.setVisibility(View.VISIBLE);
            }
        }else{
            greenImageView1.setVisibility(View.INVISIBLE);
            greenImageView2.setVisibility(View.INVISIBLE);
            redImageView1.setVisibility(View.INVISIBLE);
            redImageView2.setVisibility(View.INVISIBLE);

            tempImageView1.setVisibility(View.INVISIBLE);
            tempImageView2.setVisibility(View.INVISIBLE);
            lightImageView1.setVisibility(View.INVISIBLE);
            lightImageView2.setVisibility(View.INVISIBLE);
            movedImageView1.setVisibility(View.INVISIBLE);
            movedImageView2.setVisibility(View.INVISIBLE);
            tempTextView1.setVisibility(View.INVISIBLE);
            tempTextView2.setVisibility(View.INVISIBLE);
            lightTextView1.setVisibility(View.INVISIBLE);
            lightTextView2.setVisibility(View.INVISIBLE);
            movedTextView1.setVisibility(View.INVISIBLE);
            movedTextView2.setVisibility(View.INVISIBLE);
        }
        if(process_is_running.equals("")){
            startStopButton.setVisibility(View.INVISIBLE);
        }else if(process_is_running.equals("notActivated") || process_is_running.equals("stopped")){
            startStopButton.setText("start");
            startStopButton.setVisibility(View.VISIBLE);

        }else if(process_is_running.equals("started")){
            startStopButton.setText("stop");
            startStopButton.setVisibility(View.VISIBLE);
        }

    }




    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        boxdata = new ArrayList<>();
        gatewayNameTextView = (TextView) findViewById(R.id.gatewayNameTextView);
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        eventTableTextView= (TextView) findViewById(R.id.eventTableTextView);
        boxDataListView = (ListView) findViewById(R.id.boxDataListView);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        greenImageView1 = findViewById(R.id.greenImageView1);
        greenImageView2 = findViewById(R.id.greenImageView2);
        redImageView1 = findViewById(R.id.redImageView1);
        redImageView2 = findViewById(R.id.redImageView2);
        startStopButton = (Button) findViewById(R.id.startStopButton);
        startStopButton.setVisibility(View.INVISIBLE);

        tempImageView1 = findViewById(R.id.tempImageView1);
        tempImageView2= findViewById(R.id.tempImageView2);
        lightImageView1= findViewById(R.id.lightImageView1);
        lightImageView2= findViewById(R.id.lightImageView2);
        movedImageView1 = findViewById(R.id.movedImageView1);
        movedImageView2 = findViewById(R.id.movedImageView2);

        tempTextView1= findViewById(R.id.tempTextView1);
        tempTextView2= findViewById(R.id.tempTextView2);
        lightTextView1= findViewById(R.id.lightTextView1);
        lightTextView2= findViewById(R.id.lightTextView2);
        movedTextView1 = findViewById(R.id.movedTextView1);
        movedTextView2 = findViewById(R.id.movedTextView2);
        popupMenu1 = new Dialog(this);
        popupMenu2 = new Dialog(this);
        thresholdTemperatureTextView1 = findViewById(R.id.thresholdTemperatureTextView1);
        thresholdTemperatureTextView2 = findViewById(R.id.thresholdTemperatureTextView2);

        thresholdTemperatureTextView1.setVisibility(View.INVISIBLE);
        thresholdTemperatureTextView2.setVisibility(View.INVISIBLE);







        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
        checkConnection();
        checkVisibility();


        //start/stop recording data button
        startStopButton.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(View v) {
                // Perform action on temp button click



                if(process_is_running.equals("stopped") || process_is_running.equals("notActivated")){
                    if(!boxdata.isEmpty()){
                        boxdata.clear();
                    }
                    flag = 0;
                    boxDataListView.setAdapter(null);
                    if(!thresholdTemperature1.equals("") && !thresholdTemperature2.equals("")){
                        new AlertDialog.Builder(MainActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Start")
                                .setMessage("Do you want to start?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        (new Thread(new workerThread("start " + thresholdTemperature1 + " " + thresholdTemperature2))).start();
                                    }
                                })
                                .setNegativeButton("No",null)
                                .show();
                        generateListView();
                    }else{
                        Toast.makeText(getApplicationContext(), "Temperature threshold is not set!", Toast.LENGTH_SHORT).show();
                    }


                }else if(process_is_running.equals("started")){
                    BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
                    AdvertiseSettings settings = new AdvertiseSettings.Builder()
                            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                            .setConnectable(false)
                            .build();
                    ParcelUuid pUuid = new ParcelUuid(UUID.fromString(getString(R.string.ble_uuid)));
                    AdvertiseData data = new AdvertiseData.Builder()
                            .setIncludeDeviceName(false)
                            .addServiceUuid(pUuid)
                            .build();

                    AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
                        @Override
                        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                            super.onStartSuccess(settingsInEffect);
                        }
                    };
                    advertiser.startAdvertising(settings, data, advertisingCallback);

                    new AlertDialog.Builder(MainActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Stop")
                            .setMessage("Do you want to stop?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override

                                public void onClick(DialogInterface dialog, int i) {

                                    (new Thread(new workerThread("stop"))).start();
                                }
                            })
                            .setNegativeButton("No",null)
                            .show();
                    try {
                        Thread.sleep(2000);
                        advertiser.stopAdvertising(advertisingCallback);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });





    }


    //class to send get data to/from gateway RPI
    final class workerThread implements Runnable {

        private String btMsg;

        public workerThread(String msg) {
            btMsg = msg;
        }

        public void run() {
            sendBtMsg(btMsg);
            if (mmSocket.isConnected() == true) {
                while (!Thread.currentThread().isInterrupted()) {
                    int bytesAvailable;
                    boolean workDone = false;

                    try {
                        final InputStream mmInputStream;

                        mmInputStream = mmSocket.getInputStream();


                        byte[] messageByte = new byte[32768];
                        bytesAvailable = mmInputStream.read(messageByte);


                        if (bytesAvailable > 0) {
                            final String data = new String(messageByte, 0, bytesAvailable);

                            handler.post(new Runnable() {
                                public void run() {
                                    String[] str = data.split(" ");
                                    process_is_running = str[0];
                                    if (str.length > 1) {
                                        fileIsEmpty = str[1];
                                    }

                                    //System.out.println(str);



                                    gatewayNameTextView.setText(mmDevice.getName());

//                                    if (data.equals("launched")) {
//                                        statusTextView.setTextColor(Color.parseColor("#00FF00"));
//
//                                    } else if (data.equals("stopped")) {
//                                        statusTextView.setTextColor(Color.parseColor("#FF0000"));
//                                    } else {
//                                        statusTextView.setTextColor(Color.BLACK);
//                                    }

                                    statusTextView.setText(process_is_running);
                                    if(str.length>2){
                                        lightTextView1.setText(str[2]);
                                        tempTextView1.setText(str[3]);
                                        movedTextView1.setText(str[4]);
                                        lightTextView2.setText(str[5]);
                                        tempTextView2.setText(str[6]);
                                        movedTextView2.setText(str[7]);
                                    }


                                    checkVisibility();

                                }
                            });
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    workDone = true;

                    if (workDone == true) {
                        try {
                            mmSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }


                }
            }else{
                process_is_running = "";
                fileIsEmpty = "";


                checkVisibility();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusTextView.setText("Not Connected");
                        gatewayNameTextView.setText("Unknown");
                        Toast.makeText(getApplicationContext(), "Oops! Must be your gateway not available!", Toast.LENGTH_LONG).show();
                    }
                });

            }
        }
    }
}