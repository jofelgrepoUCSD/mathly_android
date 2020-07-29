package com.example.mathly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.icu.text.UnicodeSetSpanner;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.INotificationSideChannel;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mathly.data.Post;
import com.example.mathly.data.remote.APIService;
import com.example.mathly.data.remote.ApiUtils;
import com.example.mathly.data.remote.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.mathly.data.DataOptions;

public class ScanImage extends AppCompatActivity {

    public static final int REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;
    private APIService mAPIService;
    ImageView selectedImage;
    Button camera_button,gallery_button,send_button,retake_button;
    TextView scan1,scan2,useorretake;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_image2);

        selectedImage = findViewById(R.id.imageView);
        camera_button = findViewById(R.id.camera_button);
        gallery_button = findViewById(R.id.gallery_button);
        gallery_button = findViewById(R.id.gallery_button);
        send_button = findViewById(R.id.send);
        retake_button = findViewById(R.id.retake);
        scan1 = findViewById(R.id.scanorupload);
        scan2 = findViewById(R.id.scanorupload2);
        useorretake = findViewById(R.id.useretake_tv);

        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCameraPermission();

            }
        });



        gallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Intent gallery = new Intent (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
              startActivityForResult(gallery,GALLERY_REQUEST_CODE);


            }
        });


    }
    public void askCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA} , 101);
        } else {
            //openCamera();
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101 ){
            if(grantResults.length > 0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                //openCamera();
                dispatchTakePictureIntent();

            } else {
                Toast.makeText(ScanImage.this,"Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    public void openCamera () {
//        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(camera, REQUEST_CODE);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode,resultCode,data);


        /**
         *  Where user hit the scan button it then encodes it to  it to base64
         *  TODO: put this string to JSON?
         *        make a POST request ???
         */
        if(requestCode == REQUEST_CODE ){

            File f = new File(currentPhotoPath);
            selectedImage.setImageURI(Uri.fromFile(f));
            Log.d("tag","Absolute Url of Image is " + Uri.fromFile(f));
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
            selectedImage.setVisibility(View.VISIBLE);
            send_button.setVisibility(View.VISIBLE);
            camera_button.setVisibility(View.INVISIBLE);
            gallery_button.setVisibility(View.INVISIBLE);
            retake_button.setVisibility(View.VISIBLE);
            scan1.setVisibility(View.INVISIBLE);
            scan2.setVisibility(View.INVISIBLE);
            useorretake.setVisibility(View.VISIBLE);
            try {
                final InputStream imageStream = getContentResolver().openInputStream(contentUri);
                final Bitmap bm = BitmapFactory.decodeStream(imageStream);
                final String encodedImage = encodeImge(bm);
                System.out.println("Worked");
//                System.out.println(encodedImage);
                System.out.println("End");


//                mAPIService = ApiUtils.getAPIService();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://jsonplaceholder.typicode.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                mAPIService  = retrofit.create(APIService.class);

                send_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!TextUtils.isEmpty(encodedImage)){
                            createPost(encodedImage);
                        }
                    }
                });

            } catch (FileNotFoundException e) {

                e.printStackTrace();
            }

        }
        if(requestCode == GALLERY_REQUEST_CODE ){

            Uri contentUri = data.getData();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName ="JPEG_" + timeStamp + "." +getFileExt(contentUri);
            Log.d("tag","Gallery Image Uri is: " + imageFileName);
            selectedImage.setImageURI(contentUri);

        }
    }
    public void createPost(String encodedImage){

        List<String> list = new ArrayList<String>();
        list.add("text");
        list.add("data");
        list.add("html");

        DataOptions dataOptions = new DataOptions(true,true);
        Post post = new Post(encodedImage,list, dataOptions );

        mAPIService.createPost(post).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {

                if(response.isSuccessful()){
                    System.out.println(response.body().toString());
                    System.out.println("BITCH");
                    Log.i("tag","posted!" + response.body().toString());
                }

            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {

                System.out.println("Failed to post it");
                Log.e("tag", "YOU FAIL");
                t.printStackTrace();

            }
        });


    }
    
    private String getFileExt(Uri contentUri) {

        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));

    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG" + timeStamp + "_";
     // File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg",storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null ){
            File photoFile = null;
            try{
                photoFile = createImageFile();
            } catch (IOException ex){

            }
            if (photoFile != null ){
                Uri photoUri = FileProvider.getUriForFile(this,"com.example.mathly.android.fileprovider",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_CODE);
            }
        }


    }
    private String encodeImge(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b,Base64.DEFAULT);

        return encImage;
    }


}
