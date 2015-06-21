package cychiuae.ust.fyp_android_camera_test_3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements Camera.PreviewCallback, SurfaceHolder.Callback {

    private Button connectBtn, testBtn, startBtn;
    private ImageView cameraImageView, receiveImageView;
    private SurfaceView cameraView;

    private Camera mCamera;

    private TCPClient tcpClient;
    private final String ADDRESS = "202.125.228.170";
    private final int PORT = 8080;

    private int FPS = 30;
    private long lastTime = 0;

    private boolean stream = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = (SurfaceView)findViewById(R.id.cameraSurfaceView);
        SurfaceHolder camHolder = cameraView.getHolder();
        camHolder.addCallback(this);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        findViewById(R.id.connectBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread("Connect") {
                    public void run () {
                        MainActivity.this.tcpClient.connect();

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (MainActivity.this.tcpClient.isConnect()) {
                                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }.start();

            }
        });

        findViewById(R.id.testBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.this.tcpClient.isConnect()) {
                    MainActivity.this.tcpClient.test();
                } else {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        findViewById(R.id.streamBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.this.tcpClient.isConnect()) {
                    MainActivity.this.stream = !MainActivity.this.stream;

                    Button button = (Button) view;
                    if (MainActivity.this.stream) {
                        button.setText("Stop");
                    } else {
                        button.setText("Start");
                    }
                } else {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        receiveImageView = (ImageView)findViewById(R.id.imageView);

        tcpClient = new TCPClient(ADDRESS, PORT, this);
    }

    public void setImage(final byte[] data) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receiveImageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
            }
        });
    }

    @Override
    public void onPreviewFrame(final byte[] bytes, Camera camera) {
        if (stream) {
            if (new Date().getTime() - lastTime > (long) (1000/30)) {
                final byte[] image = bytes.clone();
                new Thread("Frame Thread") {
                    public void run () {
                        Camera.Parameters paras = mCamera.getParameters();
                        int width = paras.getPreviewSize().width;
                        int height = paras.getPreviewSize().height;

                        YuvImage yuvImage = new YuvImage(image, paras.getPreviewFormat(), width, height, null);

                        ByteArrayOutputStream out = new ByteArrayOutputStream();

                        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out);

                        MainActivity.this.tcpClient.send(out);
                    }
                }.start();
                lastTime = new Date().getTime();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.setPreviewCallback(this);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}