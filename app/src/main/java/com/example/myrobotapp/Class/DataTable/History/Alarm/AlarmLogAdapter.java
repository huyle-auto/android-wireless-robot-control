package com.example.myrobotapp.Class.DataTable.History.Alarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrobotapp.R;

import java.util.ArrayList;
import java.util.List;

public class AlarmLogAdapter extends RecyclerView.Adapter<AlarmLogAdapter.LogViewHolder> {
    private List<AlarmLogEntryModel> logs = new ArrayList<>();

    public void updateLogs(List<AlarmLogEntryModel> newLogs) {
        logs = newLogs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_history_table_row, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        AlarmLogEntryModel log = logs.get(position);
        holder.timestamp.setText(log.getTimestamp());
        holder.code.setText(log.getCode());
        holder.subCode.setText(log.getSubCode());
        holder.message.setText(log.getMessage());
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView timestamp, code, subCode, message;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            timestamp = itemView.findViewById(R.id.alarmDateTextView);
            code = itemView.findViewById(R.id.alarmCodeTextView);
            subCode = itemView.findViewById(R.id.alarmSubCodeTextView);
            message = itemView.findViewById(R.id.alarmActionTextView);
        }
    }
}

