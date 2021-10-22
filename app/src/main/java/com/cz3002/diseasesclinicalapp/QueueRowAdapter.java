package com.cz3002.diseasesclinicalapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.SneakyThrows;

public class QueueRowAdapter extends RecyclerView.Adapter<QueueRowAdapter.ViewHolder> {
    private ArrayList<String> queue;
    private HashMap<String,String> nameDict;
    private String clinicUID;
    public QueueRowAdapter()
    {
        this.queue = null;
        this.nameDict = null;
        this.clinicUID = null;
    }
    public QueueRowAdapter(ArrayList<String> queue, HashMap<String,String> nameDictionary, String clinicUID)
    {
        this.queue = queue;
        this.nameDict = nameDictionary;
        this.clinicUID = clinicUID;
    }
    public void setQueue(ArrayList<String> queue)
    {
        Log.d("clinicUID", clinicUID);
        this.queue = queue;
        notifyDataSetChanged();
    }
    public void setNameDict(HashMap<String,String> nameDict)
    {
        this.nameDict = nameDict;
    }
    public void setClinicUID(String clinicUID)
    {
        this.clinicUID = clinicUID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View item_row = inflater.inflate(R.layout.item_row,parent,false);
        ViewHolder viewHolder = new ViewHolder(item_row);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (this.queue!=null){
        HttpRequestHandler hndlr = new HttpRequestHandler();
        String uid = queue.get(position);
        Log.d("position",String.valueOf(position));
        String name = nameDict.get(uid);
        TextView nameView = holder.nameTextView;
        TextView indexView = holder.indexTextView;
        ImageView deleteButton = holder.deleteButton;
        if (name!=null)
        {
            nameView.setText(name);
        }
        else
        {
            nameView.setText(uid);
        }
        indexView.setText(String.valueOf(position+1));
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @SneakyThrows
            @Override
            public void onClick(View v) {
                Log.d("deleted",String.valueOf(position));
                hndlr.delByQueuePos(clinicUID,String.valueOf(position));
            }
        });}
    }

    @Override
    public int getItemCount() {
        if (queue!=null) {
            return queue.size();

        }
        else {
            return 0;
        }
        }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView,indexTextView;
        public ImageView deleteButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.name_row);
            indexTextView = (TextView) itemView.findViewById(R.id.index_row);
            deleteButton = (ImageView) itemView.findViewById(R.id.delbut_row);
        }
    }
}
