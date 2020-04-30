package com.github.zmilad97.core.Service;


import com.github.zmilad97.core.Module.Block;
import com.github.zmilad97.core.Module.Transaction;
import com.github.zmilad97.core.Module.Wallet;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoreService {

    private String difficultyLevel = "ab";
    private char conditionChar = 98;
    private double reward = 50;
    private List<Block> chain;
    private List<Transaction> currentTransaction;

    private List<Wallet> walletList;

    Cryptography cryptography;

    public CoreService() {
        cryptography = new Cryptography();
        currentTransaction = new ArrayList<>();
        walletList = new ArrayList<>();
        chain = new ArrayList<>();
        chain.add(generateGenesis());

      /* // test
        walletList.add(new Wallet("a", 500));
        walletList.add(new Wallet("b", 50));
        currentTransaction.add(new Transaction("test", "a", "b", 20));*/

    }

    @NotNull
    private Block generateGenesis() {
        Block genesis = new Block(0, new java.util.Date(), null);
        genesis.setPreviousHash(null);
        String stringToHash = "" + genesis.getIndex() + genesis.getDate() + genesis.getPreviousHash() + genesis.getTransactions();
        try {
            genesis.setHash(cryptography.toHexString(cryptography.getSha(stringToHash)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return genesis;
    }

    public void addBlock(Block block, CoreService coreService) {
        if (validMine(block)) {
            coreService.doTransactions(block, coreService);
            coreService.addBlockToChain(block);
            coreService.getCurrentTransaction().clear();
            System.out.println("Block has been added to chain");

        } else
            System.out.println("Block Is invalid");
    }


    private void doTransactions(@NotNull Block block, CoreService coreService) {
        for (int i = 0; i < block.getTransactions().size(); i++) {
            validTransaction(block.getTransactions().get(i), coreService);
        }
    }

    public void validTransaction(@NotNull Transaction transaction, @NotNull CoreService coreService) {
        //finding Wallets
        Wallet sourceWallet = null;
        Wallet destinationWallet= null;

        System.out.println(transaction.getTransactionId() +"  ::  "+ transaction.getSource());
        for (int i = 0; i < coreService.getWalletList().size(); i++) {
            System.out.println(coreService.getWalletList().get(i).getPublicSignature());
            if (coreService.getWalletList().get(i).getPublicSignature().equals(transaction.getSource()))
                 sourceWallet = coreService.getWalletList().get(i);
            if(coreService.getWalletList().get(i).getPublicSignature().equals(transaction.getDestination()))
                destinationWallet = coreService.getWalletList().get(i);

            }
            if (sourceWallet != null && destinationWallet != null)
                if (sourceWallet.getAmount() > transaction.getAmount()) {
                    
                    for (int i = 0; i <coreService.getWalletList().size(); i++) {

                        if (coreService.getWalletList().get(i).getPublicSignature().equals(transaction.getSource()))
                         coreService.getWalletList().get(i).setAmount(sourceWallet.getAmount() - transaction.getAmount());
                        if (coreService.getWalletList().get(i).getPublicSignature().equals(transaction.getDestination()))
                             coreService.getWalletList().get(i).setAmount(destinationWallet.getAmount()+transaction.getAmount());
                    }
                }
        }

    private boolean validMine(@NotNull Block block) {
        try {
            String transactionStringToHash = "";
            for (int i = 0; i < block.getTransactions().size(); i++)
                transactionStringToHash += block.getTransactions().get(i).getTransactionHash();

            String stringToHash = block.getNonce() + block.getIndex() + block.getPreviousHash() + transactionStringToHash;
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

    public void setReward(double reward) {
        this.reward = reward;
    }

    public List<Block> getChain() {
        return chain;
    }

    public void addBlockToChain(Block block) {
        chain.add(block);
    }

    public void setChain(List<Block> chain) {
        this.chain = chain;
    }

    public List<Transaction> getCurrentTransaction() {
        return currentTransaction;
    }

    public void setCurrentTransaction(List<Transaction> currentTransaction) {
        this.currentTransaction = currentTransaction;
    }
    public List<Wallet> getWalletList() {
        return walletList;
    }

    public void setWalletList(List<Wallet> walletList) {
        this.walletList = walletList;
    }

    public void addWalletToWalletList(Wallet wallet){
        this.walletList.add(wallet);
    }
}
