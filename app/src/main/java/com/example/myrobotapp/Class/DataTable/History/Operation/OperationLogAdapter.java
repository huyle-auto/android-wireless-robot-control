package com.example.myrobotapp.Class.DataTable.History.Operation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrobotapp.R;

import java.util.ArrayList;
import java.util.List;

public class OperationLogAdapter extends RecyclerView.Adapter<OperationLogAdapter.LogViewHolder> {
    private List<OperationLogEntryModel> logs = new ArrayList<>();

    public void updateLogs(List<OperationLogEntryModel> newLogs) {
        logs = newLogs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.operation_history_table_row, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        OperationLogEntryModel log = logs.get(position);
        holder.timestamp.setText(log.getTimestamp());
        holder.message.setText(log.getMessage());
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView timestamp, message;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            timestamp = itemView.findViewById(R.id.operationTimestampTextView);
            message = itemView.findViewById(R.id.operationMessageTextView);
        }
    }
}

