package com.edu.kiet.uploadimage;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

//import java.text.BreakIterator;
//
//import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseAuth mAuth;
    private Button choose_image_btn;
    private Button upload_image_btn;
    private ImageView image_view;
    ProgressDialog pd ;
    //private ProgressBar progressbaruploadimage;
    private TextView usernmae;
    private Uri imageUri;

    private StorageTask uploadTask;

    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        mAuth=FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        choose_image_btn = findViewById(R.id.choose_image_btn);
        upload_image_btn=findViewById(R.id.upload_image_btn);
        image_view = findViewById(R.id.image_view);
        //progressbaruploadimage=findViewById(R.id.progressbaruploadimage);
        usernmae = findViewById(R.id.username);
        pd=new ProgressDialog(MainActivity.this);

        databaseReference = FirebaseDatabase.getInstance().getReference("profilepicupload");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("profilepicupload");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                upload user = dataSnapshot.getValue(upload.class);
                usernmae.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    image_view.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getBaseContext()).load(user.getImageURL()).into(image_view);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        choose_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        upload_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uplaodImage();
            }
        });

    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            imageUri = data.getData();
//            image_view.setImageURI(imageUri);
//
//        }
//    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uplaodImage(){
     pd.setMessage("Uplaoding...");
  // pd.setTitle("ProgressDialog");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //pd.getMax();
        pd.getProgress();
        pd.incrementProgressBy(2);
        pd.setMax(100);

        pd.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (pd.getProgress() <= pd
                            .getMax()) {
                        Thread.sleep(200);
                        handle.sendMessage(handle.obtainMessage());
                        if (pd.getProgress() == pd
                                .getMax()) {
                            pd.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();




    if(imageUri !=null){
        final StorageReference fileReference=storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));

        uploadTask=fileReference.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();

            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri downloadUri=task.getResult();
                            String mUri=downloadUri.toString();
                            databaseReference = FirebaseDatabase.getInstance().getReference("profilepicupload");
                    HashMap<String,Object> map=new HashMap<>();
                    map.put("ImageURL",mUri);
                    databaseReference.updateChildren(map);
                   pd.dismiss();
                    Toast.makeText(getApplicationContext(),"Image uploaded successfully",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
               pd.dismiss();
            }
        });
    }else{
        Toast.makeText(getApplicationContext(),"No Image Selected",Toast.LENGTH_SHORT).show();
    }

    }


            @Override
            public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
                    imageUri=data.getData();
                    image_view.setImageURI(imageUri);

            if(uploadTask !=null && uploadTask.isInProgress()){

                Toast.makeText(getApplicationContext(),"upload in progess",Toast.LENGTH_SHORT).show();
            }else {

                uplaodImage();
            }
        }

    }
    Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            pd.incrementProgressBy(1);
        }
    };
}
