package com.cz3002.diseasesclinicalapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;





public class SymptomSearch extends AppCompatActivity {

    public static final String TAG = "YOUR-TAG-NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_search);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference docRef = db.collection("symptoms");

        Client client = new Client("NLPL9FXAUP", "1885b5d9b84bdb8be21ebdd26134f5d1");
        Index index = client.getIndex("symptoms");

        EditText editText = findViewById(R.id.edit_text);
        ListView listViewTop = findViewById(R.id.list_view_top);
        ListView listViewBottom = findViewById(R.id.list_view_bottom);

        List<String> selectedListOfSymptoms = new ArrayList<>();

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
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSymptom = listViewTop.getItemAtPosition(position).toString();
                editText.getText().clear();
                Log.d(TAG, selectedSymptom);
                selectedListOfSymptoms.add(selectedSymptom);
                Log.d("checkList", selectedListOfSymptoms.toString());

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter(SymptomSearch.this, android.R.layout.simple_list_item_1, selectedListOfSymptoms);
                listViewBottom.setAdapter(arrayAdapter);


                //adapter.dismiss(); // If you want to close the adapter

            }


        });



    }

}