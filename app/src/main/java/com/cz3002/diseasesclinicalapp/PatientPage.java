package com.cz3002.diseasesclinicalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.ObservableList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import lombok.Data;
import lombok.SneakyThrows;

public class PatientPage extends AppCompatActivity {
    private FirebaseDatabaseManager dbMngr;
    private Button joinQueueButton;
    private TextView queuePosText;
    private Button logoutButton;
    private FirebaseUser loggedInUser;
    private FirebaseAuth firebaseAuth;
    private FloatingActionButton fab;
    private TextView nameText;
    private DatabaseReference userDbRef;
    private PatientUser curPatientUser;
    private OngoingSymptomCard ongoingCard;
    private FrameLayout myLayout;
    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbMngr = new FirebaseDatabaseManager(PatientPage.this);

        setContentView(R.layout.profile__page);
        initPatientPage();
        HttpRequestHandler hndlr = new HttpRequestHandler();
        joinQueueButton = findViewById(R.id.join_queue);
        queuePosText = findViewById(R.id.QueueText);
        logoutButton = findViewById(R.id.logout_button);
        fab = findViewById(R.id.fab);
        nameText = findViewById(R.id.nameText);
        firebaseAuth = FirebaseAuth.getInstance();
        loggedInUser = firebaseAuth.getCurrentUser();



        // Button to join queue
        userDbRef = dbMngr.getDatabaseReference("app","Users",loggedInUser.getUid());
        userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue()!=null)
                {
                    curPatientUser = snapshot.getValue(PatientUser.class);
                    nameText.setText(curPatientUser.getName());
                    displaySymptomCards();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        userDbRef.child("ongoingCard").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null)
                {
                    ongoingCard = snapshot.getValue(OngoingSymptomCard.class);
                    displayOngoingCard();
                    fab.setEnabled(false);
                    listenForQueueChanges(ongoingCard.getClinicUID(),loggedInUser.getUid());
                    logoutButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signOut();
                        }
                    });
                }
                else{
                    ongoingCard = null;
                    fab.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ongoingCard = null;
                fab.setEnabled(true);
            }
        });



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PatientPage.this, SymptomSearch.class);
                startActivity(i);
            }
        });



    }

    private void displayOngoingCard() {

    }

    private void displaySymptomCards() {
        ArrayList<SymptomCard> symptomCards = curPatientUser.getSymptomCards();

    }

    public void listenForQueueChanges(String clinicUID,String patientUID)
    {
        NotificationManager sendNoti = new NotificationManager(this);
        DatabaseReference dbRef = dbMngr.getDatabaseReference("clinic","clinicDictionary",clinicUID,"clinicQueue");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue()!=null){
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                    ArrayList<String> queue = snapshot.getValue(t);
                    Integer queuePos = queue.indexOf(patientUID);
                        if (queuePos < 0)
                        {
                            dbMngr.getDatabaseReference("clinic","clinicDictionary",clinicUID
                                    ,"lastCancelledPatient")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.getValue()!=null)
                                            {
                                                GenericTypeIndicator<String> t = new GenericTypeIndicator<String>() {};
                                                String lastCancelledPatient = snapshot.getValue(t);
                                                if(lastCancelledPatient.equals(loggedInUser.getUid()))
                                                {
                                                    queuePosText.setText("you've been removed from the queue!");
                                                }
                                                else{
                                                    queuePosText.setText("your appointment has been completed");
                                                }

                                            }
                                            else{
                                                queuePosText.setText("your appointment has been completed");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            queuePosText.setText("your appointment has been completed");
                                        }
                                    });
                        }
                        else if (queuePos.equals(1))
                        {
                            sendNoti.sendNotification();
                        }

                        else if(queuePos.equals(0))
                        {
                            queuePosText.setText("Its your turn for consultation!");
                        }
                        else
                        {
                            queuePosText.setText(String.valueOf(queuePos));
                        }
                }
                else{
                    dbMngr.getDatabaseReference("clinic","clinicDictionary",clinicUID
                            ,"lastCancelledPatient")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue()!=null)
                                    {
                                        GenericTypeIndicator<String> t = new GenericTypeIndicator<String>() {};
                                        String lastCancelledPatient = snapshot.getValue(t);
                                        if(lastCancelledPatient.equals(loggedInUser.getUid()))
                                        {
                                            queuePosText.setText("you've been removed from the queue!");
                                        }
                                        else{
                                            queuePosText.setText("your appointment has been completed");
                                        }

                                    }
                                    else{
                                        queuePosText.setText("your appointment has been completed");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    queuePosText.setText("your appointment has been completed");
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    
    public void initPatientPage()
    {


        dbMngr.clinicInfos.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<ClinicInfo>>() {
            @Override
            public void onChanged(ObservableList<ClinicInfo> sender) {

            }

            @Override
            public void onItemRangeChanged(ObservableList<ClinicInfo> sender, int positionStart, int itemCount) {

            }

            @Override
            public void onItemRangeInserted(ObservableList<ClinicInfo> sender, int positionStart, int itemCount) {

            }

            @Override
            public void onItemRangeMoved(ObservableList<ClinicInfo> sender, int fromPosition, int toPosition, int itemCount) {

            }

            @Override
            public void onItemRangeRemoved(ObservableList<ClinicInfo> sender, int positionStart, int itemCount) {

            }
        });
    }
    private void signOut()
    {
        AuthUI.getInstance()
                .signOut(PatientPage.this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent i = new Intent(PatientPage.this,FirebaseLogin.class);
                        startActivity(i);
                    }
                });
    }
}