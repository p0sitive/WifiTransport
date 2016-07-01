package com.brucetoo.wifitransport.HotPot;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Bruce Too
 * On 6/29/16.
 * At 17:50
 * This service use to send data
 *
 * @see HotpotActivity#sendFile(ClientScanResult) server -> client
 * @see HotpotActivity#sendFile(View)  client -> server
 */
public class TransferService extends IntentService {

    private static final String TAG = "TransferService";
    public static final String INTENT_FILE_TO_SEND = "file_to_send";
    public static final String INTENT_SERVER_PORT = "server_port";
    public static final String INTENT_SERVER_IP = "server_ip";
    public static final String INTENT_SEND_RESULT = "send_result";
    public static final int TIME_OUT = 5000;

    private ResultReceiver mSendResult;

    public TransferService() {
        super("ClientService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mSendResult = intent.getParcelableExtra(INTENT_SEND_RESULT);
        String serverIp = intent.getStringExtra(INTENT_SERVER_IP);
        String fileToSend = intent.getStringExtra(INTENT_FILE_TO_SEND);

        Socket clientSocket = null;
        OutputStream out = null;
        InputStream in = null;

        try {
            clientSocket = new Socket();
            clientSocket.bind(null);
            clientSocket.connect((new InetSocketAddress(serverIp, ReceiveService.PORT)), TIME_OUT);
            out = clientSocket.getOutputStream();

            Log.i(TAG, "Start send file: " + fileToSend);

            byte[] buffer = new byte[4096];
            in = new BufferedInputStream(new FileInputStream(fileToSend));
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                out.flush();
            }

            Log.i(TAG, "File send complete, sent file: " + fileToSend);
            setResult(ReceiveService.RECEIVE_SUCCESS);
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
            setResult(ReceiveService.RECEIVE_FAILED);
        } finally {
            try {
                if (clientSocket != null) {
                    if (clientSocket.isConnected()) {
                        clientSocket.close();
                    }
                }
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void setResult(int state) {
        Bundle bundle = new Bundle();
        bundle.putInt(ReceiveService.BUNDLE_RECEIVE_STATE, state);
        mSendResult.send(ReceiveService.PORT, bundle);
    }

}