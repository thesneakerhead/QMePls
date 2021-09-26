package com.cz3002.diseasesclinicalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

import lombok.SneakyThrows;

public class ClinicPage extends AppCompatActivity {
    private Button nextPatientButton;
    private TextView signoutButton;
    private TextView queueText;
    private FirebaseDatabaseManager dbMngr;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clinic_page);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        dbMngr = new FirebaseDatabaseManager(ClinicPage.this);
        HttpRequestHandler hndlr = new HttpRequestHandler();
        nextPatientButton = findViewById(R.id.dequeue_button);
        signoutButton = findViewById(R.id.sign_out);
        queueText = findViewById(R.id.queue_text);
        dbMngr.getDatabaseReference("app","Users",mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null)
                {
                    ClinicUser curUser = snapshot.getValue(ClinicUser.class);
                    listenForQueueChanges(curUser.getClinicUID());
                    nextPatientButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                hndlr.deQueue(curUser.getClinicUID())
                                        .thenApply(s->{
                                            Log.e("the result", s);
                                            return null;
                                        });
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }
    public void listenForQueueChanges(String clinicUID)
    {
        dbMngr.clinicDatabase.getReference("clinicDictionary")
                .child(clinicUID)
                .child("clinicQueue")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue()!=null){
                            GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                            ArrayList<String> queue = snapshot.getValue(t);
                            //set display text to number of people in the queue
                            //queueText.setText(String.valueOf(queue.size()));
                            String queueStr = getString(R.string.queue, String.valueOf(queue.size()));
                            queueText.setText(queueStr);

                        }
                        else{
                            String queueStr = getString(R.string.queue, "0");
                            queueText.setText(queueStr);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void signOut()
    {
        AuthUI.getInstance()
                .signOut(ClinicPage.this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent i = new Intent(ClinicPage.this,FirebaseLogin.class);
                        startActivity(i);
                    }
                });
    }
}