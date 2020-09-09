package com.github.zmilad97.core.Module.Transaction;

public class TransactionOutput {
    private double amount;
    private String signature;

    public TransactionOutput(double amount, String signature) {
        this.amount = amount;
        this.signature = signature;
    }

    public TransactionOutput() {

    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
