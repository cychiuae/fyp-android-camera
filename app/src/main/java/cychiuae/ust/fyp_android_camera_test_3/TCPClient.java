package cychiuae.ust.fyp_android_camera_test_3;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by yinyinchiu on 21/6/15.
 */
public class TCPClient {

    private String address;
    private int port;
    private Socket socket;
    private Thread sendThread;

    public TCPClient(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void connect() {
        try {
            socket = new Socket(address, port);
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

}
