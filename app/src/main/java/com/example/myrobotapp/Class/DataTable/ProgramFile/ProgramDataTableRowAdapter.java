package com.example.myrobotapp.Class.DataTable.ProgramFile;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.List;

public class ProgramDataTableRowAdapter extends RecyclerView.Adapter<ProgramDataTableRowAdapter.ViewHolder> {

    Context context;
    List<ProgramDataTableRowModel> programList;
    private TableRow selectedTableRow;
    public static String posProgramTable = "0";
    public static String selectedProgramName;

    public ProgramDataTableRowAdapter(Context context, List<ProgramDataTableRowModel> programList) {
        this.context = context;
        this.programList = programList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.program_list_table_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramDataTableRowAdapter.ViewHolder holder, int position) {
        if (programList != null && !programList.isEmpty()) {
            // Get current List child from List<DataTableRowModel>
            ProgramDataTableRowModel programDataTableRowModel = programList.get(position);

            // On every table row bind. Check if that row is the selected one and highlight accordingly
            if (position == Integer.parseInt(posProgramTable)) {
                holder.tableRow.setBackgroundColor(Color.parseColor("#A9A9A9")); // Selected color
                selectedProgramName = programDataTableRowModel.getProgramName();
                System.out.println("Selected program name is: " + selectedProgramName);
            } else {
                holder.tableRow.setBackgroundColor(Color.parseColor("#4E1496BB")); // Unselected color
            }

            // Set highlight listener for Table Rows
            holder.tableRow.setClickable(true);
            holder.tableRow.setFocusable(true);
            holder.tableRow.setOnClickListener(v -> {
                posProgramTable = String.valueOf(holder.getAdapterPosition());
                notifyDataSetChanged();
            });

            // Get Model's fields and apply listeners
            // Program Name
            holder.programNameEditText.setText(programDataTableRowModel.getProgramName());

            // Program Name
            holder.programNameEditText.setText(programDataTableRowModel.getProgramName());
            holder.programNameEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    //saveProgramRow(holder.getAdapterPosition(), 1, editable.toString());
                }
            });


            // Tag
            holder.tagEditText.setText(programDataTableRowModel.getTag());
            holder.tagEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    //saveProgramRow(holder.getAdapterPosition(), 2, editable.toString());
                }
            });

            // Timestamp
            holder.timeStampEditText.setText(programDataTableRowModel.getTimeStamp());
            holder.timeStampEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    //saveProgramRow(holder.getAdapterPosition(), 3, editable.toString());
                }
            });

            // Attribute
            holder.attributeEditText.setText(programDataTableRowModel.getAttribute());
            holder.attributeEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    //saveProgramRow(holder.getAdapterPosition(), 4, editable.toString());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return programList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView programNameEditText, tagEditText, timeStampEditText, attributeEditText;
        TableRow tableRow;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tableRow = itemView.findViewById(R.id.programTableRow);
            programNameEditText = itemView.findViewById(R.id.programNameEditText);
            tagEditText = itemView.findViewById(R.id.tagEditText);
            timeStampEditText = itemView.findViewById(R.id.timeStampEditText);
            attributeEditText = itemView.findViewById(R.id.attributeEditText);
        }
    }

    // Highlight  and display selected row's info for displaying Parameters
    public void highlightRow(TableRow tableRow) {
        // Reset the previously selected button
        if (selectedTableRow != null) {
            selectedTableRow.setBackgroundColor(Color.parseColor("#4E1496BB")); // Unselected color
        }

        // Highlight the currently selected button
        tableRow.setBackgroundColor(Color.parseColor("#A9A9A9")); // Selected color
        selectedTableRow = tableRow; // Update the selected button
    }
}
