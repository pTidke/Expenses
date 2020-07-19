package com.example.expensesrecordapp.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Work {
    private String nameWork;
    private List<Material> materials;

    public Work() {
    }

    public Work(String nameWork, List<Material> materials) {
        this.nameWork = nameWork;
        this.materials = materials;
    }

    public float getGrandTotal(){
        Iterator<Material> crunchyIterator = materials.iterator();
        float sum = 0;
        while (crunchyIterator.hasNext()) {
            sum += crunchyIterator.next().getCostTotal();
        }
        return sum;
    }

    public String getNameWork() {
        return nameWork;
    }

    public List<Material> getMaterials() {
        return materials;
    }
}
