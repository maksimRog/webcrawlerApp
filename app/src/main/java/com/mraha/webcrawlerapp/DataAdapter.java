package com.mraha.webcrawlerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
    private List<LinkHolder> storage;

    public DataAdapter(List<LinkHolder> storage) {
        this.storage = storage;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            holder.idView.setText("id");
            holder.urlView.setText("url");
            holder.termView.setText("term");
        } else {
            holder.idView.setText(String.valueOf(storage.get(position-1).getId()));
            holder.urlView.setText(storage.get(position-1).getLink());
            holder.termView.setText(String.valueOf(storage.get(position-1).getTermCounter()));
        }
    }

    @Override
    public int getItemCount() {
        return storage.size() + 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView idView;
        TextView urlView;
        TextView termView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            idView = itemView.findViewById(R.id.idViewHolder);
            urlView = itemView.findViewById(R.id.urlViewHolder);
            termView = itemView.findViewById(R.id.termViewHolder);
        }
    }
}
