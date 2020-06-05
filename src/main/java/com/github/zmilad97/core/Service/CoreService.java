package com.github.zmilad97.core.Service;


import com.github.zmilad97.core.Module.Block;
import com.github.zmilad97.core.Module.Transaction;
import com.github.zmilad97.core.Module.Wallet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoreService {

    private static final Logger LOG = LoggerFactory.getLogger(CoreService.class);
    private String difficultyLevel = "ab";
    private char conditionChar = 98;
    private double reward = 50;
    private final int changeRewardAmountPer = 5;
    Cryptography cryptography;
    private List<Block> chain;
    private List<Transaction> currentTransaction;
    private List<Wallet> walletList;

    public CoreService() {
        cryptography = new Cryptography();
        currentTransaction = new ArrayList<>();
        walletList = new ArrayList<>();
        chain = new ArrayList<>();
        chain.add(generateGenesis());

//        Transaction transaction = new Transaction("test 1" , " test 2", 50);
//        currentTransaction.add(transaction);

    }

    @NotNull
    private Block generateGenesis() {
        Block genesis = new Block(0, new java.util.Date().toString(), null);
        genesis.setPreviousHash(null);
        String stringToHash = "" + genesis.getIndex() + genesis.getDate() + genesis.getPreviousHash() + genesis.getTransactions();
        try {
            genesis.setHash(cryptography.toHexString(cryptography.getSha(stringToHash)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return genesis;
    }

    public void addBlock(Block block) {
        if (validMine(block)) {
            this.doTransactions(block);

            this.addBlockToChain(block);
            this.getCurrentTransaction().clear();
            LOG.info("Block has been added to chain");

        } else
            LOG.info("Block Is invalid");
    }


    private void doTransactions(@NotNull Block block) {
        for (int i = 0; i < block.getTransactions().size(); i++) {
            validTransaction(block.getTransactions().get(i));
        }
    }

    public void validTransaction(@NotNull Transaction transaction) //TODO fix Transaction then fix this method
     {
        //finding Wallets
        Wallet sourceWallet = null;
        Wallet destinationWallet= null;

        for (int i = 0; i < this.getWalletList().size(); i++) {
            System.out.println(this.getWalletList().get(i).getPublicSignature());
            if (this.getWalletList().get(i).getPublicSignature().equals(transaction.getSource()))
                 sourceWallet = this.getWalletList().get(i);
            if(this.getWalletList().get(i).getPublicSignature().equals(transaction.getDestination()))
                destinationWallet = this.getWalletList().get(i);

            }
            if (sourceWallet != null && destinationWallet != null)
                if (sourceWallet.getAmount() > transaction.getAmount()) {
                    
                    for (int i = 0; i <this.getWalletList().size(); i++) {

                        if (this.getWalletList().get(i).getPublicSignature().equals(transaction.getSource()))
                         this.getWalletList().get(i).setAmount(sourceWallet.getAmount() - transaction.getAmount());
                        if (this.getWalletList().get(i).getPublicSignature().equals(transaction.getDestination()))
                             this.getWalletList().get(i).setAmount(destinationWallet.getAmount()+transaction.getAmount());
                    }
                }
        }

    private boolean validMine(@NotNull Block block) {
        try {
            String transactionStringToHash = "";
            for (int i = 0; i < block.getTransactions().size(); i++)
                transactionStringToHash += block.getTransactions().get(i).getTransactionHash();

            String stringToHash = block.getNonce() + block.getIndex() +block.getDate()+ block.getPreviousHash() + transactionStringToHash;
            LOG.info(stringToHash);
            LOG.info("Block hash : {}",block.getHash());
            LOG.info("crypto : {} ",cryptography.toHexString(cryptography.getSha(stringToHash)));
            if (cryptography.toHexString(cryptography.getSha(stringToHash)).equals(block.getHash()))
                return true;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setDifficultyLevel() {
        if (chain.size() % 5 == 0)
            this.difficultyLevel += ++conditionChar;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public char getConditionChar() {
        return conditionChar;
    }

    public void setConditionChar(char conditionChar) {
        this.conditionChar = conditionChar;
    }

    public double getReward() {
        return reward;
    }

    public void setReward() {
        if(chain.size()>changeRewardAmountPer)
        this.reward = this.reward/2;
    }

    public List<Block> getChain() {
        return chain;
    }

    public void addBlockToChain(Block block) {
        chain.add(block);
    }


    public List<Transaction> getCurrentTransaction() {
        return currentTransaction;
    }

    public List<Wallet> getWalletList() {
        return walletList;
    }

    public void addTransaction(Transaction transaction){
        currentTransaction.add(transaction);
    }


    public void addWalletToWalletList(Wallet wallet){
        this.walletList.add(wallet);
    }

    public boolean isCurrentTransactionEmpty(){
       return this.currentTransaction.isEmpty();
    }

    public void clean() {
        currentTransaction.clear();
    }
}
