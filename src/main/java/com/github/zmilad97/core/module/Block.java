package com.github.zmilad97.core.module;

import com.github.zmilad97.core.module.transaction.Transaction;

import java.util.Collection;
import java.util.List;

public class Block {

    private int index;
    private String date;
    private String hash;
    private String previousHash;
    private long nonce;
    private List<Transaction> transactions;
    private String difficultyLevel;
    private Double reward;

    //    private String transactionsHash = "";


    public Block(int index, String date, List<Transaction> transactions) {
        this.index = index;
        this.date = date;
        this.transactions = transactions;
    }

    public Block() {

    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    public Double getReward() {
        return reward;
    }

    public void setReward(Double reward) {
        this.reward = reward;
    }



    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
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

    public void setTransactions(Collection<Transaction> transactions) {
        this.transactions.clear();
        this.transactions.addAll(transactions);
    }

    @Override
    public String toString() {
        return "Block{" +
            "index=" + index +
            ", date=" + date +
            ", hash='" + hash + '\'' +
            ", previousHash='" + previousHash + '\'' +
            ", nonce=" + nonce +
            ", transactions=" + transactions +
            ", difficultyLevel='" + difficultyLevel + '\'' +
            ", reward=" + reward +
            '}';
    }

}
