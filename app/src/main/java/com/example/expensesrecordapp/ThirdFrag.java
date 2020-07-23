package com.example.expensesrecordapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
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
import androidx.core.content.ContextCompat;
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
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.google.common.reflect.Reflection.getPackageName;

public class ThirdFrag extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference paymentsRef = db.collection("payments");

    private SupplierAdapter adapter;
    private MatAdapter adapter1;

    private String fileName = "file.txt";
    
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_third, container, false);
        setUpRecyclerView();
        return view;
    }

    public String getStorageDir(String fileName) {
        //create folder
        File file = new File(getContext().getObbDir() + "/Invoices");
        if (!file.mkdirs()) {
            file.mkdirs();
        }
        String filePath = file.getAbsolutePath() + File.separator + fileName;
        return filePath;
    }

    /*private void createPdf(String material, String items){
        // create a new document
        PdfDocument document = new PdfDocument();

        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 1000, 1).create();

        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        int color = ContextCompat.getColor( getContext(), R.color.colorPrimaryDark );
        paint.setColor(color);
        paint.setTextAlign( Paint.Align.CENTER );
        paint.setTextSize( 20f );
        canvas.drawText("Materials Invoice", page.getCanvas().getWidth() / 2 , 30, paint);
        canvas.drawText(material.toUpperCase(), page.getCanvas().getWidth() / 2 , 60, paint);
        paint.setColor( Color.BLACK );
        paint.setTextSize( 16f );
        canvas.drawText( items, 80, 80, paint );
        // finish the page
        document.finishPage(page);
        // draw text on the graphics object of the page

        // Create Page 2
        pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 2).create();
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        paint = new Paint();
        paint.setColor(Color.BLUE);
        canvas.drawCircle(100, 100, 100, paint);
        document.finishPage(page);

        String targetPdf = getStorageDir( "demo.pdf" );
        File filePath = new File(targetPdf);

        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(getContext(), "Done", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("main", "error "+e.toString());
            Toast.makeText(getContext(), "Something wrong: " + e.toString(),  Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();
    }*/

    private  void savepdf(String payDates, String Sup, float payment, float grandTotal)
    {
        Document doc = new Document();
        String mfile = new SimpleDateFormat("HHmmss_yyyy_MM_dd", Locale.getDefault()).format(System.currentTimeMillis());
        String mfilepath = getStorageDir( "INVOICE" + mfile + ".pdf" );
        Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN,20, Font.BOLDITALIC);
        Font small = new Font( Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD );
        BaseColor color = new BaseColor( 99, 54, 82 );
        smallBold.setColor( color );
        try{
            PdfWriter.getInstance(doc,new FileOutputStream(mfilepath));
            doc.open();
            String mtext = MatAdapter.invoice + "\n\n" + payDates;
            doc.addAuthor("Ankush Kakde");
            doc.addTitle( "Invoice" );
            doc.addCreator( "Ankush Kakde" );
            doc.add(new Paragraph( "Invoice", smallBold ) );
            doc.add( new Paragraph( "Supplier Name : " + toTitleCase( Sup ) + "\n\n\n", small) );

            float[] widths = {4, 4, 3, 4, 5, 6};
            PdfPTable table = new PdfPTable(6);
            table.setWidths( widths );
            table.addCell( "Material" );
            table.addCell( "Date" );
            table.addCell( "Quantity" );
            table.addCell( "Price(In Rs.)" );
            table.addCell( "Total (In Rs.)" );
            table.addCell( "Description" );
            table.setWidthPercentage(100);
            List<List<String>> dataset = MatAdapter.mats;
            for (List<String> record : dataset) {
                for (String field : record) {
                    table.addCell(field);
                }
            }
            doc.add(table);

            doc.add(new Paragraph( "\nTimeline-\n" + payDates ));
            doc.add( new Paragraph( "\nGrand Total : " + grandTotal, small) );
            doc.add( new Paragraph( "Paid Amount : " + payment, small) );
            doc.close();
            MatAdapter.invoice = "";
            MatAdapter.mats.clear();
            Toast.makeText(getContext(), "Done", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Toast.makeText(getContext(),"This is Error msg : " +e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void setUpRecyclerView() {
        Query query = paymentsRef;

        FirestoreRecyclerOptions<Supplier> options = new FirestoreRecyclerOptions.Builder<Supplier>()
                .setQuery( query, Supplier.class )
                .build();

        MatAdapter.invoice = "";
        MatAdapter.mats.clear();

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

                if (isExternalStorageAvailable() || isExternalStorageReadable()) {
                    print.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //writeData( getStorageDir( fileName ) );
                            //createPdf( nameSupplier, MatAdapter.invoice.concat( "\n\n" + paidDates2 ) );
                            savepdf(paidDates2, nameSupplier, payment, grandTotal);
                            MatAdapter.invoice = "";
                        }
                    } );
                } else {
                    Toast.makeText( getContext(), "Error!", Toast.LENGTH_SHORT ).show();
                }

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

    //checks if external storage is available for read and write
    public boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //checks if external storage is available for read
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    //write data to file
    public void writeData(String filePath) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(MatAdapter.invoice.getBytes());
            fileOutputStream.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
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

