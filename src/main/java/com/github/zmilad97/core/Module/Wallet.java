/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.github.zmilad97.core.Module;

public class Wallet {
    private String publicSignature;
    private double amount;

    public Wallet(String publicSignature, double amount) {
        this.publicSignature = publicSignature;
        this.amount = amount;
    }
    public Wallet(){

    }

    public String getPublicSignature() {
        return publicSignature;
    }

    public void setPublicSignature(String publicSignature) {
        this.publicSignature = publicSignature;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
