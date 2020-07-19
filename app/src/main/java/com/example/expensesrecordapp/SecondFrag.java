package com.example.expensesrecordapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensesrecordapp.model.Material;
import com.example.expensesrecordapp.model.Work;
import com.example.expensesrecordapp.ui.main.MaterialAdapter;
import com.example.expensesrecordapp.ui.main.WorkAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Objects;

public class SecondFrag extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference worksRef = db.collection("Works");

    private WorkAdapter adapter;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_second, container, false);
        setUpRecyclerView();
        return view;
    }

    private void setUpRecyclerView() {

        Query query = worksRef;

        FirestoreRecyclerOptions<Work> options = new FirestoreRecyclerOptions.Builder<Work>()
                .setQuery(query, Work.class)
                .build();

        adapter = new WorkAdapter(options);

        RecyclerView worksRecyclerView = view.findViewById(R.id.worksRecyclerView);
        worksRecyclerView.setHasFixedSize(true);
        worksRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        worksRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener( new WorkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Work work = documentSnapshot.toObject(Work.class);
                List<Material> mList = Objects.requireNonNull( work ).getMaterials();

                BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog( Objects.requireNonNull( getContext() ) );
                View dialogView = Objects.requireNonNull( getActivity() ).getLayoutInflater().inflate(R.layout.dialog_bottom_sheet, null);

                TextView tv = dialogView.findViewById(R.id.tv);
                TextView gt = dialogView.findViewById(R.id.grandTotal);
                RecyclerView listView = dialogView.findViewById(R.id.list);

                tv.setText(toTitleCase(work.getNameWork()));
                gt.setText("Grand Total : " + Float.toString(work.getGrandTotal()));

                MaterialAdapter materialAdapter = new MaterialAdapter(mList);
                listView.setHasFixedSize(true);
                listView.setLayoutManager(new LinearLayoutManager(mBottomSheetDialog.getContext()));
                listView.setAdapter(materialAdapter);

                materialAdapter.setOnItemClickListener( new MaterialAdapter.onClickListner() {
                    @Override
                    public void onItemClick(int position, View v) {

                    }

                    @Override
                    public void onItemLongClick(int position, View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder( Objects.requireNonNull( getContext() ) );
                        builder.setMessage("Do you want to Delete this Material ?").setTitle("Delete Alert!")
                                .setCancelable(true)
                                .setPositiveButton("Yes", (dialog, id) -> DeleteMaterial() )
                                .setNegativeButton("No", (dialog, id) -> dialog.cancel() );

                        AlertDialog alert = builder.create();
                        alert.show();
                    }

                    private void DeleteMaterial() {

                        try {
                            if(mList.size() > 1){
                                mList.remove( position );
                                Work w = new Work(work.getNameWork(), mList );

                                worksRef.document(work.getNameWork()).set(w)
                                        .addOnSuccessListener(aVoid -> Log.d("TAG", "Success"))
                                        .addOnFailureListener(e -> Log.d("TAG", "Failed"));
                            }
                            else {
                                adapter.DeleteItem( position );
                            }
                            materialAdapter.notifyDataSetChanged();
                            adapter.notifyDataSetChanged();
                            mBottomSheetDialog.dismiss();
                            Snackbar.make( getView(), "Material Deleted!", Snackbar.LENGTH_LONG ).show();
                        }
                        catch (Exception e){
                            Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }

                } );

                mBottomSheetDialog.setContentView(dialogView);
                mBottomSheetDialog.show();
            }

            @Override
            public void onItemLongClick(int position, View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Do you want to Delete this Work ?").setTitle("Delete Alert!")
                        .setCancelable(true)
                        .setPositiveButton( "Yes", (dialog, which) -> {
                            adapter.DeleteItem( position );
                            Snackbar.make( getView(), "Work Deleted!", Snackbar.LENGTH_LONG ).show();
                        } )
                        .setNegativeButton("No", (dialog, id) -> dialog.cancel() );

                AlertDialog alert = builder.create();
                alert.show();
            }
        } );

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
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
