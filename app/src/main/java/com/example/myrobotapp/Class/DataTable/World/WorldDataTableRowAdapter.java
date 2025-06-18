package com.example.myrobotapp.Class.DataTable.World;

import static com.example.myrobotapp.Fragments.Features.DataFragment.saveWorldRow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
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

import java.text.DecimalFormat;
import java.util.List;

public class WorldDataTableRowAdapter extends RecyclerView.Adapter<WorldDataTableRowAdapter.ViewHolder> {

    Context context;
    List<WorldDataTableRowModel> worldDataList;
    private TableRow selectedTableRow;
    public static String posWorldTable = "-1";

    public WorldDataTableRowAdapter(Context context, List<WorldDataTableRowModel> worldDataList ){
        this.context = context;
        this.worldDataList = worldDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.data_world_table_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (worldDataList != null && !worldDataList.isEmpty()){
            // Get current List child from List<DataTableRowModel>
            WorldDataTableRowModel worldDataTableRowModel = worldDataList.get(position);

            // On every table row bind. Check if that row is the selected one and highlight accordingly
            if (position == Integer.parseInt(posWorldTable)) {
                holder.tableRow.setBackgroundColor(Color.parseColor("#A9A9A9")); // Selected color
            } else {
                holder.tableRow.setBackgroundColor(Color.parseColor("#4E1496BB")); // Unselected color
            }

            // Set highlight listener for Table Rows
            holder.tableRow.setClickable(true);
            holder.tableRow.setFocusable(true);
            holder.tableRow.setOnClickListener(v -> {
                //highlightRow(holder.tableRow);
                posWorldTable =  worldDataList.get(position).getNo();
                notifyDataSetChanged();
            });

            // Get Model's fields and apply listeners
            // No.
            holder.indexTextView.setText(worldDataTableRowModel.getNo());

            // X
            holder.xEditText.setText(worldDataTableRowModel.getX());
            holder.xEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveWorldRow(holder.getAdapterPosition(), 1, editable.toString());
                }
            });


            // Y
            holder.yEditText.setText(worldDataTableRowModel.getY());
            holder.yEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveWorldRow(holder.getAdapterPosition(), 2, editable.toString());
                }
            });

            // Z
            holder.zEditText.setText(worldDataTableRowModel.getZ());
            holder.zEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveWorldRow(holder.getAdapterPosition(), 3, editable.toString());
                }
            });

            // RX
            holder.rXEditText.setText(worldDataTableRowModel.getRx());
            holder.rXEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveWorldRow(holder.getAdapterPosition(), 4, editable.toString());
                }
            });

            // RY
            holder.rYEditText.setText(worldDataTableRowModel.getRy());
            holder.rYEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveWorldRow(holder.getAdapterPosition(), 5, editable.toString());
                }
            });

            // RZ
            holder.rZEditText.setText(worldDataTableRowModel.getRz());
            holder.rZEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    saveWorldRow(holder.getAdapterPosition(), 6, editable.toString());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return worldDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView indexTextView;
        EditText xEditText, yEditText, zEditText, rXEditText, rYEditText, rZEditText;
        TableRow tableRow;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tableRow = itemView.findViewById(R.id.worldTableRow);
            indexTextView = itemView.findViewById(R.id.worldIndexTextView);

            xEditText = itemView.findViewById(R.id.xEditText);
            xEditText.setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(context, xEditText, 1);
                numpadPopup.showNumpadPopup((Activity) context);
            });

            yEditText = itemView.findViewById(R.id.yEditText);
            yEditText.setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(context, yEditText, 1);
                numpadPopup.showNumpadPopup((Activity) context);
            });

            zEditText = itemView.findViewById(R.id.zEditText);
            zEditText.setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(context, zEditText, 1);
                numpadPopup.showNumpadPopup((Activity) context);
            });

            rXEditText = itemView.findViewById(R.id.rXEditText);
            rXEditText.setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(context, rXEditText, 1);
                numpadPopup.showNumpadPopup((Activity) context);
            });

            rYEditText = itemView.findViewById(R.id.rYEditText);
            rYEditText.setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(context, rYEditText, 1);
                numpadPopup.showNumpadPopup((Activity) context);
            });

            rZEditText = itemView.findViewById(R.id.rZEditText);
            rZEditText.setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(context, rZEditText, 1);
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
