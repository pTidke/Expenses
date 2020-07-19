package com.example.expensesrecordapp.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensesrecordapp.R;
import com.example.expensesrecordapp.model.Material;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class MatAdapter extends FirestoreRecyclerAdapter<Material, MatAdapter.MatHolder> {

    private static onClickListner onclicklistner;

    public MatAdapter(@NonNull FirestoreRecyclerOptions<Material> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MatHolder holder, int position, @NonNull Material model) {
        holder.nameCardMaterial.setText( toTitleCase( model.getNameMaterial() ) );
        holder.getNameCardMaterialDescription.setText("Description : " + model.getDescription());
        holder.nameCardDate.setText("Date : " + model.getDate());
        holder.nameCardQuantity.setText("Quantity : " + Integer.toString(model.getQuantity()));
        holder.nameCardTotal.setText("Total Price : " + Float.toString(model.getCostTotal()));
    }

    @NonNull
    @Override
    public MatAdapter.MatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate( R.layout.supplier_item, parent, false);
        return new MatAdapter.MatHolder(v);
    }

    public void DeleteItem(int position){
        getSnapshots().getSnapshot( position ).getReference().delete();
    }

    class MatHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        public TextView nameCardMaterial;
        public TextView nameCardDate;
        public TextView nameCardQuantity;
        public TextView nameCardTotal;
        public TextView getNameCardMaterialDescription;

        public MatHolder(@NonNull View itemView) {
            super(itemView);
            nameCardMaterial = itemView.findViewById( R.id.nameCardMaterial1 );
            nameCardDate = itemView.findViewById( R.id.nameCardDate1 );
            nameCardQuantity = itemView.findViewById( R.id.nameCardQuantity1 );
            nameCardTotal = itemView.findViewById( R.id.nameCardTotal1 );
            getNameCardMaterialDescription = itemView.findViewById( R.id.nameCardDesc1 );

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
        MatAdapter.onclicklistner = onclicklistner;
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

}
