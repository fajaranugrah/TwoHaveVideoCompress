package com.example.twohavevideocompress.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.twohavevideocompress.MainActivity;
import com.example.twohavevideocompress.R;
import com.github.tcking.giraffecompressor.GiraffeCompressor;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

import static com.github.tcking.giraffecompressor.GiraffeCompressor.TYPE_FFMPEG;
import static com.github.tcking.giraffecompressor.GiraffeCompressor.TYPE_MEDIACODEC;

public class MainGiraffe extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static String mCurrentVideoPath;
    Button button, button2, button3;
    TextView text_video, after_text_video;
    VideoView video_play, video_already_compress;
    final private int SELECT_VIDEO_REQUEST_CODE = 129;
    final private int SELECT_VIDEO_REQUEST_CODE_2 = 130;
    final private int REQUEST_CAMERA_VIDEO_PERMISSION = 128;
    String[] PERMISSIONS_VID = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final String FILE_PROVIDER_AUTHORITY = ".provider";
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_VIDEO = 2;

    public static final int MEDIA_TYPE_VIDEO = 2;
    Uri fileUri;
    String mCurrentPhotoPath;
    Uri capturedUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress_main_video);

        Toolbar toolbar = findViewById(R.id.toolbar_video);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button3.setVisibility(View.GONE);
        text_video = (TextView) findViewById(R.id.text_video);
        text_video.setVisibility(View.GONE);
        after_text_video = (TextView) findViewById(R.id.after_text_video);
        after_text_video.setVisibility(View.GONE);
        video_play = (VideoView) findViewById(R.id.video_play);
        video_already_compress = (VideoView) findViewById(R.id.video_already_Compress);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hasPermissions(MainGiraffe.this, PERMISSIONS_VID)){
                    ActivityCompat.requestPermissions(MainGiraffe.this, PERMISSIONS_VID, REQUEST_CAMERA_VIDEO_PERMISSION);
                } else {
                    startVideo();
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File files = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/PetBacker/videos");
                final Long tsLong = System.currentTimeMillis();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                Date date = new Date();
                String timeStamp = dateFormat.format(date);
                String NameVideo = files + "_" + timeStamp + "_" + tsLong.toString() + ".mp4";

                GiraffeCompressor.create(TYPE_MEDIACODEC)// ? TYPE_MEDIACODEC : TYPE_FFMPEG) //two implementations: mediacodec and ffmpeg,default is mediacodec
                        .input(mCurrentPhotoPath) //set video to be compressed
                        .output(NameVideo) //set compressed video output
                        .bitRate(12200)//set bitrate 码率 2073600
                        .resizeFactor(1)//set video resize factor 分辨率缩放,默认保持原分辨率
                        //.watermark("/sdcard/videoCompressor/watermarker.png")//add watermark(take a long time) 水印图片(需要长时间处理)
                        .ready()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<GiraffeCompressor.Result>() {
                            @Override
                            public void onCompleted() {
                                //$.id(R.id.btn_start).enabled(true).text("start compress");
                                Log.e("checkVideoCompleted", "yeah completed");
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                //$.id(R.id.btn_start).enabled(true).text("start compress");
                                //$.id(R.id.tv_console).text("error:"+e.getMessage());
                                Log.e("checkVideoError", "error : " + e.getMessage());

                            }

                            @Override
                            public void onNext(GiraffeCompressor.Result s) {
                                String msg = String.format("compress completed \ntake time:%s \nout put file:%s", s.getCostTime(), s.getOutput());
                                msg = msg + "\ninput file size:"+ Formatter.formatFileSize(getApplication(),new File(mCurrentPhotoPath).length());
                                msg = msg + "\nout file size:"+ Formatter.formatFileSize(getApplication(),new File(s.getOutput()).length());
                                System.out.println(msg);
                                after_text_video.setVisibility(View.VISIBLE);
                                after_text_video.setText(msg);

                                video_already_compress.setVisibility(View.VISIBLE);

                                video_already_compress.setVideoPath(s.getOutput());
                                final MediaController mediaController = new MediaController(MainGiraffe.this);
                                video_already_compress.setMediaController(mediaController);

                                video_already_compress.requestFocus();
                                video_already_compress.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        video_already_compress.seekTo(1);
                                    }
                                });
                            }
                        });
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(MainGiraffe.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_silli) {
            Intent intent = new Intent(MainGiraffe.this, MainSilli.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_giraffe) {
            Intent intent = new Intent(MainGiraffe.this, MainGiraffe.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_VIDEO_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    startVideo();
                } else {

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode){
                case SELECT_VIDEO_REQUEST_CODE:
                    Uri videoUri = data.getData();
                    video_play.setVideoPath(videoUri.toString());
                    //add control play or stop or pause
                    final MediaController mediaController = new MediaController(MainGiraffe.this);
                    video_play.setMediaController(mediaController);

                    video_play.requestFocus();
                    video_play.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            video_play.seekTo(1);
                        }
                    });


                    Log.d( "mCurrentPhotoPathResu", videoUri.getPath());
                    File imageFile = new File(mCurrentPhotoPath);
                    long length = imageFile.length();
                    length = length / 1024; // Size in KB
                    String value;
                    if (length >= 1024)
                        value = length / 1024 + " MB";
                    else
                        value = length + " KB";
                    String text = String.format(Locale.US, "%s\nName: %s\nSize: %s", "video original ", imageFile.getName(), value);
                    text_video.setVisibility(View.VISIBLE);
                    text_video.setText(text);
                    break;
            }
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void startVideo(){
        /*try {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // set video quality if 1 = high quality video and 0 = low quality
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            // set video max record 15 sec
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,15);
            // set size record video
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 2097152);// 2*1024*1024 = 2MB
            if (intent.resolveActivity(getPackageManager()) != null) {
                // start the video capture Intent
                startActivityForResult(intent, SELECT_VIDEO_REQUEST_CODE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/

        /*Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // set video quality, 0 = low quality and then 1 = high quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        // set video max record 15 sec
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,15);
        if (intent.resolveActivity(getPackageManager()) != null) {
            // create a file to save the video
            fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO, MainGiraffe.this);
            Log.d("VideoUri:"," " + fileUri.toString());

            // set the image file name
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            if (Build.VERSION.SDK_INT > 24){
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            // start the video capture Intent
            startActivityForResult(intent, SELECT_VIDEO_REQUEST_CODE);
        }*/

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            try {
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,15);
                capturedUri = FileProvider.getUriForFile(this,
                        getPackageName() + FILE_PROVIDER_AUTHORITY,
                        createMediaFile(TYPE_VIDEO));

                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedUri);
                startActivityForResult(takeVideoIntent, SELECT_VIDEO_REQUEST_CODE);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private File createMediaFile(int type) throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = (type == TYPE_IMAGE) ? "JPEG_" + timeStamp + "_" : "VID_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                type == TYPE_IMAGE ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_MOVIES);
        File file = File.createTempFile(
                fileName,  /* prefix */
                type == TYPE_IMAGE ? ".jpg" : ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Get the path of the file created
        mCurrentPhotoPath = file.getAbsolutePath();
        Log.d( "mCurrentPhotoPath: ", mCurrentPhotoPath);
        return file;
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type, Activity activity){

        if (Build.VERSION.SDK_INT > 24){
            //try {
            return FileProvider.getUriForFile(activity,
                    activity.getApplicationContext().getPackageName() + FILE_PROVIDER_AUTHORITY, getOutputMediaFile(type, activity));
            /*} catch (IOException e) {
                e.printStackTrace();
                return null;
            }*/
        } else {
            return Uri.fromFile(getOutputMediaFile(type, activity));
        }
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type, Activity activity){

        // Check that the SDCard is mounted
        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);


        // Create the storage directory(MyCameraVideo) if it does not exist
        if (! mediaStorageDir.exists()){

            if (! mediaStorageDir.mkdirs()){

                String output = "Failed to create directory Video.";
                Log.e("output", output);

                Toast.makeText(activity, "Failed to create directory MyCameraVideo.",
                        Toast.LENGTH_LONG).show();

                Log.d("Video", "Failed to create directory MyCameraVideo.");
                return null;
            }
        }


        // Create a media file name

        // For unique file name appending current timeStamp with file name
        java.util.Date date= new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());

        File mediaFile;

        if(type == MEDIA_TYPE_VIDEO) {

            // For unique video file name appending current timeStamp with file name
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");

        } else {
            return null;
        }

        return mediaFile;
    }
}