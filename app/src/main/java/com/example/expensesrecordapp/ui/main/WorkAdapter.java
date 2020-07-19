package com.example.expensesrecordapp.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensesrecordapp.R;
import com.example.expensesrecordapp.model.Work;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class WorkAdapter extends FirestoreRecyclerAdapter<Work, WorkAdapter.WorkHolder> {

    private OnItemClickListener listener;

    public WorkAdapter(@NonNull FirestoreRecyclerOptions<Work> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkHolder workHolder, int i, @NonNull Work work) {
        workHolder.nameWork.setText(toTitleCase(work.getNameWork()));
        workHolder.totalMaterials.setText("Total materials : " + work.getMaterials().size());
    }

    @NonNull
    @Override
    public WorkHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.work_item, parent, false);
        return new WorkHolder(v);
    }

    public void DeleteItem(int position){
        getSnapshots().getSnapshot( position ).getReference().delete();
    }

    class WorkHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{

        TextView nameWork;
        TextView totalMaterials;

        public WorkHolder(@NonNull View itemView) {
            super(itemView);
            nameWork = itemView.findViewById(R.id.nameCardWork);
            totalMaterials = itemView.findViewById(R.id.materials);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null){
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
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
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
        void onItemLongClick(int position, View v);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
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
