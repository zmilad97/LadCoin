package com.github.zmilad97.core.module.transaction;


import java.util.HashMap;

public class TransactionInput {
    private HashMap<Integer,String> previousTransactionHash;
    private int indexReferenced;
    private String pubKey;

    public TransactionInput() {
        this.previousTransactionHash = new HashMap<Integer, String>();
    }

    public void addPreviousTransactionHash(int i, String s ){
        previousTransactionHash.put(i,s);
    }
    public void setPreviousTransactionHash(HashMap<Integer, String> previousTransactionHash) {
        this.previousTransactionHash = previousTransactionHash;
    }

    public HashMap<Integer, String> getPreviousTransactionHash() {
        return previousTransactionHash;
    }

    public int getIndexReferenced() {
        return indexReferenced;
    }

    public void setIndexReferenced(int indexReferenced) {
        this.indexReferenced = indexReferenced;
    }

    public String getPubKey() {

        return pubKey;
    }

    public void setPubKey(String scriptSignature) {
        this.pubKey = scriptSignature;
    }
}
