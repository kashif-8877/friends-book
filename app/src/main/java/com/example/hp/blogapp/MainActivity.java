package com.example.hp.blogapp;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.FragmentTransitionSupport;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolBar;
    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    FloatingActionButton add_post_btn;
    private  String user_id;

    private CircleImageView User_Profile_Image_View;
    private TextView User_Profile_name_Text_View;

    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment;

    private String name,image;
    private StorageReference mStorageRef;
    private Uri default_uri = null;
    private Uri main_uri = null;

    private boolean isChanged=true;

   // private AccountFragment accountFragment;
   // private NotificationFragment notificationFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RequestOptions placeHolder = new RequestOptions();
        placeHolder.placeholder(R.mipmap.user);


        firebaseFirestore = FirebaseFirestore.getInstance();


//        toolBar =  findViewById(R.id.toolbar);
//        setSupportActionBar(toolBar);
//
//        getSupportActionBar().setTitle("Photo Blog");

        User_Profile_Image_View = findViewById(R.id.user_profile_image_view);
        User_Profile_name_Text_View = findViewById(R.id.user_profile_name);


        toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle(null);

        User_Profile_Image_View.setImageResource(R.drawable.profile_placeholder);


        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        default_uri = Uri.parse("R.mipmap.user");
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

     if(mAuth.getCurrentUser() != null) {


         bottomNavigationView = findViewById(R.id.mainBottomNav);

         //fragments
         homeFragment = new HomeFragment();
        // notificationFragment = new NotificationFragment();
         //accountFragment = new AccountFragment();

         replaceFragment(homeFragment);

         //Onclick on Bottom Nav Bar
         bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
             @Override
             public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                 switch (item.getItemId()) {

//                     case R.id.bottomAccount:
//                         replaceFragment(accountFragment);
//                         return true;


                     case R.id.bottomHome:
                         replaceFragment(homeFragment);
                         return true;


//                     case R.id.bottomNotification:
//                         replaceFragment(notificationFragment);
//                         return true;
                 }
                 return false;
             }
         });




         add_post_btn = findViewById(R.id.addPostButton);

         add_post_btn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                 if (currentUser != null) {
                     user_id = mAuth.getCurrentUser().getUid();
                     firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                         @Override
                         public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                             if (task.getResult().exists()) {
                                 Intent addPost = new Intent(MainActivity.this, NewPost.class);
                                 startActivity(addPost);
                             } else {
                                 Toast.makeText(MainActivity.this, "Please choose profile photo and name", Toast.LENGTH_LONG).show();
                                 Intent main = new Intent(MainActivity.this, AccounrSetup.class);
                                 startActivity(main);
                             }
                         }
                     });
                 } else {
                     Toast.makeText(MainActivity.this, "Please choose profile photo and name", Toast.LENGTH_LONG).show();
                     Intent main = new Intent(MainActivity.this, AccounrSetup.class);
                     startActivity(main);
                 }


             }
         });

     }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Check if user logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            sendLogin();
            finish();
        }
        else
        {
            //Evertime settings load check if data is already present in FireStore, if yes retrive and set name and image using glide
            firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(task.getResult().exists()){
                            isChanged = false;
                            name = task.getResult().getString("name");
                            image = task.getResult().getString("image");

//                        Toast.makeText(AccounrSetup.this,"DATA EXISTS",Toast.LENGTH_LONG).show();
                            User_Profile_name_Text_View.setText(name);

                            //GLIDE APP set default background
                            RequestOptions placeHolder = new RequestOptions();
                            placeHolder.placeholder(R.mipmap.user);

                            //Convert image string to URI and store it in mainImageUri
                            main_uri = Uri.parse(image);

                            Glide.with(MainActivity.this).setDefaultRequestOptions( placeHolder.placeholder(R.mipmap.user)).load(image).into(User_Profile_Image_View);

                        }
                        else{
                            main_uri = default_uri;
                            Toast.makeText(MainActivity.this,"NO DATA EXISTS",Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this,"Firestore Retrieve Error OUTSIDE",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    private void sendLogin() {
       Intent ash = new Intent(MainActivity.this,Login.class);
        startActivity(ash);
    }

    //add menu drawable resource to action bar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout:
                logOut();
                return true;

            case R.id.action_settings:
                Intent acS = new Intent(MainActivity.this,AccounrSetup.class);
                startActivity(acS);

                return true;

                default:
                    return false;
        }
    }


    private void logOut() {
        mAuth.signOut();
        sendLogin();
    }

    //Fragment transition to change fragment when pressed
    private void replaceFragment(android.support.v4.app.Fragment fragment){
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();
    }

}
