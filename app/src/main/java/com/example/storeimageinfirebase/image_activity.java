package com.example.storeimageinfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class image_activity extends AppCompatActivity implements ImageAdapter.OnItemClickListener  {
    private RecyclerView recyclerView;
    private ImageAdapter madapter;
    private FirebaseStorage mStorage;

    private ProgressBar mProgressCircle;
    private ValueEventListener mDBListener;


    private DatabaseReference reference;
    private List<Upload> muploads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mProgressCircle = findViewById(R.id.progress_circle);
        muploads = new ArrayList<>();
        madapter = new ImageAdapter(image_activity.this,muploads);

        recyclerView.setAdapter(madapter);
        mStorage = FirebaseStorage.getInstance();
       // madapter.notifyDataSetChanged();

        reference = FirebaseDatabase.getInstance().getReference("uploads");
        mDBListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                muploads.clear();
                for (DataSnapshot postSnapshot: snapshot.getChildren()){
                    Upload  upload = postSnapshot.getValue(Upload.class);
                    upload.setMkey(postSnapshot.getKey());
                    muploads.add(upload);
                }
                madapter.notifyDataSetChanged();
                madapter.setOnItemClickListener(image_activity.this);


                mProgressCircle.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(image_activity.this, error.getMessage(),Toast.LENGTH_LONG).show();
                mProgressCircle.setVisibility(View.INVISIBLE);

            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, "Normal click at position: " + position, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onWhatEverClick(int position) {
        Toast.makeText(this, "Whatever click at position: " + position, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDeleteClick(int position) {

        Upload selectedItem = muploads.get(position);
        final String selectedkey = selectedItem.getMkey();
        StorageReference imageref = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                reference.child(selectedkey).removeValue();
                Toast.makeText(image_activity.this,"delated",Toast.LENGTH_SHORT).show();

            }
        });
        Toast.makeText(this, "delete click at position: " + position, Toast.LENGTH_SHORT).show();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        reference.removeEventListener(mDBListener);
    }
}