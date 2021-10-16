package com.cz3002.diseasesclinicalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.ObservableList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
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
    private CardView profilecard1, profilecard2;
    private TextView clinicname1, clinicname2;
    private TextView dateTimeCard1, dateTimeCard2;
//    private TextView ongoingText;
    private ChipGroup chip_group_profile1, chip_group_profile2;


    //test
    private CardView profilecard;
    private TextView clinicname;
    private TextView dateTimeCard;
    private TextView ongoingText;
    private ChipGroup chip_group_profile;
    private LinearLayout layout_list;
    private Button testButton;

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

//        //cards
//       profilecard1 = findViewById(R.id.profilecard1);
//       profilecard2 = findViewById(R.id.profilecard1);
//       clinicname1 = findViewById(R.id.clinicname1);
//       clinicname2 = findViewById(R.id.clinicname2);
//       dateTimeCard1 = findViewById(R.id.dateTimeCard1);
//       dateTimeCard2 = findViewById(R.id.dateTimeCard2);
//       ongoingText= findViewById(R.id.ongoingText);
//       chip_group_profile1= findViewById(R.id.chip_group_profile1);
//       chip_group_profile2= findViewById(R.id.chip_group_profile2);

        //cards
        profilecard = findViewById(R.id.profilecard);
        clinicname = findViewById(R.id.clinicname);
        dateTimeCard = findViewById(R.id.dateTimeCard);
        ongoingText= findViewById(R.id.ongoingText);
        chip_group_profile= findViewById(R.id.chip_group_profile);
        layout_list= findViewById(R.id.layout_list);
        testButton= findViewById(R.id.testButton);


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
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

        testButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                addView();
            }
        });


    }

    private void displayOngoingCard() {

    }

    private void displaySymptomCards() {
        ArrayList<SymptomCard> symptomCards = curPatientUser.getSymptomCards();
        //ArrayList<SymptomCard> clinicNames = curPatientUser.getClinicNames();

        //ongoing
        //editClinicName
        //editdatetime
        //editsymptomschips
        //editvisibility card
        //edit visibility ongoing

//          profilecard1, profilecard2;
//          clinicname1, clinicname2;
//          dateTimeCard1, dateTimeCard2;
//          ongoingText; chip_group_profile1;

        //edit text with data
//        for(int i=0; i<symptomCards.size(); i++){

  //      }
    }
    private void addView() {

        //recent
//        for(int i=0; i<symptomCards.size(); i++){
//
//        }
        final View cardView = getLayoutInflater().inflate(R.layout.profile_card,null,false);
        layout_list.addView(cardView);
        // layout_list.removeView(cardView);

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