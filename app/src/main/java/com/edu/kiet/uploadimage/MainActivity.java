package com.edu.kiet.uploadimage;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
//import com.google.firebase.database.FirebaseDatabse;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST=1;

    private Button choose_image_btn;
    private Button upload_image_btn;
    private ImageView image_view;
    private ProgressBar progressbaruploadimage;
    private EditText filename;

    private Uri imageUri;


    private StorageReference strorageref;
    private DatabaseReference databaseref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choose_image_btn=findViewById(R.id.choose_image_btn);
        upload_image_btn=findViewById(R.id.upload_image_btn);
        image_view=findViewById(R.id.image_view);
        progressbaruploadimage=findViewById(R.id.progressbaruploadimage);
        filename=findViewById(R.id.filename);

        strorageref= FirebaseStorage.getInstance().getReference("profilepicupload");
        databaseref= FirebaseDatabase.getInstance().getReference("profilepicupload");

        choose_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        upload_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadfile();
            }
        });
//        if(!FirebaseApp.getApps(this).isEmpty()){
//            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//
//        }


    }

    private void openImageChooser(){
    Intent intent=new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(intent,PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() !=null){
            imageUri = data.getData();
            image_view.setImageURI(imageUri);

        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));


    }

    private void uploadfile() {
        if (imageUri != null) {

//        final ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Uploading...");
//        progressDialog.show();

            StorageReference fileReference = strorageref.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressbaruploadimage.setProgress(0);
                        }
                    }, 500);
                    Toast.makeText(MainActivity.this, "Upload Successful ", Toast.LENGTH_SHORT).show();
                    upload upload;
                    upload = new upload(filename.getText().toString().trim(), uri.toString());
                    String uplaodId = databaseref.push().getKey();
                    databaseref.child(uplaodId).setValue(upload);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Error while uploading", Toast.LENGTH_SHORT).show();
                }
            });
//}).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                //displaying the upload progress
//                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
//            }
//        });

        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
}
