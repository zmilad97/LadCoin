package com.github.zmilad97.core.Module.Transaction;

import java.util.HashMap;

public class TransactionInput {
    private String previousTransactionHash;
    private String indexReferenced;
    private HashMap<String,String> scriptSignature;

    public String getPreviousTransactionHash() {
        return previousTransactionHash;
    }

    public void setPreviousTransactionHash(String previousTransactionHash) {
        this.previousTransactionHash = previousTransactionHash;
    }

    public String getIndexReferenced() {
        return indexReferenced;
    }

    public void setIndexReferenced(String indexReferenced) {
        this.indexReferenced = indexReferenced;
    }

    public HashMap<String, String> getScriptSignature() {

        return scriptSignature;
    }

    public void setScriptSignature(HashMap<String,String> scriptSignature) {
        this.scriptSignature = scriptSignature;
    }
}
