package com.example.expensesrecordapp.model;

public class Supplier {

    private float grandTotal;
    private float payment;
    private String nameSupplier;

    public Supplier() {
    }

    public Supplier(float grandTotal, float payment, String nameSupplier){
        this.grandTotal = grandTotal;
        this.payment = payment;
        this.nameSupplier = nameSupplier;
    }

    public float getGrandTotal(){
        return grandTotal;
    }

    public String getNameSupplier(){
        return nameSupplier;
    }

    public float getPayment(){
        return payment;
    }

}
