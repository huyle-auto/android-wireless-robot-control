package com.example.myrobotapp.Class.DeviceTable.DeviceInfo;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrobotapp.R;

import java.util.List;

public class DeviceTableRowAdapter extends RecyclerView.Adapter<DeviceTableRowAdapter.TableRowViewHolder> {

    private List<DeviceTableRowModel> tableDataList;

    public DeviceTableRowAdapter(List<DeviceTableRowModel> tableDataList) {
        this.tableDataList = tableDataList;
    }

    @NonNull
    @Override
    public TableRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_table_row, parent, false);
        return new TableRowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableRowViewHolder holder, int position) {
        DeviceTableRowModel rowData = tableDataList.get(position);

        holder.idEditText.setText(rowData.getId());
        holder.groupEditText.setText(rowData.getGroup());
        holder.indexEditText.setText(rowData.getIndex());
        holder.subIndexEditText.setText(rowData.getSubIndex());
        holder.sizeEditText.setText(String.valueOf(rowData.getSize()));
        holder.roleEditText.setText(rowData.getRole());

        // Optionally, add listeners for when the user edits the fields
        // to update the corresponding data in the tableDataList
        holder.idEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                rowData.setId(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Similarly, add listeners for other fields
    }

    @Override
    public int getItemCount() {
        return tableDataList.size();
    }

    // ViewHolder class to hold each row's views
    public static class TableRowViewHolder extends RecyclerView.ViewHolder {

        EditText idEditText;
        EditText groupEditText;
        EditText indexEditText;
        EditText subIndexEditText;
        EditText sizeEditText;
        EditText roleEditText;

        public TableRowViewHolder(@NonNull View itemView) {
            super(itemView);
            idEditText = itemView.findViewById(R.id.idEditText);
            groupEditText = itemView.findViewById(R.id.groupEditText);
            indexEditText = itemView.findViewById(R.id.indexEditText);
            subIndexEditText = itemView.findViewById(R.id.subIndexEditText);
            sizeEditText = itemView.findViewById(R.id.sizeEditText);
            roleEditText = itemView.findViewById(R.id.roleEditText);
        }
    }
}
