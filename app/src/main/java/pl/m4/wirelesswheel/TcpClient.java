package pl.m4.wirelesswheel;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpClient {
    public static final String TAG = "TcpClient";
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private TalkerTask talkerTask;
    private String msgToSend;

    public TcpClient() {
        socket = null;
        dataOutputStream = null;
        dataInputStream = null;
        msgToSend = "IP";
        talkerTask = null;
    }

    public void connect(Context context, String address) {
        int port = getPort(address);
        String host = getHost(address);
        if (port == 0 || host == null)
            showToast(context, "Invalid address format.");
        Log.i(TAG, "address: "+host+":"+port);
        talkerTask = new TalkerTask();
        new ConnectTask(context).execute(host, String.valueOf(port));
    }

    public void disconnect(Context context) {
        if (socket != null && socket.isConnected()) {
            try {
                dataOutputStream.close();
                dataInputStream.close();
                socket.close();
                socket = null;
            } catch (IOException e) {
                showToast(context, "Couldn't get I/O for the connection");
                Log.e(TAG, "disconnect "+e.getMessage());
            }
        }
    }

    /**
     * Function to send a message to host.
     * @param message String types message.
     */
    public void send(String message) {
        byte[] buffer = message.getBytes();
        if (socket != null && socket.isConnected()) {
            Log.i(TAG, "msg sended: "+message);
            try {
                dataOutputStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Function set message to send (TalkerTask). Message will be send in loop.
     * @param msg message for send to host.
     */
    public void setMessage(String msg){
        msgToSend = msg;
    }

    public String getHost(String address){
        String separatedAddress[] = address.split(":");
        if (separatedAddress.length != 2)
            return null;
        return separatedAddress[0];
    }

    public int getPort(String address){
        String separatedAddress[] = address.split(":");
        if (separatedAddress.length != 2)
            return 0;
        try {
            return Integer.parseInt(separatedAddress[1]);
        }catch(NumberFormatException e){
            Log.e(TAG, "Invalid port, must be a number.");
        }catch(Exception e){
            Log.e(TAG, e.getMessage());
        }
        return 0;
    }
    /**
     * Specific function for robots...
     * message format:
     * j:0;a:-255:-255:0   :0   ;b:0:0:0:0:0:0:0:0:0:0:0:0;h:0=[0, 0];END#
     *      around f\b:l\r
     * j:0;a:255 :0   :0   :0   ;b:0:0:0:0:0:0:0:0:0:0:0:0;h:0=[0, 0];END#
     * third param axis left/right
     * second param axis forward/back
     * first param around axis
     * '-255' - move to forward
     * 67 - message size
     * @param axisAround - first param around axis
     * @param axisX second param axis forward/back
     * @param axisY third param axis left/right
     * @return String generated message to robot must be 67bytes.
     */
    public String generateMessage(String axisAround, String axisX, String axisY){
        String msg = "j:0;a:"+axisAround+":"+axisX+":"+axisY+":0   ;b:0:0:0:0:0:0:0:0:0:0:0:0;h:0=[0, 0];END#";
        return msg;
    }

    private class TalkerTask extends AsyncTask<Void, Void, String> {
        private String recvMsg;

        public TalkerTask() {
            this.recvMsg = null;
        }

        @Override
        protected String doInBackground(Void... params) {
            while (socket != null && socket.isConnected()) {
                int count = 0;
                send(msgToSend);
                do {
                    try {
                        count = dataInputStream.available();
                        if (count > 0) {
                            byte[] buffer = new byte[count];
                            dataInputStream.read(buffer);
                            recvMsg = new String(buffer, "UTF-8");
                            Log.i(TAG, "msg recv: " + recvMsg);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "talkerTask: "+e.getMessage());
                    }
                } while (count <= 0 && socket != null);
            }
            return recvMsg;
        }
    }

    private class ConnectTask extends AsyncTask<String, Void, Void> {
        private Context context;
        private String host;
        private int port;

        public ConnectTask(Context context) {
            socket = null;
            this.context = context;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (socket == null || !socket.isConnected()) {
                showToast(context, "Can't connect with a host.");
            }else
                talkerTask.execute();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                host = params[0];
                port = Integer.parseInt(params[1]);
                socket = new Socket(host, port);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                Log.i(TAG, "connected");
            } catch (UnknownHostException e) {
                showToast(context, "Don't know about host: " + host + ":" + port);
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                showToast(context, "Couldn't get I/O for the connection to: " + host + ":" + port);
                Log.e(TAG, e.getMessage());
            }
            return null;
        }
    }

    private void showToast(final Context context, final String message) {
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}