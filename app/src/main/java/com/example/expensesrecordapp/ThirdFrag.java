package com.example.expensesrecordapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
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
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.google.common.reflect.Reflection.getPackageName;

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
            public void onItemClick(String nameSupplier, float payment, String paidDates2, float grandTotal, int position) {

                BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog( Objects.requireNonNull( getContext() ) );
                View dialogView = Objects.requireNonNull( getActivity() ).getLayoutInflater().inflate(R.layout.bottom_dailog, null);

                TextView tv = dialogView.findViewById(R.id.tv1);
                tv.setText(toTitleCase( nameSupplier ));
                TextView tvPaidTotal = dialogView.findViewById( R.id.paidTotal );
                TextView gt = dialogView.findViewById(R.id.grandTotal1);
                TextView paidDates = dialogView.findViewById( R.id.paidDates );

                //pdfSetup(dialogView, nameSupplier);

                TextInputEditText paidAmount = dialogView.findViewById( R.id.paidAmount );
                MaterialButton pay = dialogView.findViewById( R.id.addPayment );
                MaterialButton print = dialogView.findViewById( R.id.print );

                tvPaidTotal.setText( "Paid Amount : "  + payment);
                gt.setText("Grand Total : " + grandTotal);
                paidDates.setText( paidDates2 );

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

                        AlertDialog.Builder builder = new AlertDialog.Builder( Objects.requireNonNull( getContext() ) );
                        builder.setMessage("Payment amount is : Rs." + paidAmount.getText().toString()).setTitle("Payment Confirmation!")
                                .setCancelable(true)
                                .setPositiveButton( "Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        paymentsRef.document(nameSupplier).update( "payment", FieldValue.increment( Float.parseFloat( Objects.requireNonNull( paidAmount.getText() ).toString())));
                                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                        paymentsRef.document(nameSupplier).update( "paidDates" , paidDates2 + paidAmount.getText().toString() + "Rs. : On " + date  + "\n" );
                                        paidDates.append( paidAmount.getText().toString() + " : On " + date  + "\n");
                                        hideKeyboard( v );
                                        Snackbar.make( Objects.requireNonNull( getView() ), "Payment Added Successfully!", Snackbar.LENGTH_LONG ).show();
                                    }
                                } )
                                .setNegativeButton("No", (dialog, id) -> dialog.cancel() );

                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    catch (Exception e){
                        Toast.makeText( getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT ).show();
                    }
                } );
                print.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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

