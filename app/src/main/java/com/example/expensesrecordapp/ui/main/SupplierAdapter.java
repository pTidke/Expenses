package com.example.expensesrecordapp.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensesrecordapp.R;
import com.example.expensesrecordapp.model.Supplier;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class SupplierAdapter extends FirestoreRecyclerAdapter<Supplier, SupplierAdapter.SupplierHolder> {

    private SupplierAdapter.OnItemClickListener listener;

    public SupplierAdapter(@NonNull FirestoreRecyclerOptions<Supplier> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull SupplierAdapter.SupplierHolder supplierHolder, int i, @NonNull Supplier supplier) {
        supplierHolder.nameSupplier.setText(toTitleCase(supplier.getNameSupplier()));
        supplierHolder.grandTotal.setText("Total Bill : " + supplier.getGrandTotal());
        supplierHolder.paid.setText( "Paid Amount : " + supplier.getPayment() );
    }

    @NonNull
    @Override
    public SupplierAdapter.SupplierHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.suppliers, parent, false);
        return new SupplierAdapter.SupplierHolder(v);
    }

    public void DeleteItem(int position){
        getSnapshots().getSnapshot( position ).getReference().delete();
    }

    class SupplierHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{

        TextView nameSupplier;
        TextView grandTotal;
        TextView paid;

        public SupplierHolder(@NonNull View itemView) {
            super(itemView);
            nameSupplier = itemView.findViewById(R.id.nameCardSupplier);
            grandTotal = itemView.findViewById(R.id.sMaterials);
            paid = itemView.findViewById( R.id.paid );

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null){
                    listener.onItemClick(
                            getSnapshots().getSnapshot(position).get( "nameSupplier" ).toString(),
                            Float.parseFloat( getSnapshots().getSnapshot(position).get( "payment" ).toString() ),
                            getSnapshots().getSnapshot( position ).get( "paidDates" ).toString(),
                            Float.parseFloat( getSnapshots().getSnapshot(position).get( "grandTotal" ).toString() ),
                            position);
                }
            });
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            listener.onItemLongClick(getAdapterPosition(), v);
            return true;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String nameSupplier, float payment, String paidDates, float grandTotal, int position);
        void onItemLongClick(int position, View v);
    }

    public void setOnItemClickListener(SupplierAdapter.OnItemClickListener listener){
        this.listener = listener;
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
