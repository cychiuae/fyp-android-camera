package cychiuae.ust.fyp_android_camera_test_3;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

/**
 * Created by yinyinchiu on 21/6/15.
 */
public class TCPClient {

    private String address;
    private int port;
    private Socket socket;
    private boolean running;
    private MainActivity a;

    public TCPClient(String address, int port, MainActivity a) {
        this.address = address;
        this.port = port;
        this.a = a;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void connect() {
        try {
            socket = new Socket(address, port);
            running = true;
            TCPClient.this.receive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnect() {
        if (socket == null) {
            return false;
        }
        return socket.isConnected();
    }

    public void send(final ByteArrayOutputStream out) {
        new Thread("Send Thread") {
            public void run () {
                try {
                    DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
                    byte[] array = out.toByteArray();
                    dOut.writeInt(array.length);
                    dOut.write(array);
                    dOut.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void sendByte(final byte[] out){
        new Thread( "Send Thread"){
            public void run(){
                try{
                    DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
                    dOut.writeInt(out.length);
                    dOut.writeLong(new Date().getTime());
                    dOut.write(out);
                    dOut.flush();
                }
                catch( Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void test() {
        new Thread("Test Send") {
            public void run () {
                try {
                    DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
                    String s = "Test/ Test connection";
                    dOut.writeInt(s.length());
                    dOut.writeBytes(s);
                    dOut.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void receive() {
        new Thread() {
            public void run() {
                while (true) {
                    while (running) {
                        try {
                            DataInputStream dIn = new DataInputStream(socket.getInputStream());

                            int length = dIn.readInt();

                            if (length > 0 && length < 100000) {

                                long time = dIn.readLong();
                                Log.d("time", "### " + (new Date().getTime() - time));

                                byte[] data = new byte[length];
                                dIn.readFully(data, 0, data.length);

                                a.setImage(data);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }.start();
    }

}
