package com.example.expensesrecordapp.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.expensesrecordapp.R;
import com.example.expensesrecordapp.model.Material;
import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {

    private List<Material> mList;
    private static onClickListner onclicklistner;

    public MaterialAdapter(List<Material> mList) {
        this.mList = mList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.material_item, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Material data = mList.get(position);
        holder.nameCardMaterial.setText(toTitleCase(data.getNameMaterial().toUpperCase()));
        holder.nameCardMaterialSupplier.setText("Supplier : " + data.getNameSupplier());
        holder.nameCardDate.setText("Date : " + data.getDate());
        holder.nameCardQuantity.setText("Quantity : " + Integer.toString(data.getQuantity()));
        holder.nameCardTotal.setText("Total Price : " + Float.toString(data.getCostTotal()));
        if (!data.getDescription().isEmpty()){
            holder.getNameCardMaterialDescription.setText( "Description : " + data.getDescription() );
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView nameCardMaterial;
        public TextView nameCardMaterialSupplier;
        public TextView nameCardDate;
        public TextView nameCardQuantity;
        public TextView nameCardTotal;
        public TextView getNameCardMaterialDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            nameCardDate = itemView.findViewById(R.id.nameCardDate);
            nameCardMaterial = itemView.findViewById(R.id.nameCardMaterial);
            nameCardMaterialSupplier = itemView.findViewById(R.id.nameCardMaterialSupplier);
            nameCardQuantity = itemView.findViewById(R.id.nameCardQuantity);
            nameCardTotal = itemView.findViewById(R.id.nameCardTotal);
            getNameCardMaterialDescription = itemView.findViewById( R.id.nameCardMaterialDescription );

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onclicklistner.onItemClick(getAdapterPosition(), v);
        }

        @Override
        public boolean onLongClick(View v) {
            onclicklistner.onItemLongClick(getAdapterPosition(), v);
            return true;
        }
    }

    public void setOnItemClickListener(onClickListner onclicklistner) {
        MaterialAdapter.onclicklistner = onclicklistner;
    }

    public interface onClickListner {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }

    public static String toTitleCase(String str) {

        if (str == null) {
            return null;
        }

        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

}
