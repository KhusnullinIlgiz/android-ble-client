package com.example.zebra_test;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import static com.example.zebra_test.MainActivity.boxdata;
import static com.example.zebra_test.MainActivity.handler;
import static com.example.zebra_test.MainActivity.mmSocket;


public class ReadBoxData implements Runnable {


    private String btMsg;

//    "Oops! Must be your gateway not available!"
    public static void showToast(Context mcontext, String message){

        Toast.makeText(mcontext, message, Toast.LENGTH_LONG).show();
    }

    public ReadBoxData(String msg) {
        btMsg = msg;
    }


    public void run() {
        MainActivity.sendBtMsg(btMsg);
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
                                boxdata.clear();
                                String[] str;
                                if (!data.contains("stopped")) {
                                    str = data.split(";");
                                    boxdata.addAll(Arrays.asList(str));
                                }
    //                            for (int i=0;i<=str.length-1;i++){
    //                                boxdata.add(str[i]);
    //                            }

                                System.out.println(Arrays.toString(boxdata.toArray()));
                            }
                        });

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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            System.out.println("HUI");
        }
    }

}
