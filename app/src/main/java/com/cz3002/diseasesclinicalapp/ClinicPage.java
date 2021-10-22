package com.cz3002.diseasesclinicalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;

import lombok.SneakyThrows;

public class ClinicPage extends AppCompatActivity {
    private Button nextPatientButton;
    private Button walkInPatient;
    private TextView signoutButton;
    private TextView queueText;
    private TextView patientNames;
    private TextView patientIndex;
    private TextView clinic_Name;
    private FirebaseDatabaseManager dbMngr;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    public ArrayList<String> names;
    public ArrayList<String> index;
    private EditText walkInName;
    private Button next_confirm, next_cancel, walkin_confirm, walkin_cancel, swap_confirm, swap_cancel, push_confirm, push_cancel;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private HttpRequestHandler hndlr;
    private ClinicUser curUser;
    private TextView NameLabel,IndexLabel;
    private String clinicUID;
    private ImageView deletePatient, swapBackPatient;
    private RecyclerViewEmptySupport itemRowView;
    private QueueRowAdapter adapter;
    private Button swapButton;
    //private List<String> names = new ArrayList<>();
    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clinic);
        mAuth = FirebaseAuth.getInstance();
        NameLabel = findViewById(R.id.NameLabel);
        IndexLabel = findViewById(R.id.IndexLabel);
        mUser = mAuth.getCurrentUser();
        dbMngr = new FirebaseDatabaseManager(ClinicPage.this);
        hndlr = new HttpRequestHandler();
        nextPatientButton = findViewById(R.id.dequeue_button);
        signoutButton = findViewById(R.id.sign_out);
        queueText = findViewById(R.id.queue_text);
        swapButton = findViewById(R.id.swapButton);
        patientNames = findViewById(R.id.patient_name);
        names = new ArrayList<>();
        patientIndex = findViewById(R.id.patient_index);
        index = new ArrayList<>();
        walkInPatient = findViewById(R.id.add_walkin);
        clinic_Name = findViewById(R.id.clinic_name);
        dialogBuilder = new AlertDialog.Builder(this);
        swapBackPatient= findViewById(R.id.swapBackPatient);
        deletePatient= findViewById(R.id.deletePatient);
        itemRowView = findViewById(R.id.queueView);
        //itemRowView.setEmptyView(findViewById(R.id.list_empty));
        adapter = new QueueRowAdapter();
        itemRowView.setAdapter(adapter);
        itemRowView.setLayoutManager(new LinearLayoutManager(this));
        dbMngr.getDatabaseReference("app","Users",mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null)
                {
                    curUser = snapshot.getValue(ClinicUser.class);
                    clinicUID = curUser.getClinicUID();
                    adapter.setClinicUID(clinicUID);
                    setClinicName(clinicUID);
                    listenForQueueChanges(clinicUID);
                    nextPatientButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            nextPatient();
                        }
                    });
                    swapButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pushPatient();
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

        walkInPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWalkinPatient();
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
                        if (snapshot.getValue()!=null) {
                            GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                            };
                            ArrayList<String> queue = snapshot.getValue(t);
                            //set display text to number of people in the queue\
                            queueText.setText(String.valueOf(queue.size()));
                            dbMngr.getNameDictionary()
                                    .thenApply(nameDict -> {
                                        adapter.setNameDict(nameDict);
                                        adapter.setQueue(queue);
                                    return null;
                                    });
                        }
                        else{
                            queueText.setText("No one in queue");
                               // Log.d("empty queue", "here");
                                adapter.setQueue(null);
                                //itemRowView.setEmptyView(findViewById(R.id.list_empty));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void setClinicName(String clinicUID)
    {
        dbMngr.clinicDatabase.getReference("clinicDictionary")
                .child(clinicUID).child("clinicName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue()!=null)
                        {
                            String clinicName = snapshot.getValue().toString();
                            clinic_Name.setText(clinicName);
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

    public void nextPatient(){
        dialogBuilder = new AlertDialog.Builder(this);
        final View nextPopUpView = getLayoutInflater().inflate(R.layout.next_popup,null);
        next_confirm = nextPopUpView.findViewById(R.id.next_confirm);
        next_cancel = nextPopUpView.findViewById(R.id.next_cancel);
        dialogBuilder.setView(nextPopUpView);
        dialog = dialogBuilder.create();
        dialog.show();
        next_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        next_confirm.setOnClickListener(new View.OnClickListener() {
            @SneakyThrows
            @Override
            public void onClick(View v) {
                try {
                    hndlr.deQueue(clinicUID)
                            .thenApply(s->{
                                Log.e("the result", s);
                                return null;
                            });
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                // try {
                //                                hndlr.deQueue(clinicUID)
                //                                        .thenApply(s->{
                //                                            Log.e("the result", s);
                //                                            return null;
                //                                        });
                //                            } catch (JsonProcessingException e) {
                //                                e.printStackTrace();
                //                            }
                //                        }
                dialog.dismiss();
            }
        });
    }
    public void addWalkinPatient(){
        final View walkinPopUpView = getLayoutInflater().inflate(R.layout.walkin_popup,null);
        walkInName = walkinPopUpView.findViewById(R.id.walkin_name);
        walkin_confirm = walkinPopUpView.findViewById(R.id.walkin_confirm);
        walkin_cancel = walkinPopUpView.findViewById(R.id.walkin_cancel);
        dialogBuilder.setView(walkinPopUpView);
        dialog = dialogBuilder.create();
        dialog.show();
        walkin_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        walkin_confirm.setOnClickListener(new View.OnClickListener() {
            @SneakyThrows
            @Override
            public void onClick(View v) {
                String name = walkInName.getText().toString();
                if (name != null && name != "")
                {
                    hndlr.joinQueue(curUser.getClinicUID(),name);
                    dialog.dismiss();
                }
                else
                {
                    Toast.makeText(ClinicPage.this,"Please Enter a Valid Name",Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });
    }

//    public void swapPatient(){
//        final View swapPopUpView = getLayoutInflater().inflate(R.layout.swap_popup,null);
//        swap_confirm = swapPopUpView.findViewById(R.id.swap_confirm);
//        swap_cancel = swapPopUpView.findViewById(R.id.swap_cancel);
//        dialogBuilder.setView(swapPopUpView);
//        dialog = dialogBuilder.create();
//        dialog.show();
//        swap_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        swap_confirm.setOnClickListener(new View.OnClickListener() {
//            @SneakyThrows
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//    }
    public void pushPatient(){
        final View pushPopUpView = getLayoutInflater().inflate(R.layout.push_popup,null);
        push_confirm = pushPopUpView.findViewById(R.id.push_confirm);
        push_cancel = pushPopUpView.findViewById(R.id.push_cancel);
        dialogBuilder.setView(pushPopUpView);
        dialog = dialogBuilder.create();
        dialog.show();
        push_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        push_confirm.setOnClickListener(new View.OnClickListener() {
            @SneakyThrows
            @Override
            public void onClick(View v) {
                HttpRequestHandler hndlr = new HttpRequestHandler();
                hndlr.swapPatients(clinicUID);
                dialog.dismiss();
            }
        });
    }



}