package com.github.zmilad97.core.Module.Transaction;


public class TransactionInput {
    private String previousTransactionHash;
    private int indexReferenced;
    private String scriptSignature;

    public String getPreviousTransactionHash() {
        return previousTransactionHash;
    }

    public void setPreviousTransactionHash(String previousTransactionHash) {
        this.previousTransactionHash = previousTransactionHash;
    }

    public int getIndexReferenced() {
        return indexReferenced;
    }

    public void setIndexReferenced(int indexReferenced) {
        this.indexReferenced = indexReferenced;
    }

    public String getScriptSignature() {

        return scriptSignature;
    }

    public void setScriptSignature(String scriptSignature) {
        this.scriptSignature = scriptSignature;
    }
}
