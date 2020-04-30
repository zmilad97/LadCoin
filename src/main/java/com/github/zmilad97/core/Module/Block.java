package com.github.zmilad97.core.Module;

import java.util.Date;
import java.util.List;

public class Block {

    private int index;
    private Date date;
    private String hash;
    private String previousHash;
    private long nonce;
    private List<Transaction> transactions;
//    private String transactionsHash = "";

    public Block() {

    }

    public Block(int index, Date date, List<Transaction> transactions) {
        this.index = index;
        this.date = date;
        this.transactions = transactions;
    }



    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction) {
            this.transactions.add(transaction);
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
