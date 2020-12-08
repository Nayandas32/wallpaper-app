package com.example.storeimageinfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private  static final int PICK_IMAGE_REQUEST =1;

    private Button choose,upload,show;
    private ImageView img;
    private EditText select;
    private ProgressBar bar;

    private Uri imageuri;

    private StorageReference storageRef;
    private DatabaseReference databaseRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        choose = findViewById(R.id.choose);
        upload = findViewById(R.id.send);
        show = findViewById(R.id.next);
        img = findViewById(R.id.imgview);
        select = findViewById(R.id.select);
        bar = findViewById(R.id.prog);


        storageRef = FirebaseStorage.getInstance().getReference("Upload");
        databaseRef = FirebaseDatabase.getInstance().getReference("uploads");


        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openfilechooser();

            }
        });




        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();

            }
        });


        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInagesAcitivity();

            }
        });
    }

    private void openInagesAcitivity() {
        Intent intent = new Intent(this,image_activity.class);
        startActivity(intent);

    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void uploadFile() {

        if (imageuri != null){
            DatabaseReference reference = databaseRef.child("upload");

            StorageReference storageRefe = storageRef.child(System.currentTimeMillis()+"."+getFileExtension(imageuri));
            storageRefe.putFile(imageuri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    bar.setProgress(0);

                                }
                            },500);
                            Toast.makeText(MainActivity.this,"Upload successfull",Toast.LENGTH_SHORT).show();

                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadUrl = uriTask.getResult();
                            //Log.d(this, "onSuccess: firebase download url: " + downloadUrl.toString());
                            Upload upload = new Upload(select.getText().toString().trim(),downloadUrl.toString());



                            //Upload upload = new Upload(select.getText().toString().trim(),taskSnapshot.getUploadSessionUri().toString());
                            String uploadId = databaseRef.push().getKey();
                            databaseRef.child(uploadId).setValue(upload);
                            Toast.makeText(MainActivity.this,"Upload data",Toast.LENGTH_LONG).show();


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            bar.setProgress((int)progress);

                        }
                    });

        } else {
            Toast.makeText(this,"No file selected",Toast.LENGTH_LONG).show();
        }

    }


    private void openfilechooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == resultCode && data != null && data.getData()!= null){
            imageuri = data.getData();
            Picasso.get().load(imageuri).into(img);
        }



    }
}