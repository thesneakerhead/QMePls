package com.cz3002.diseasesclinicalapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.SneakyThrows;
import lombok.val;

import static com.google.android.gms.common.util.CollectionUtils.listOf;

public class FirebaseLogin extends AppCompatActivity {
    private FirebaseDatabaseManager dbMngr;
    private FirebaseAuth firebaseAuth;
    private List<AuthUI.IdpConfig> providers;
    private ArrayAdapter adapter;
    private AutoCompleteTextView autoCompleteTextView;
    private String selection;
    private Button confirmButton;
    private User loggedInUser;
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @SneakyThrows
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );
    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        dbMngr = new FirebaseDatabaseManager(FirebaseLogin.this);
        super.onCreate(savedInstanceState);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null)
        {
            Intent i = new Intent(FirebaseLogin.this,PatientPage.class);
            startActivity(i);
        }
        setContentView(R.layout.firebaselogin);
        List<String> options = List.of("Patient","Clinic");
        adapter = new ArrayAdapter(FirebaseLogin.this,R.layout.list_item,options);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        confirmButton = findViewById(R.id.confirm_button);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selection = (String)parent.getItemAtPosition(position);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selection!=null)
                {
                    if (selection.equals("Patient"))
                    {
                        init(false);
                    }
                    else if(selection.equals("Clinic"))
                    {
                        init(true);
                    }
                }
            }
        });


        //init();
    }

    private void init(Boolean isClinicLogin) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Toast.makeText(FirebaseLogin.this, "There is an account logged in: " + user.getUid(), Toast.LENGTH_SHORT).show();
            navigateBasedOnAccount(user.getUid(),user.getProviderId());
        } else {
            createSignInPage(isClinicLogin);
        }

    }
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) throws JsonProcessingException {

        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = firebaseAuth.getCurrentUser();
            //Log.e("test", user.getDisplayName());
            //check if account is new/clinic/patient account
            navigateBasedOnAccount(user.getUid(),user.getProviderId());
        } else {

        }
    }

    private void navigateBasedOnAccount(String uid, String providerId) {

        DatabaseReference dbRef = dbMngr.appDatabase.getReference("Users").child(uid);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null)
                {
                    Log.d("userexists", "This is not the first time signing in");
                    GenericTypeIndicator<User> t = new GenericTypeIndicator<User>(){};
                    GenericTypeIndicator<PatientUser> p = new GenericTypeIndicator<PatientUser>(){};
                    loggedInUser = snapshot.getValue(t);
                    if(loggedInUser.isClinicAcc==true && selection.equals("Clinic"))
                    {
                        Intent i = new Intent(FirebaseLogin.this,ClinicPage.class);
                        startActivity(i);
                    }
                    else if(loggedInUser.isClinicAcc==false && selection.equals("Patient"))
                    {
                        PatientUser user = snapshot.getValue(p);
                        Intent i;
                        if(user.getName()!=null){
                            i = new Intent(FirebaseLogin.this, PatientPage.class);
                        }
                        else {
                            if (providerId.equals("phone")) {
                                i = new Intent(FirebaseLogin.this, AddInfoActivity.class);
                            }
                            else if (firebaseAuth.getCurrentUser().getDisplayName()!= null || firebaseAuth.getCurrentUser().getDisplayName()!="")
                            {
                                user.setName(firebaseAuth.getCurrentUser().getDisplayName());
                                dbRef.setValue(user);
                                i = new Intent(FirebaseLogin.this, PatientPage.class);
                            }
                            else
                            {
                                i = new Intent(FirebaseLogin.this, AddInfoActivity.class);
                            }
                        }
                        startActivity(i);
                    }
                    else{
                        Toast.makeText(FirebaseLogin.this, "Account and Domain doesnt Match!" , Toast.LENGTH_SHORT).show();
                        AuthUI.getInstance()
                                .signOut(FirebaseLogin.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    public void onComplete(@NonNull Task<Void> task) {
                                    }
                                });
                    }

                }
                else
                {  if(selection.equals("Patient")) {
                        Log.d("usernotexists", "First time user is signing in, creating data for user");
                        //assuming only patients register through the app
                        PatientUser newUser  = new PatientUser();
                        dbRef.setValue(newUser);
                    Intent i = new Intent(FirebaseLogin.this,AddInfoActivity.class);
                    startActivity(i);
                    }
                    else if(selection.equals("Clinic")) {
                    Log.d("nosuchadmin", "there is no such clinical user");
                    AuthUI.getInstance()
                            .signOut(FirebaseLogin.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                }
                            });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("checkfailed", "Account check failed!");
            }
        });
    }

    public void createSignInPage(Boolean isAdminLogin)
    {
        if (!isAdminLogin)
            {
                providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build(), // Log in using Google
                    new AuthUI.IdpConfig.EmailBuilder().build(), // Log in using Email
                    new AuthUI.IdpConfig.FacebookBuilder().build(), // Log in using Facebook
                    new AuthUI.IdpConfig.PhoneBuilder().build() //Log in using Phone
                );
                Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build();
                signInLauncher.launch(signInIntent);
            }
        else if (isAdminLogin)
        {
            providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build() // Log in using Email
            );
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build();
            signInLauncher.launch(signInIntent);
        }

    }
    //to be deleted, used to upload clinic info.
    public void uploadClinicInfo() throws PackageManager.NameNotFoundException {
        Update2Firebase update = new Update2Firebase();
        ClinicInfo clinicInfo = new ClinicInfo();
        clinicInfo.setLat(103.872312298306994);
        clinicInfo.setLng(1.36995099695439);
        clinicInfo.setOpeningHour("0830");
        clinicInfo.setClosingHour("1330");
        clinicInfo.setAddress("321 testing road");
        clinicInfo.setTelNo("6777 7777");
        clinicInfo.setClinicName("Test Hospital 1");
        update.uploadClinicsToFirebase(clinicInfo,FirebaseLogin.this);
    }
}