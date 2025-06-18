package com.example.myrobotapp.Class.DataTable.Joint;

import static com.example.myrobotapp.Fragments.Features.DataFragment.saveJointRow;

import android.app.Activity;
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

import com.example.myrobotapp.Class.UserInput.NumpadPopup;
import com.example.myrobotapp.R;

import java.util.List;

public class JointDataTableRowAdapter extends RecyclerView.Adapter<JointDataTableRowAdapter.ViewHolder>{
    Context context;
    List<JointDataTableRowModel> jointDataList;
    private TableRow selectedTableRow;
    public static String posJointTable = "-1";

    public JointDataTableRowAdapter(Context context, List<JointDataTableRowModel> jointDataList ){
        this.context = context;
        this.jointDataList = jointDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.data_joint_table_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (jointDataList != null && !jointDataList.isEmpty()){
            // Get current List child from List<DataTableRowModel>
            JointDataTableRowModel jointDataTableRowModel = jointDataList.get(position);

            // On every table row bind. Check if that row is the selected one and highlight accordingly
            if (position == Integer.parseInt(posJointTable)) {
                holder.tableRow.setBackgroundColor(Color.parseColor("#A9A9A9")); // Selected color
            } else {
                holder.tableRow.setBackgroundColor(Color.parseColor("#4E1496BB")); // Unselected color
            }

            // Set highlight listener for Table Rows
            holder.tableRow.setClickable(true);
            holder.tableRow.setFocusable(true);
            holder.tableRow.setOnClickListener(v -> {
                // Highlight row when clicked and save all position before notify Dataset Changed
                posJointTable =  jointDataList.get(position).getNo();
                notifyDataSetChanged();
            });

            // Get Model's fields and apply listeners
            // No.
            holder.indexTextView.setText(jointDataTableRowModel.getNo());

            // X
            holder.j1EditText.setText(jointDataTableRowModel.getJ1());
            holder.j1EditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveJointRow(holder.getAdapterPosition(), 1, editable.toString());
                }
            });

            // Y
            holder.j2EditText.setText(jointDataTableRowModel.getJ2());
            holder.j2EditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveJointRow(holder.getAdapterPosition(), 2, editable.toString());
                }
            });

            // Z
            holder.j3EditText.setText(jointDataTableRowModel.getJ3());
            holder.j3EditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveJointRow(holder.getAdapterPosition(), 3, editable.toString());
                }
            });

            // RX
            holder.j4EditText.setText(jointDataTableRowModel.getJ4());
            holder.j4EditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveJointRow(holder.getAdapterPosition(), 4, editable.toString());
                }
            });

            // RY
            holder.j5EditText.setText(jointDataTableRowModel.getJ5());
            holder.j5EditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveJointRow(holder.getAdapterPosition(), 5, editable.toString());
                }
            });

            // RZ
            holder.j6EditText.setText(jointDataTableRowModel.getJ6());
            holder.j6EditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveJointRow(holder.getAdapterPosition(), 6, editable.toString());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return jointDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView indexTextView;
        EditText j1EditText, j2EditText, j3EditText, j4EditText, j5EditText, j6EditText;
        TableRow tableRow;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tableRow = itemView.findViewById(R.id.jointTableRow);
            indexTextView = itemView.findViewById(R.id.jointIndexTextView);

            j1EditText = itemView.findViewById(R.id.j1EditText);
            j1EditText.setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(context, j1EditText, 1);
                numpadPopup.showNumpadPopup((Activity) context);
            });

            j2EditText = itemView.findViewById(R.id.j2EditText);
            j2EditText.setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(context, j2EditText, 1);
                numpadPopup.showNumpadPopup((Activity) context);
            });

            j3EditText = itemView.findViewById(R.id.j3EditText);
            j3EditText.setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(context, j3EditText, 1);
                numpadPopup.showNumpadPopup((Activity) context);
            });

            j4EditText = itemView.findViewById(R.id.j4EditText);
            j4EditText.setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(context, j4EditText, 1);
                numpadPopup.showNumpadPopup((Activity) context);
            });

            j5EditText = itemView.findViewById(R.id.j5EditText);
            j5EditText.setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(context, j5EditText, 1);
                numpadPopup.showNumpadPopup((Activity) context);
            });

            j6EditText = itemView.findViewById(R.id.j6EditText);
            j6EditText.setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(context, j6EditText, 1);
                numpadPopup.showNumpadPopup((Activity) context);
            });
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
