package com.example.myrobotapp.Class.DataTable.ProgramInput;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrobotapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProgramInputRowAdapter extends RecyclerView.Adapter<ProgramInputRowAdapter.ViewHolder> {

    private final List<ProgramInputCodeLineModel> codeLines;
    public static String posProgramLine = "-1";
    Activity activity;

    public ProgramInputRowAdapter(Context context, List<ProgramInputCodeLineModel> initialLines) {
        this.codeLines = initialLines != null ? initialLines : new ArrayList<>();
        activity = (Activity) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.program_input_code_line, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProgramInputCodeLineModel codeLine = codeLines.get(position);

        // Reset background color for all rows
        holder.tableRow.setBackgroundColor(Color.parseColor("#B0E0E6")); // Unselected color (Powder blue)

        // On every table row bind. Check if that row is the selected one and highlight accordingly
        if (position == Integer.parseInt(posProgramLine)) {
            holder.tableRow.setBackgroundColor(Color.parseColor("#A9A9A9")); // Selected color (Gray)
        } else {
            holder.tableRow.setBackgroundColor(Color.parseColor("#B0E0E6")); // Unselected color (Powder blue)
        }

        holder.tableRow.setClickable(true);
        holder.tableRow.setFocusable(true);
        holder.tableRow.setOnClickListener(v -> {
            posProgramLine = String.valueOf(holder.getAdapterPosition());
            notifyDataSetChanged();
        });

        // Set line index
        codeLine.setIndex(position + 1); // Ensure index is always updated
        holder.lineIndex.setText(String.valueOf(codeLine.getIndex()));

        // Set and track code content
        holder.codeContent.setText(codeLine.getContent());
        holder.codeContent.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                codeLine.setContent(holder.codeContent.getText().toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return codeLines.size();
    }

    // Method to gather all code content from current Recycler View
    public String getCodeContent() {
        StringBuilder allCode = new StringBuilder();
        for (ProgramInputCodeLineModel line : codeLines) {
            allCode.append(line.getContent()).append("\n"); // Append each line of code followed by a newline
        }
        return allCode.toString().trim(); // Remove the trailing newline
    }

    // Method to gather one selected code line's content
    public String getSelectedContent() {
        String selectedCodeContent = "";
        if (!("-1").equals(posProgramLine)){
            selectedCodeContent = codeLines.get(Integer.parseInt(posProgramLine)).getContent();
        }
        return selectedCodeContent;
    }

    // Method to set all code content (from String) to Recycler View
    public void setCodeContent(String codeContent) {
        List<String> lines = Arrays.asList(codeContent.split("\\R"));
        if (!lines.isEmpty()){
            for (int i = 1; i <= lines.size(); i++){
                ProgramInputCodeLineModel lineWithIndex = new ProgramInputCodeLineModel(i, lines.get(i-1));
                codeLines.add(lineWithIndex);
            }
            notifyDataSetChanged();
        }
    }

    // Method to delete all code content from adapter
    public void clearCodeContent() {
        codeLines.clear();
    }

    // Add a new code line
    public boolean addCodeLine(String newLineContent) {
        boolean status = false;
        if (!("-1").equals(posProgramLine)){
            codeLines.add(Integer.parseInt(posProgramLine) + 1, new ProgramInputCodeLineModel(Integer.parseInt(posProgramLine), newLineContent));
            notifyDataSetChanged();
            status = true;
        }
        else {
            Toast.makeText(activity, "Choose a code line to insert after", Toast.LENGTH_SHORT).show();
        }
        return status;
    }

    // Delete a code line
    public void deleteCodeLine() {
        if (!("-1").equals(posProgramLine)){
            codeLines.remove(Integer.parseInt(posProgramLine));
            notifyDataSetChanged();
        }
        else {
            Toast.makeText(activity, "Choose a code line to delete", Toast.LENGTH_SHORT).show();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView lineIndex;
        final TextView codeContent;
        TableRow tableRow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tableRow = itemView.findViewById(R.id.programInputTableRow);
            lineIndex = itemView.findViewById(R.id.programInputIndexTextView);
            codeContent = itemView.findViewById(R.id.programInputContentTextView);
        }
    }
}
