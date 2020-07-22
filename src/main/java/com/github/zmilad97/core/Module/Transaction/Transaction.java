package com.github.zmilad97.core.Module.Transaction;


public class Transaction {

    private String transactionId;       //TODO need to fix this class
    private TransactionInput TransactionInput;
    private TransactionOutput TransactionOutput;
    private String transactionHash;


    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionInput getTransactionInput() {
        return TransactionInput;
    }

    public void setTransactionInput(TransactionInput transactionInput) {
        this.TransactionInput = transactionInput;
    }

    public TransactionOutput getTransactionOutput() {
        return TransactionOutput;
    }

    public void setTransactionOutput(TransactionOutput transactionOutput) {
        this.TransactionOutput = transactionOutput;
    }

    public String getTransactionHash() {   //TODO : Think About This Method

        return this.transactionHash;
    }
}