package com.example.s3_bucket;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST=2;
    private Uri filePath;
    private File file;
    private Uri videoPath;
    private File videoFile;
    private ImageView imageView;
    private BasicAWSCredentials creds;
    private AmazonS3Client s3Client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TransferNetworkLossHandler.getInstance(getApplicationContext());

        imageView = findViewById(R.id.imageView);
        Button selectImageButton = findViewById(R.id.uploadButton);


        creds = new BasicAWSCredentials(Constants.ACCESS_ID, Constants.SECRET_KEY);
        s3Client = new AmazonS3Client(creds);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectVideo();
            }
        });
    }


    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    private void selectVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }


  /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the Uri of data
            filePath = data.getData();

            imageView.setImageURI(filePath);

            try {
                InputStream inputStream = getContentResolver().openInputStream(filePath);
                if (inputStream != null) {
                    file = File.createTempFile("image", filePath.getLastPathSegment());
                    OutputStream outStream = new FileOutputStream(file);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }

                    inputStream.close();
                    outStream.close();

                    // Call uploadImage() after the file is created
                    uploadImage();
                } else {
                    // Handle the case where inputStream is null (e.g., show an error message).
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            videoPath = data.getData();


            imageView.setImageResource(R.drawable.android);

            try {
                InputStream inputStream = getContentResolver().openInputStream(videoPath);
                if (inputStream != null) {
                    videoFile = File.createTempFile("video", videoPath.getLastPathSegment());
                    OutputStream outputStream = new FileOutputStream(videoFile);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    inputStream.close();
                    outputStream.close();

                    uploadVideo();
                } else {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    private void uploadVideo() {
        TransferUtility transferUtility = TransferUtility.builder()
                .context(getApplicationContext())
                .s3Client(s3Client)
                .build();


        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("video/mp4"); // Set the appropriate content type
        metadata.setContentLength(videoFile.length());

        TransferObserver transferObserver = transferUtility.upload(
                Constants.BUCKET_NAME,
                videoPath.getLastPathSegment(),
                videoFile,
                metadata
        );

        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    Log.d("msg", "Video upload success");
                } else if (state == TransferState.FAILED) {
                    Log.d("msg", "Video upload failed");
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("error", ex.toString());
            }
        });
    }





    private void uploadImage() {
        TransferUtility trans = TransferUtility.builder().context(getApplicationContext()).s3Client(s3Client).build();
        TransferObserver observer = trans.upload(Constants.BUCKET_NAME, filePath.getLastPathSegment(), file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    Log.d("msg", "success");
                } else if (state == TransferState.FAILED) {
                    Log.d("msg", "fail");
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                if (bytesCurrent == bytesTotal) {
                    imageView.setImageResource(R.drawable.image);
                }
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("error", ex.toString());
            }
        });
    }

    public static class Constants {
        public static final String ACCESS_ID = "AKIARGF6Q3VSS62OB6WA";
        public static final String SECRET_KEY = "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";
        public static final String BUCKET_NAME = "novusandroid";
    }
}
