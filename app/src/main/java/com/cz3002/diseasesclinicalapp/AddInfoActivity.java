package com.cz3002.diseasesclinicalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import lombok.SneakyThrows;

public class AddInfoActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Button confirmButton;
    private EditText nameTextField;
    private FirebaseDatabaseManager dbMngr;
    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_info);
        dbMngr = new FirebaseDatabaseManager(AddInfoActivity.this);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        nameTextField = findViewById(R.id.EnterNameText);
        confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameText = nameTextField.getText().toString();
                DatabaseReference dbref = dbMngr.getDatabaseReference("app","Users",user.getUid());
                dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue() != null)
                        {
                            PatientUser user = snapshot.getValue(PatientUser.class);
                            user.setName(nameText);
                            dbref.setValue(user);
                            Intent i = new Intent(AddInfoActivity.this,PatientPage.class);
                            startActivity(i);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

    }
}