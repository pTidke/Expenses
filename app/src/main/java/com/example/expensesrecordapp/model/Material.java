package com.example.expensesrecordapp.model;

public class Material{

    private String nameMaterial;
    private int quantity;
    private float price;
    private String nameSupplier;
    private String date;
    private float costTotal;
    private String description;

    public Material(String nameMaterial, int quantity, float price, String nameSupplier, String date, float costTotal, String description) {
        this.nameMaterial = nameMaterial;
        this.quantity = quantity;
        this.price = price;
        this.nameSupplier = nameSupplier;
        this.date = date;
        this.costTotal = costTotal;
        this.description = description;
    }

    public Material() {
    }

    public Material(String nameMaterial, int quantity, float price, String nameSupplier, String date, float costTotal) {
        this.nameMaterial = nameMaterial;
        this.quantity = quantity;
        this.price = price;
        this.nameSupplier = nameSupplier;
        this.date = date;
        this.costTotal = costTotal;
    }

    public String getDescription() {
        return description;
    }

    public String getNameMaterial() {
        return nameMaterial;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getPrice() {
        return price;
    }

    public String getNameSupplier() {
        return nameSupplier;
    }

    public String getDate() {
        return date;
    }

    public float getCostTotal() {
        return costTotal;
    }

    @Override
    public String toString() {
        return  "Material Name : " + nameMaterial + '\n' +
                "\tDate : " + date + '\n' +
                "\tQuantity : " + quantity + '\n' +
                "\tPrice : " + price + '\n' +
                "\tTotal Cost : " + costTotal + '\n' +
                "\tDescription : " + description + '\n';
    }

    public String getString() {
        return "" + nameMaterial + "          "
                + date + "          "
                + quantity + "           "
                + price + "     "
                + costTotal + "              "
                + description;
    }
}
