package com.android.andopencvyuztanima;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


public class OpenCVCamera extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {


    final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);


    public static boolean gizle=true;
    public static int  kayma_faktor=0;
    public static int aci=0;
    public static boolean btDurum=false;

    // Bluetooth Kısmı **********************************
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update


    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    TextView btText;

    //*********************************************************************************************************



    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private EditText editText;
    private ImageView micButton;


    TextToSpeech textToSpeech;


    private static final String TAG = "MainActivity";
    Mat mRGBA;
    Mat mRGBAT;
    CascadeClassifier cascadeClassifier;

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {

                    Log.i(TAG, "OpenCV Camera Activity Yüklendi...");

                    cameraBridgeViewBase.setCameraIndex(1);//Ön Kamera
                    cameraBridgeViewBase.enableView();
                    cameraBridgeViewBase.setMaxFrameSize(640, 480);// Çözünürlük Düşürme....
                }
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(OpenCVCamera.this,
                new String[]{Manifest.permission.CAMERA}, 1);



        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_open_cvcamera);

        //******************************** Text To Speech Kısmı ****************************

        // create an object textToSpeech and adding features into
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if (i != TextToSpeech.ERROR) {
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.getDefault());
                }
            }
        });

        //*********************************************************************


        // Konuşma Tanıma Kısmı **************************************************
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }

        editText = findViewById(R.id.text);
        editText.setVisibility(View.INVISIBLE);
        micButton = findViewById(R.id.button);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);


        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                editText.setText("");
                editText.setHint("Dinliyorum...");

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                micButton.setImageResource(R.drawable.ic_mic_black_off);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                String text=data.get(0);

                Toast toast= Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG);
                toast.show();

                if (text.equalsIgnoreCase("kamerayı göster")){
                    gizle=false;

                }
                if (text.equalsIgnoreCase("kamerayı gizle")){
                    gizle=true;
                }

                textToSpeech.speak(data.get(0),TextToSpeech.QUEUE_FLUSH,null);



            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });


        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                editText.setVisibility(View.VISIBLE);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    micButton.setImageResource(R.drawable.ic_mic_black_24dp);
                    textToSpeech.speak("Konuş Dinliyorum",TextToSpeech.QUEUE_FLUSH,null);
                    SystemClock.sleep(750);
                        speechRecognizer.startListening(speechRecognizerIntent);

                }
                return false;
            }
        });


        // Konuşma Tanıma Kısmı  **************************************************************************


        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.camera_surface);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        // Haar Cascade Modelini Yüklemek için...

        try {
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE); // klasör oluşturur....
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");// oluşturulan klasörde dosyayı oluşturur...
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int byteRead;

            // Oluşturulan dosyayı raw klasörüne yazar...
            while ((byteRead = is.read(buffer)) != -1) {

                os.write(buffer, 0, byteRead);
            }
            is.close();
            os.close();

            // Yukarıda oluşturulan Cascade klasöründeki dosyayı(modeli) yükler....
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());

        } catch (IOException e) {
            Log.i(TAG, "Cascade XML Dosyası Bulunamadı...");
        }


        // ******************** Bluetooth Kısmı *******************************************************************************************
        // Bluetooth Setup ********************************************************************************************************
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btText=(TextView) findViewById(R.id.textBluetooth);
        // Get List of Paired Bluetooth Device

        /*
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<Object> deviceList = new ArrayList<>();


        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);
        listView.setAdapter(adapter);
        */

        createConnectThread = new CreateConnectThread(bluetoothAdapter,"20:18:07:13:55:10");
        createConnectThread.start();



  /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                btText.setText("Bağlandı.... ");
                                btDurum=true;

                                break;
                            case -1:
                                btText.setText("Bağlanamadı.... ");
                                btDurum=false;

                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino

                        /*
                        switch (arduinoMsg.toLowerCase()){
                            case "led is turned on":
                                imageView.setBackgroundColor(getResources().getColor(R.color.colorOn));
                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                break;
                            case "led is turned off":
                                imageView.setBackgroundColor(getResources().getColor(R.color.colorOff));
                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
                                break;
                        }
                        */

                        break;
                }
            }
        };




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Eğer request (izin) verilmediyse boş bir dizi(array) döndürür

        switch (requestCode) {

            case 1: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraBridgeViewBase.setCameraPermissionGranted();
                } else {
                    // Cameraya izin Verilmedi
                }


            }
            if (requestCode == RecordAudioRequestCode && grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
            }
            return;
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        // Eğer başarıyla init olduysa
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OnResume OpenCV Inıt Oldu...");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "OnResume OpenCV Inıt Olamadı...");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }

        speechRecognizer.destroy();


    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        mRGBA = new Mat(height, width, CvType.CV_8UC4);
        mRGBAT = new Mat(height, width, CvType.CV_8UC1);

    }

    @Override
    public void onCameraViewStopped() {

        mRGBA.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRGBA = inputFrame.rgba();
        mRGBAT = inputFrame.gray(); // Gri Scale Resim

        // Resmi Griye Çevirme
        // return mRGBAT;
        // veya
        // Imgproc.cvtColor(mRGBA,mRGBA,Imgproc.COLOR_RGBA2GRAY);

        //Cascade Rec nesnesini mRGBA için işleme....
        mRGBA = CascadeRec(mRGBA);

        return mRGBA;

    }

    private Mat CascadeRec(Mat mRGBA) {
        // Örjinal Frame -90 dır bunu flip yapmalıyız..
        //Imgproc.resize(mRGBA, mRGBA, new Size(640,480));
        Core.flip(mRGBA.t(), mRGBA, -1); //**************************

        //RGB'ye dönüştürmek gerek...
        Mat mRGB = new Mat();
        Imgproc.cvtColor(mRGBA, mRGB, Imgproc.COLOR_RGBA2RGB);

        int yukseklik = mRGB.height();
        // framedeki mininmum yüz boyutu
        int absoluteFaceSize = (int) (yukseklik * 0.1);
        MatOfRect yuzler = new MatOfRect();
        if (cascadeClassifier != null) {

            //                                 giriş,çıkış                                  //minimum çıkış boyutu
            cascadeClassifier.detectMultiScale(mRGB, yuzler, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }

        // Tüm yüzlerin döngüsü
        Rect[] yuzlerDizisi = yuzler.toArray();
        Point Yuz1 = new Point();
        Point Yuz2 = new Point();

        //btText.setText("Yuz="+yuzlerDizisi.length);



            for (int i = 0; i < yuzlerDizisi.length; i++) {
                // Yüzleri Ekrana Çiz
                Imgproc.rectangle(mRGBA, yuzlerDizisi[i].tl(), yuzlerDizisi[i].br(), new Scalar(0, 255, 0, 255), 2);

                Yuz1 = yuzlerDizisi[i].tl();
                Yuz2 = yuzlerDizisi[i].br();
            }


            // Kamerayı Gizeleme
            if (gizle) {
                Imgproc.rectangle(mRGBA, new Point(0, 0), new Point(mRGBA.width(), mRGBA.height()), new Scalar(0, 0, 0), Imgproc.FILLED);
                //btText.setText(" True");
            } else {
                //btText.setText(" False");
            }

        int yuzX = (int) (Yuz2.x - Yuz1.x);
        int yuzY = (int) (Yuz2.y - Yuz1.y);


        // Eğer Yüz Yoksa
        if(yuzlerDizisi.length==0) {

            if (btDurum==true) {
                connectedThread.write("b");
            }

            RotatedRect sag = new RotatedRect(new Point(mRGBA.width() / 2 - 120, mRGBA.height() / 2 - 160), new Size(160, 60), 180);
            RotatedRect sol = new RotatedRect(new Point(mRGBA.width() / 2 + 120, mRGBA.height() / 2 - 160), new Size(160, 60), 180);


            Imgproc.ellipse(mRGBA, sag, new Scalar(255, 255, 255), Imgproc.FILLED);
            Imgproc.ellipse(mRGBA, sol, new Scalar(255, 255, 255), Imgproc.FILLED);

            Imgproc.ellipse(mRGBA, sag,  new Scalar(189, 0, 0), 3);
            Imgproc.ellipse(mRGBA, sol, new Scalar(189, 0, 0), 3);

            yuzX+=kayma_faktor-70;
            aci+=30;
            kayma_faktor+=50* Math.sin(Math.toRadians(aci));







            // Göz Bebeği
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 - 120) + (yuzX / 4)), ((mRGBA.height() / 2 - 160) - (yuzY / 4))), 20, new Scalar(150, 63, 24), Imgproc.FILLED);
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 + 120) + (yuzX / 4)), ((mRGBA.height() / 2 - 160) - (yuzY / 4))), 20, new Scalar(150, 63, 24), Imgproc.FILLED);

            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 - 120) + (yuzX / 4)), ((mRGBA.height() / 2 - 160) - (yuzY / 4))), 20, new Scalar(255, 255, 255), 2);
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 + 120) + (yuzX / 4)), ((mRGBA.height() / 2 - 160) - (yuzY / 4))), 20, new Scalar(255, 255, 255), 2);


            // Göz Merceği
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 - 120) + (yuzX / 4)), ((mRGBA.height() / 2 - 160) - (yuzY / 4))), 5, new Scalar(0, 0, 0), Imgproc.FILLED);
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 + 120 )+ (yuzX / 4)), ((mRGBA.height() / 2 - 160) - (yuzY / 4))), 5, new Scalar(0, 0, 0), Imgproc.FILLED);

            // Göz Bebeği Işık Yansıması
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 - 120) + (yuzX / 4)) - 10, ((mRGBA.height() / 2 - 160) - (yuzY / 4)) - 10), 2, new Scalar(255, 255, 255), Imgproc.FILLED);
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 + 120) + (yuzX / 4)) - 10, ((mRGBA.height() / 2 - 160) - (yuzY / 4)) - 10), 2, new Scalar(255, 255, 255), Imgproc.FILLED);



            //Ağız

            Imgproc.line(mRGBA,new Point(((mRGBA.width() / 2 - 100) ) - 10,(mRGBA.height() / 2 + 200)),
                    new Point(((mRGBA.width() / 2 + 100) ) - 10,(mRGBA.height() / 2 + 200)),new Scalar(255, 255, 255),3);



        }
        else if(yuzlerDizisi.length>0) {


            //textToSpeech.speak("Selam",TextToSpeech.QUEUE_FLUSH,null);
            //SystemClock.sleep(750);
            //speechRecognizer.startListening(speechRecognizerIntent);


            if (yuzX != 0) {
                yuzX = (mRGBA.width() / 2 - ((int) (Yuz1.x) + (int) (Yuz2.x - Yuz1.x) / 2));
                yuzY = (mRGBA.height() / 2 - ((int) (Yuz1.y) + (int) (Yuz2.y - Yuz1.y) / 2));



            }

            //Dış Göz
            Imgproc.circle(mRGBA, new Point(mRGBA.width() / 2 - 100, mRGBA.height() / 2 - 160), 80, new Scalar(255, 255, 255), Imgproc.FILLED);
            Imgproc.circle(mRGBA, new Point(mRGBA.width() / 2 + 100, mRGBA.height() / 2 - 160), 80, new Scalar(255, 255, 255), Imgproc.FILLED);
            //#FF99E6
            Imgproc.circle(mRGBA, new Point(mRGBA.width() / 2 - 100, mRGBA.height() / 2 - 160), 80, new Scalar(189, 0, 0), 3);
            Imgproc.circle(mRGBA, new Point(mRGBA.width() / 2 + 100, mRGBA.height() / 2 - 160), 80, new Scalar(189, 0, 0), 3);


            // Göz Bebeği
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 - 100) + (yuzX / 4)), ((mRGBA.height() / 2 - 160) - (yuzY / 4))), 40, new Scalar(150, 63, 24), Imgproc.FILLED);
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 + 100) + (yuzX / 4)), ((mRGBA.height() / 2 - 160) - (yuzY / 4))), 40, new Scalar(150, 63, 24), Imgproc.FILLED);

            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 - 100) + (yuzX / 4)), ((mRGBA.height() / 2 - 160) - (yuzY / 4))), 40, new Scalar(255, 255, 255), 2);
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 + 100) + (yuzX / 4)), ((mRGBA.height() / 2 - 160) - (yuzY / 4))), 40, new Scalar(255, 255, 255), 2);


            // Göz Merceği
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 - 100) + (yuzX / 4)), ((mRGBA.height() / 2 - 160) - (yuzY / 4))), 10, new Scalar(0, 0, 0), Imgproc.FILLED);
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 + 100) + (yuzX / 4)), ((mRGBA.height() / 2 - 160) - (yuzY / 4))), 10, new Scalar(0, 0, 0), Imgproc.FILLED);

            // Göz Bebeği Işık Yansıması
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 - 100) + (yuzX / 4)) - 10, ((mRGBA.height() / 2 - 160) - (yuzY / 4)) - 10), 2, new Scalar(255, 255, 255), Imgproc.FILLED);
            Imgproc.circle(mRGBA, new Point(((mRGBA.width() / 2 + 100) + (yuzX / 4)) - 10, ((mRGBA.height() / 2 - 160) - (yuzY / 4)) - 10), 2, new Scalar(255, 255, 255), Imgproc.FILLED);




            Imgproc.ellipse(mRGBA,new Point(((mRGBA.width() / 2 ) ) - 10,(mRGBA.height() / 2 + 150)),
                    new Size(140,50),180.0,-30,-150, new Scalar(189, 0, 0),3);

            Imgproc.ellipse(mRGBA,new Point(((mRGBA.width() / 2 ) ) - 10,(mRGBA.height() / 2 + 135)),
                    new Size(140,80),180.0,-30,-150, new Scalar(189, 0, 0),3);




            int gbDeger=((mRGBA.width() / 2 - 120) + (yuzX / 4));

            btText.setText(String.valueOf(gbDeger));

            if (gbDeger>140 && gbDeger<160) {
                if (btDurum==true)
                {
                    connectedThread.write("i");
                    connectedThread.write("r");
                }
            }




        }

        // tekrar -90 derece döndürmeli
        Core.flip(mRGBA.t(), mRGBA, -1); //*******************



        return mRGBA;
    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);



        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        }
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }

        */

    }


///*********************************Bluetooth Kısmı ****************************************



    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }







    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }




}