package com.cz3002.diseasesclinicalapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lombok.SneakyThrows;


public class SymptomSearch extends AppCompatActivity {

    public static final String TAG = "YOUR-TAG-NAME";
    int chipIdCounter=1;
    ArrayList<String> selectedListOfSymptoms;
    private ChipGroup chip_group;
    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_search);
        ApplicationInfo ai = SymptomSearch.this.getPackageManager()
                .getApplicationInfo(SymptomSearch.this.getPackageName(), PackageManager.GET_META_DATA);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference docRef = db.collection("symptoms");
        Client client = new Client(ai.metaData.getString("SymptomAppId"), ai.metaData.getString("SymptomApiKey"));
        Index index = client.getIndex("symptoms");

        EditText editText = findViewById(R.id.edit_text);
        ListView listViewTop = findViewById(R.id.list_view_top);
        Button symptom_button =findViewById(R.id.symptom_button);
        LinearLayout symptomCardLayout= findViewById(R.id.symptomCardLayout);


        //add card
        final View symptom_card = getLayoutInflater().inflate(R.layout.symptom_card,null,false);
        symptomCardLayout.addView(symptom_card);
        chip_group= symptom_card.findViewById(R.id.chip_group);

        selectedListOfSymptoms = new ArrayList<>();

        docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> list = new ArrayList<>();
                    for (DocumentSnapshot document : task.getResult()) {
                        list.add(document.getString("Symptoms"));
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SymptomSearch.this, android.R.layout.simple_list_item_1, list);
                    listViewTop.setAdapter(arrayAdapter);
                } else {
                    Log.d(TAG, "failed to get document");
                }
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Query query = new Query(editable.toString())
                        .setAttributesToRetrieve("Symptoms")
                        .setHitsPerPage(10);
                index.searchAsync(query, new CompletionHandler() {
                    @Override
                    public void requestCompleted(@Nullable JSONObject content, @Nullable AlgoliaException e) {
                        Log.d(TAG, content.toString());
                        try {
                            JSONArray hits = content.getJSONArray("hits");
                            List<String> list = new ArrayList<>();
                            for (int i = 0; i < hits.length(); i++) {
                                JSONObject jsonObject = hits.getJSONObject(i);
                                String disease = jsonObject.getString("Symptoms");
                                list.add(disease);
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SymptomSearch.this, android.R.layout.simple_list_item_1, list);
                                listViewTop.setAdapter(arrayAdapter);

                            }
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }

                    }
                });
            }

        });



        listViewTop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Chip chip;
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSymptom = listViewTop.getItemAtPosition(position).toString();
                editText.getText().clear();
                Log.d(TAG, selectedSymptom);
                if (selectedListOfSymptoms.size()<5)
                {
                    if (selectedListOfSymptoms.contains(selectedSymptom) == false) {
                        selectedListOfSymptoms.add(selectedSymptom);
                        Log.d("checkList", selectedListOfSymptoms.toString());
                        //Create new chip once symptom is selected
                        chip = new Chip(SymptomSearch.this);
                        chip.setText(selectedSymptom);
                        chip.setCloseIconEnabled(true);
                        //chip.setBackgroundColor();
                        chip_group.addView(chip);
                    } else {
                        Log.d("Duplicate", "Duplicate Selection");
                    }
                }
                else
                {
                    Log.d("Max Reached", "Maximum Selection Count of 5 Reached");
                }
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Chip tempChip = (Chip)v;
                        chip_group.removeView(v);
                        if(selectedListOfSymptoms.contains(tempChip.getText())){
                            selectedListOfSymptoms.remove(tempChip.getText());
                        }
                        Log.d(TAG, "onClick: remaining list" + selectedListOfSymptoms.toString());
                    }
                });
            }


        });
    }
    public void buttonClickFunction(View view) throws PackageManager.NameNotFoundException {
        try {
            selectedListOfSymptoms.get(0);
            Intent intent = new Intent(SymptomSearch.this, MapsActivity.class);
            intent.putExtra("symptoms",selectedListOfSymptoms);
            SymptomSearch.this.startActivity(intent);
        }catch (Exception e){
            Intent intent = new Intent(SymptomSearch.this, MapsActivity.class);
            SymptomSearch.this.startActivity(intent);
        }
//        FirebaseDatabaseManager dbMngr = new FirebaseDatabaseManager(SymptomSearch.this);
//        DatabaseReference dbRef = dbMngr.getDatabaseReference("app","Users",
//                FirebaseAuth.getInstance().getCurrentUser().getUid());
//        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.getValue()!=null)
//                {
//                    PatientUser user = snapshot.getValue(PatientUser.class);
//                    SymptomCard symCard = new SymptomCard();
//                    Date c = Calendar.getInstance().getTime();
//                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//                    String formattedDate = df.format(c);
//                    symCard.setDate(formattedDate);
//                    symCard.setSymptoms(selectedListOfSymptoms);
//                    symCard.setClinicName("name placeholder");
//                    ArrayList<SymptomCard> symptomCards;
//                    if (user.getSymptomCards()!=null)
//                    {
//                        symptomCards = user.getSymptomCards();
//                    }
//                    else
//                    {
//                        symptomCards = new ArrayList<SymptomCard>();
//                    }
//                    symptomCards.add(symCard);
//                    user.setSymptomCards(symptomCards);
//                    dbRef.setValue(user);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }
}