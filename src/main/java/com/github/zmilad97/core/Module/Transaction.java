package com.github.zmilad97.core.Module;

import com.github.zmilad97.core.Service.Cryptography;

import java.security.NoSuchAlgorithmException;

public class Transaction {

    private String transactionId;
    private int outputIndex;
    private String source;
    private String destination;
    private double amount;
    private String transactionHash;

    public Transaction(String source, String destination, double amount) {
        this.source = source;
        this.destination = destination;
        this.amount = amount;
        this.transactionHash = computeTransactionHash();
    }

    public Transaction() {

    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public int getOutputIndex() {
        return outputIndex;
    }

    public void setOutputIndex(int outputIndex) {
        this.outputIndex = outputIndex;
    }

    public String computeTransactionHash() {
        Cryptography cryptography = new Cryptography();
        String stringToHash = this.transactionId + this.source + this.destination + this.amount;
        try {
            return cryptography.toHexString(cryptography.getSha(stringToHash));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
