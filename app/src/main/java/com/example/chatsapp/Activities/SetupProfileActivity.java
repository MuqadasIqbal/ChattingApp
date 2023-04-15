package com.example.chatsapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatsapp.Models.Users;
import com.example.chatsapp.databinding.ActivitySetupProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SetupProfileActivity extends AppCompatActivity {
ActivitySetupProfileBinding binding;
FirebaseAuth auth;
FirebaseDatabase database;
FirebaseStorage storage;
Uri selectedImage;
ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog=new ProgressDialog(this);
        dialog.setMessage("Uploading Profile..");
        dialog.setCancelable(false);
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        getSupportActionBar().hide();

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,45);
            }
        });
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name=binding.nameBox.getText().toString();
                if (name.isEmpty()){
                    binding.nameBox.setError("Please Type Name");
                    return;
                }
                dialog.show();
                if (selectedImage!=null){
                    StorageReference reference=storage.getReference().child("Profiles").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                               reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                   @Override
                                   public void onSuccess(Uri uri) {
                                       String imageUri=uri.toString();
                                       String uid=auth.getUid();
                                       String phone=auth.getCurrentUser().getPhoneNumber();
                                       String name=binding.nameBox.getText().toString();

                                       Users users=new Users(uid,name,phone,imageUri);

                                       database.getReference().child("users").child(uid).setValue(users).addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void unused) {
                                               dialog.dismiss();
                                             Intent intent=new Intent(SetupProfileActivity.this, MainActivity.class);
                                             startActivity(intent);
                                             finishAffinity();
                                           }
                                       });
                                   }
                               });
                            }
                        }
                    });

                }else {

                    String uid=auth.getUid();
                    String phone=auth.getCurrentUser().getPhoneNumber();


                    Users users=new Users(uid,name,phone,"No Image");

                    database.getReference().child("users").child(uid).setValue(users).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            dialog.dismiss();
                            Intent intent=new Intent(SetupProfileActivity.this,MainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        }
                    });

                }

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            selectedImage=data.getData();
            binding.imageView.setImageURI(selectedImage);
        }
    }
}