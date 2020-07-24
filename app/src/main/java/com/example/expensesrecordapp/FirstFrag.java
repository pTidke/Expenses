package com.example.expensesrecordapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.expensesrecordapp.model.Material;
import com.example.expensesrecordapp.model.Work;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FirstFrag extends Fragment implements View.OnClickListener {

    private static final String TAG = "Expenses";

    private MaterialButton datePicker;
    private TextInputEditText nameMaterial, quantity, price, description;
    private AutoCompleteTextView  nameSupplier, nameWork;
    private NestedScrollView nestedScrollView;
    private Calendar calendar;
    private String date;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference works = db.collection("Works");
    private CollectionReference payments = db.collection( "payments" );

    public static List<Material> materialsList = new ArrayList<>();
    public static List<String> allWorks = new ArrayList<>();
    public static List<String> allSuppliers = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        nestedScrollView = (NestedScrollView) inflater.inflate(R.layout.fragment_first, container, false);
        initViews();
        makeWorksList();
        hideKeyboard( nestedScrollView );
        return  nestedScrollView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        calendar = Calendar.getInstance();
    }

    @Override
    public void onClick(View view) {

        String mWork;

        if (view.getId() == R.id.datePicker){
            DatePickerDialog datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getContext()), (view1, year, monthOfYear, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
                datePicker.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }

        if(view.getId() == R.id.btnAddMaterial) {

            try {
                mWork = nameWork.getText().toString().toLowerCase();
                String mMaterial = Objects.requireNonNull( nameMaterial.getText() ).toString().toLowerCase();
                String mSupplier = nameSupplier.getText().toString().toLowerCase();
                int materialQuantity = Integer.parseInt( Objects.requireNonNull( quantity.getText() ).toString() );
                float materialPrice = Float.parseFloat( Objects.requireNonNull( price.getText() ).toString() );
                String mDescription = Objects.requireNonNull( description.getText() ).toString();
                String date = datePicker.getText().toString();
                Material m;

                if (!mDescription.isEmpty()){
                    m = new Material( mMaterial, materialQuantity, materialPrice, mSupplier, date, materialPrice * materialQuantity, mDescription );
                }

                else {
                    m = new Material( mMaterial, materialQuantity, materialPrice, mSupplier, date, materialPrice * materialQuantity, "");
                }

                payments.document(mSupplier).get().addOnCompleteListener( task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "Document exists!");
                            payments.document( mSupplier ).collection( "materials" ).add( m );
                            if (document.getData().get( "grandTotal" ).toString() != null){
                                payments.document( mSupplier )
                                        .update( "grandTotal",
                                                 materialPrice * materialQuantity + Float.parseFloat( document.getData().get( "grandTotal" ).toString() ) );
                            }
                        } else {
                            Log.d(TAG, "Document does not exist!");
                            Map<String, Object> s = new HashMap<>(  );
                            s.put( "payment", 0 );
                            s.put( "paidDates", "" );
                            s.put( "nameSupplier", mSupplier );
                            s.put( "grandTotal", materialPrice * materialQuantity );
                            payments.document( mSupplier ).set(s);
                            payments.document( mSupplier ).collection( "materials" ).document().set( m );
                        }
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                } );

                works.document( mWork ).get().addOnCompleteListener( task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if(document.exists()) {
                            Work w = document.toObject(Work.class);
                            assert w != null;
                            materialsList = w.getMaterials();
                            materialsList.add(m);
                        }
                        else {
                            materialsList.add(m);
                        }
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                });

                nameMaterial.setText("");
                nameSupplier.setText("");
                quantity.setText("");
                price.setText("");
                description.setText("");
                datePicker.setText( R.string.enter_date);

                Snackbar.make( Objects.requireNonNull( getView() ), "Material Added Successfully", Snackbar.LENGTH_LONG ).show();
                hideKeyboard( view );

            }
            catch (Exception e){
                Toast.makeText(view.getContext(), "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }

        if (view.getId() == R.id.btnSaveWorkData){

            try {
                mWork = nameWork.getText().toString().toLowerCase();
                Work w = new Work( mWork, materialsList);
                
                works.document( mWork ).set(w)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Success"))
                        .addOnFailureListener(e -> Log.d(TAG, "Failed"));

                nameWork.setText("");
                materialsList.clear();
                Snackbar.make( Objects.requireNonNull( getView() ), "Work Added Successfully", Snackbar.LENGTH_LONG ).show();
                hideKeyboard( view );
            }
            catch (Exception e){
                Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }

    }

    public void makeWorksList(){
        works.get()
            .addOnCompleteListener( task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull( task.getResult() ))
                        allWorks.add( document.getId() );
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            } );

        payments.get()
                .addOnCompleteListener( task -> {
                    if (task.isSuccessful()){
                        for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull( task.getResult() ))
                            allSuppliers.add( documentSnapshot.getId() );
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                } );
    }

    public void initViews(){
        datePicker = nestedScrollView.findViewById(R.id.datePicker);
        MaterialButton btnAddMaterial = nestedScrollView.findViewById( R.id.btnAddMaterial );
        MaterialButton btnSaveWorkData = nestedScrollView.findViewById( R.id.btnSaveWorkData );
        nameWork = nestedScrollView.findViewById(R.id.nameWork);
        nameMaterial = nestedScrollView.findViewById(R.id.nameMaterial);
        nameSupplier = nestedScrollView.findViewById(R.id.nameSupplier);
        quantity = nestedScrollView.findViewById(R.id.quantity);
        description = nestedScrollView.findViewById( R.id.description );
        price = nestedScrollView.findViewById(R.id.price);
        nestedScrollView = nestedScrollView.findViewById(R.id.nestedScrollView);

        ArrayAdapter<String> adapter = new ArrayAdapter<>( nestedScrollView.getContext(), android.R.layout.simple_dropdown_item_1line, allWorks );
        nameWork.setAdapter(adapter);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>( nestedScrollView.getContext(), android.R.layout.simple_dropdown_item_1line, allSuppliers );
        nameSupplier.setAdapter(adapter2);

        btnAddMaterial.setOnClickListener(this);
        datePicker.setOnClickListener(this);
        btnSaveWorkData.setOnClickListener(this);
    }

    private void hideKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) Objects.requireNonNull( getActivity() ).getSystemService( Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
