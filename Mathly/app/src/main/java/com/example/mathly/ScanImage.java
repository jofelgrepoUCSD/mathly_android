package com.example.mathly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
//import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.amazonaws.util.Base64;
import com.example.mathly.data.Post;
import com.example.mathly.data.remote.APIService;

import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.io.FileUtils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.mathly.data.DataOptions;
import com.example.mathly.data.remote.ApiUtils;

public class ScanImage extends AppCompatActivity {

    public static final int REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;
    private APIService mAPIService;
    LinearLayout initial_layout, result_layout, top_layout;
    ImageView selectedImage;
    ImageButton camera_button,gallery_button,send_button,retake_button;
//    Button camera_button,gallery_button,send_button,retake_button;
    TextView scan1,scan2,useorretake,tv1,tv2;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_image2);

        selectedImage = findViewById(R.id.new_image_view);
        camera_button = findViewById(R.id.scan_button);
        gallery_button = findViewById(R.id.upload_button);
        send_button = findViewById(R.id.send_button);
        retake_button = findViewById(R.id.send_button);

        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);

        initial_layout = findViewById(R.id.allcontent);
        result_layout = findViewById(R.id.layout_result);
        top_layout = findViewById(R.id.top_layout);



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
            tv1.setVisibility(View.INVISIBLE);

            initial_layout.setVisibility(View.INVISIBLE);
            result_layout.setVisibility(View.VISIBLE);
            top_layout.setVisibility(View.VISIBLE);



            try {
                final InputStream imageStream = getContentResolver().openInputStream(contentUri);
                final Bitmap bm = BitmapFactory.decodeStream(imageStream);
                String encodedImage = encodeImge(bm);
               // String encodedImages = apachebase64(currentPhotoPath);
              //  String encodedImage = encodeFilePath(f);


                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://qonbfsyvbf.execute-api.us-west-1.amazonaws.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();


                mAPIService  = retrofit.create(APIService.class);

                String practice = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAAXNSR0IArs4c6QAAADhlWElmTU0AKgAAAAgAAYdpAAQAAAABAAAAGgAAAAAAAqACAAQAAAABAAAAQKADAAQAAAABAAAAQAAAAABlmWCKAAAIZElEQVR4Ad1beWxURRifmV1ajlYDgi2h5SgtGMohcnS7bbEGxB4JELWCkCDIYQiXaLiKJiScGiMF9B8wGqKJQk08YrcFMUJLdyloBaQRaUVuloIcUkop7Bu/77W7bh9v983b7tlJdt+bed988/2+mfm+b+bNoyTAyWye+KSDsZeZRAdxSnpSQnrhFZrtRTjFKyGUX4f/a5ST67z1KjF+2iBJX1ut++plmgD9gTz+TwiaM2MBJ7wAQGZRSpgvrXBOJFBOBfDZEyhl+FUB6Zm5MwHwHELJOF8Aa9bhpFzi0s4qa9kXmrSCBH5RgCkjZxJ08gYAPlSw3XaRwYj4XZKk1Uese0vaxQgqt0sBpsy8NMJ5EaXU1F5BfKkPiqikDrbCZiux+lIf6/ikgFGjRnWK6hy3BWov9LVhf9bjnH9051bM2zU1xc16+epWwJjM/CQDGCWoOEpvY4GkB+9xhDocM2y2vXV62tFlndPMeZONRDoebuARMMg0ljBDNcj4kh4FCI2A7OzszvcfdP0gXIa8FkCcElS6BbbBdk+LVlMBOOSNXPqeUJqqxSycnqOnoA7pRa0poTkFjIRvjjTw2BGU0GGcsfVanWLwRpCWmbMKGC3xRhPOz8A9D+3Td+D9S+frDnmS0+MUSDPnTgQGpb6GsZ4aDHY5htOAId92yFKm1raqAsZmvjCCcVYOCnhMrVKklYE9uO2QaPZRq+WYUnZVG8AI+7yjgEfAMI0fN1KyVQke848oIM2cMw0NiBpxRJfBAi09PXeKEoNSAYwytlFJ1FHynNG1gKUN5jYZkzlvJhiFAR0FsBIHGMMRiNG93GUEk5Nzo3vG0z86sgJagZ+5ZudD6upK72PeNQJ6xZEFkQY+K9NE5syeTlJSktw7Ves+CbE6iYzOG04pGL/ISAj4ndXLXMDnvD6DlJTuJxs2bhECgFiBsAiJ5UhwzJi8eGbgH4LrC3sdIPiPt20mvXvHtQE7CMrjoayi4nCbctUM533i+g/cceX8Xw3yFDBEkSmRBD4mppsqrvzcCa5RoUrQWohYO0mGyZiVFUApf8Q/emMQimfOnvcE3inTuKx0563XqxMzy8iYFMs5fc4rdYgfioJHMe32q0LSImbc2jM+5M35jLEooVohIMrPm0CWLp5PtHoeRbPb68nBcpuQlGDtoqK6xI03QuT3lFCNEBAh+DVg7UVSw927ZGXhOtLQcFeEXKaBfUQTg9dR8cI1gkioF/zCxatIbe0ZXRIidjSCYaeAYIBv1VQ8gxeVYaWAIIKHt3gwAsJpCgQTPI6AlilAuU8jACMxdE/+SsEGL8sN2I1gCZsg/o0WBYLuaPPGd8kzI1v2TK6A392wsYhU/3ZClMUjdLigwXheJKGrQ2uv1+Cp8UbshsTElFmw7d1yUEGNyq0MwWMcPjT1f88ZGxND8iAEtV+t90moNYXLyNRXxALR2rq/ybw33pL9vZtYPt/CFDiHRtAuymEu9JKnYY/+GsHoSUiP8btIQvALF6/U5ee1+CJ2Rri4ArKyTF55IhhRJYQavAwEsKMXEB4BsbExXhWAD1EJuz7d7jV0DQvwICtiZ3AYSVgB5YJxtnPxolyzo4LCBTzKAvvlZxm8AzgmZwT+irbtIDgXRRIqAUeCu80IK/AIgtJTNDW1ICq2e0M9vjwQASZ7gu3vkZTkASLkstFaD1tVaD9CafCUwsIr9H8fNNXjsT1YEmXkfgWbJFOVRJ7yqIQ3l8yX3Z8nGl/LLbC3hwoLdAIF7D5cWTpN3hGCoLhMT4O45EQhUVh/pmCBl2VuxSxviiYmDL7GqbRM775gOWxANtxtJKa09h8XCiZ46H3ueEAXXL5c2yAr4OLF03cS+g3KhfmQoLdHa2pOyVGg6F6cGv9ggpfbp/TIkcMWeVu8ZQq0SKVrGrgDKbHsJ4uWrIbRIL4b46wfdPDQMBwZ+NbZvksB1+18EywOxHycs7bbFRdDuCujRwkhAc/52X/s1GVlXUdkbtyocyQkpNyGzUKxlYkbeOftjRs3yf6fymGlOJw80aO7s1j1GgrwsiCcLjtxvPRXp1CyG3Rm4MpMmbnH2ns+QCtW2Lp9J9m9xzUK3ZoP8C0nJ22VlhHQiuRsyTUFWgskAL/C+dDXK7rJ12YvInuKv2vDAqfHhk1bQgMeJaFkOfy7wLcU4b8iwSg4AIp4VlHsUxanw8jWzRMc9leuiL248KkxL5XgnNDBw4dKs5UkyikgPx+dkTPYSGmVaHisZBpueQx7+cOHo6uqfqxVyqacAvLzXyrL/oRK0+UvNpQ1IiyPGOBXoAYeobi8gBLXpQt1tX36DWyGUSC2ZaNkECZ5iHALqyrLdnkSx6MCsAKesExITB4EIXKknhorhnm/1BN4LFedAu4V4NT1XDAg1e5lEXHPybFoY2ObA1FqcmsqAI+cU8etTIgf2/o0NW7hU/ZDdKfG9AMHDjRpiaTqBTxUouAeC0ER6/SuGj3w83sxrvIA0FpbZek6YA6RvXbSowCZG34hBjPnSwiZu2qzDx4FWPp7EncU6P2STLcCENIYc97TBsq/gZHQH/MhT/CCQ2LSpKqKMt2vp7x6AU/ALl+otffonvRJVBS7BWNuGGhRe7/cE7N2lEOvX4XP9tY1NjhmVR/de9EXVj6NAPeG8HuipuYu88CfLIeYIdH9WQDvz8AMfx8M3S4RQ+dNjnYrwJ05fLE1G2zDq/B73r3cP/e8CXr8ZzBzxVVWy2f+4YnrowCk4cMndusay8aDjcgHU5wHI0P3VhuKBfHHRRDQIknUwvjNfSJfgemFExAFKIUwm/OHSpQPgcb6wbN+AKwvqF6+b6U9B0o6D4jPQf4cPD8PX6zUWK0lJ5W8/J3/D0sRF6eisGLBAAAAAElFTkSuQmCC";



                send_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(!TextUtils.isEmpty(practice)){
                            System.out.println("Here");
                            createPost(practice);
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


//            try {
//                final InputStream imageStream = getContentResolver().openInputStream(contentUri);
//                final Bitmap bm = BitmapFactory.decodeStream(imageStream);
//               // final String encodedImage = encodeImge(bm);
//
//                System.out.println("On gallery");
//                //   System.out.println(encodedImage);
//                System.out.println("End");
//                String baseconnect = "data:image/jpeg;base64,";
//                System.out.println(encodedImage);
//
//                send_button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        if(!TextUtils.isEmpty(encodedImage)){
//                            System.out.println("Here");
//                            createPost(encodedImage);
//                        }
//                    }
//                });
//
//
//            } catch (FileNotFoundException e) {
//
//                e.printStackTrace();
//            }



        }
    }
    public void createPost(String encodedImage){

        Post post = new Post("first post12345",encodedImage,"type",69);
        Call<Post> call = mAPIService.createPost(post);

        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if(!response.isSuccessful()){
                    System.out.println("Code:" + response.code());
                }
                System.out.println("Code :" + response.code());
                if(response.isSuccessful()){
                    System.out.println("Bitch");
                }

            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {

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
    private String encodeImge(Bitmap bm) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,10,baos);
        byte[] b = baos.toByteArray();
        String encImage = android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT);
        encImage = "data:image/jpeg;base64," + encImage;
        System.out.println(encImage);

        return encImage;

    }
    // Tried Jeremey's link 1
    private String apachebase64(String path){

        String encodedString = "";
        try {
            byte [] fileContent = org.apache.commons.io.FileUtils.readFileToByteArray(new File(path));
            encodedString = Base64.encodeBase64String(fileContent);
            System.out.println(encodedString);

          } catch (Exception e) {
              return null;
        }

        return encodedString;
    }
    // Tried Jeremy's Link 2
    private String encodeFilePath(File file){

        String encodedfile = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = new String(java.util.Base64.getEncoder().encodeToString(bytes));
            System.out.println(encodedfile);
          //  encodedfile = Base64.encodeBase64(bytes).toString();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return encodedfile;
    }



}
