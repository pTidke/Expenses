package com.example.expensesrecordapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensesrecordapp.model.Material;
import com.example.expensesrecordapp.model.Supplier;
import com.example.expensesrecordapp.ui.main.MatAdapter;
import com.example.expensesrecordapp.ui.main.SupplierAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class ThirdFrag extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference paymentsRef = db.collection("payments");

    private SupplierAdapter adapter;
    private MatAdapter adapter1;
    
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_third, container, false);
        setUpRecyclerView();
        return view;
    }

    private void setUpRecyclerView() {
        Query query = paymentsRef;

        FirestoreRecyclerOptions<Supplier> options = new FirestoreRecyclerOptions.Builder<Supplier>()
                .setQuery( query, Supplier.class )
                .build();

        adapter = new SupplierAdapter(options);

        RecyclerView suppliersRecyclerView = view.findViewById(R.id.suppliersRecyclerView);
        suppliersRecyclerView.setHasFixedSize(true);
        suppliersRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        suppliersRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener( new SupplierAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String nameSupplier, float payment, float grandTotal, int position) {

                BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog( Objects.requireNonNull( getContext() ) );
                View dialogView = Objects.requireNonNull( getActivity() ).getLayoutInflater().inflate(R.layout.bottom_dailog, null);

                TextView tv = dialogView.findViewById(R.id.tv1);
                tv.setText(toTitleCase( nameSupplier ));
                TextView tvPaidTotal = dialogView.findViewById( R.id.paidTotal );
                TextView gt = dialogView.findViewById(R.id.grandTotal1);

                TextInputEditText paidAmount = dialogView.findViewById( R.id.paidAmount );
                MaterialButton pay = dialogView.findViewById( R.id.addPayment );

                tvPaidTotal.setText( "Paid Amount : "  + payment);
                gt.setText("Grand Total : " + grandTotal);

                Query query1 = paymentsRef.document( nameSupplier ).collection( "materials" );

                FirestoreRecyclerOptions<Material> options1 = new FirestoreRecyclerOptions.Builder<Material>()
                        .setQuery(query1, Material.class)
                        .build();

                adapter1 = new MatAdapter(options1);
                adapter1.startListening();
                RecyclerView suppliers1RecyclerView = dialogView.findViewById(R.id.list1);
                suppliers1RecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
                suppliers1RecyclerView.setAdapter(adapter1);

                adapter1.setOnItemClickListener( new MatAdapter.onClickListner() {
                    @Override
                    public void onItemClick(int position, View v) {
                    }
                    @Override
                    public void onItemLongClick(int position, View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder( Objects.requireNonNull( getContext() ) );
                        builder.setMessage("Do you want to Delete this Supplier ?").setTitle("Delete Alert!")
                                .setCancelable(true)
                                .setPositiveButton("Yes", (dialog, id) -> deleteSup(position) )
                                .setNegativeButton("No", (dialog, id) -> dialog.cancel() );

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                } );

                pay.setOnClickListener( v -> {
                    try {
                        paymentsRef.document(nameSupplier).update( "payment", FieldValue.increment( Float.parseFloat( Objects.requireNonNull( paidAmount.getText() ).toString())));
                        paidAmount.setText( "" );
                        mBottomSheetDialog.dismiss();
                        hideKeyboard( v );
                        Snackbar.make( Objects.requireNonNull( getView() ), "Payment Added Successfully!", Snackbar.LENGTH_LONG ).show();
                    }
                    catch (Exception e){
                        Toast.makeText( getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT ).show();
                    }
                } );

                mBottomSheetDialog.setContentView(dialogView);
                mBottomSheetDialog.show();
            }

            @Override
            public void onItemLongClick(int position, View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder( Objects.requireNonNull( getContext() ) );
                builder.setMessage("Do you want to Delete this Supplier ?").setTitle("Delete Alert!")
                        .setCancelable(true)
                        .setPositiveButton("Yes", (dialog, id) -> {
                            if(adapter1.getItemCount() == 0){
                                deleteSupWork(position);
                            } else {
                                Snackbar.make( getView(), "Delete All itens First", Snackbar.LENGTH_LONG ).show();
                            }
                        } )
                        .setNegativeButton("No", (dialog, id) -> dialog.cancel() );

                AlertDialog alert = builder.create();
                alert.show();

            }

            private void deleteSup(int position) {
                try {
                    adapter1.DeleteItem( position );
                    Snackbar.make( Objects.requireNonNull( getView() ), "Supplier Deleted Successfully!", Snackbar.LENGTH_LONG ).show();
                } catch (Exception e){

                }
            }

            private void deleteSupWork(int position) {
                try {
                    adapter.DeleteItem( position );
                    Snackbar.make( Objects.requireNonNull( getView() ), "Supplier Deleted Successfully!", Snackbar.LENGTH_LONG ).show();
                } catch (Exception e){

                }
            }
        } );
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

    private void hideKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) Objects.requireNonNull( getActivity() ).getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);
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

}

