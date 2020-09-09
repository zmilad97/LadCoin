package com.github.zmilad97.core.Service;


import com.github.zmilad97.core.Module.Block;
import com.github.zmilad97.core.Module.Transaction.Transaction;
import com.github.zmilad97.core.Module.Transaction.TransactionOutput;
import com.github.zmilad97.core.Module.Wallet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

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
        Block genesis = new Block(0, new java.util.Date().toString(), new ArrayList<>());
        genesis.setPreviousHash(null);
        String stringToHash = "" + genesis.getIndex() + genesis.getDate() + genesis.getPreviousHash() + genesis.getTransactions();
        try {
            genesis.setHash(cryptography.toHexString(cryptography.getSha(stringToHash)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return genesis;
    }


    public String getTransactionId() {
        StringBuilder sb = new StringBuilder();
        sb.append("CHA");
        sb.append(chain.size() - 1);
        sb.append("TRX");
        sb.append(currentTransaction.size() - 1);
        sb.append("RAND");
        sb.append(new Random(1024));

        return sb.toString();
    }

    public void addBlock(Block block) {
        if (validMine(block)) {
            LOG.debug(String.valueOf(block.getIndex()));
            doTransactions(block);
            addBlockToChain(block);
//            this.getCurrentTransaction().clear();
            this.currentTransaction.clear();
            LOG.info("Block has been added to chain");
        } else
            LOG.info("Block Is invalid");
    }

    //removing unValid Transaction
    private void doTransactions(@NotNull Block block) {
        LOG.debug("block transaction size " + block.getTransactions().size());
        for (int i = 0; i < block.getTransactions().size(); i++) {
            LOG.debug("i " + i);
            LOG.debug(block.getTransactions().get(0).getTransactionHash());
            if (!(validTransaction(block.getTransactions().get(i))))
                block.getTransactions().remove(block.getTransactions().get(i));
        }
    }


    public boolean validTransaction(@NotNull Transaction transaction) {
        LOG.debug("in valid");
        //just for test
        LOG.debug(transaction.getTransactionInput().getPubKey());
        if (transaction.getTransactionInput().getPubKey().equals("null"))
            return true;

        LOG.debug(transaction.getTransactionInput().getPubKey());
        boolean result = false;
        EncodedKeySpec encodedKeySpec = null;
        try {
            encodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(transaction.getTransactionInput().getPubKey()));
        }
        catch (IllegalArgumentException e){
            LOG.error(e.getLocalizedMessage()); //TODO fix this
        }
        KeyFactory keyFactory;
        PublicKey publicKey = null;
        Signature signature = null;

        try {
            keyFactory = KeyFactory.getInstance("EC");
            publicKey = keyFactory.generatePublic(encodedKeySpec);
            signature = Signature.getInstance("SHA256withECDSA");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOG.error(e.getLocalizedMessage());
        }

        for (int i = 0; i < transaction.getTransactionInput().getPreviousTransactionHash().size(); i++) {
            Transaction trx = findTransactionByTransactionHash(transaction.getTransactionInput().getPreviousTransactionHash().get(i));
            if (trx.getTransactionOutput().getSignature().equals(transaction.getTransactionOutput().getSignature())) {
                try {
                    assert signature != null;
                    signature.initVerify(publicKey);
                    signature.update(Base64.getEncoder().encode(publicKey.getEncoded()));
                    result = signature.verify(Base64.getDecoder().decode(trx.getTransactionOutput().getSignature()));

                } catch (InvalidKeyException | SignatureException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public Transaction findTransactionByTransactionHash(String hash) {
        List<Block> blocks = this.getChain();
        for (int i = blocks.size() - 1; i >= 1; i--)
            for (int j = blocks.get(i).getTransactions().size() - 1; j >= 1; j--)
                if (blocks.get(i).getTransactions().get(j).getTransactionHash().equals(hash))
                    return blocks.get(i).getTransactions().get(j);

        return new Transaction();
    }


    public List<Transaction> findUTXOs(String signature) {
        List<Transaction> UTXOsList = new ArrayList<>();

        List<Block> blockChain = this.getChain();
        for (int i = blockChain.size() - 1; i >= 1; i--) {
            LOG.debug(" i = " + i);
//            LOG.debug(blockChain.get(i).getTransactions().size());
            for (int j = blockChain.get(i).getTransactions().size() - 1; j >= 0; j--) {
                LOG.debug(" J = " + j);
                if (blockChain.get(i).getTransactions().get(j).getTransactionOutput().getSignature().equals(signature)) {
                    LOG.debug("Found");
                    if (UTXOsList.isEmpty()) {
                        LOG.debug("empty");
                        LOG.debug(blockChain.get(i).getTransactions().get(j).getTransactionId());
                        UTXOsList.add(blockChain.get(i).getTransactions().get(j));
                    } else if (transactionIsUnspent(blockChain.get(i).getTransactions().get(j), UTXOsList)) {
                        LOG.debug("UTXOs" + UTXOsList.size());
                        LOG.debug("NOT EMPTY");
                        UTXOsList.add(blockChain.get(i).getTransactions().get(j));
                    }
                }
            }
        }
        Transaction nullTransaction = new Transaction();
        nullTransaction.setTransactionId("404");
        nullTransaction.setTransactionOutput(new TransactionOutput(0, "404"));
        if (UTXOsList.isEmpty())
            UTXOsList.add(nullTransaction);
        return UTXOsList;
    }

    //Check the UTXOs unspent or not
    private boolean transactionIsUnspent(Transaction transaction, List<Transaction> UTXOsList) {
        LOG.debug(" unspent Transactions ");
        for (int i = UTXOsList.size() - 1; i >= 0; i--)
            for (int j = transaction.getTransactionInput().getPreviousTransactionHash().size() - 1; j >= 0; i--)
                if (UTXOsList.get(i).getTransactionHash()
                        .equals(transaction.getTransactionInput().getPreviousTransactionHash().get(j)))
                    return false;

        return true;
    }

    private boolean validMine(@NotNull Block block) {
        try {
            String transactionStringToHash = "";
            for (int i = 0; i < block.getTransactions().size(); i++)
                transactionStringToHash += block.getTransactions().get(i).getTransactionHash();

            String stringToHash = block.getNonce() + block.getIndex() + block.getDate() + block.getPreviousHash() + transactionStringToHash;
            LOG.info(stringToHash);
            LOG.info("Block hash : {}", block.getHash());
            LOG.info("crypto : {} ", cryptography.toHexString(cryptography.getSha(stringToHash)));
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
        if (chain.size() > changeRewardAmountPer)
            this.reward = this.reward / 2;
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

    public void addTransaction(Transaction transaction) {
        currentTransaction.add(transaction);
    }


    public void addWalletToWalletList(Wallet wallet) {
        this.walletList.add(wallet);
    }

    public boolean isCurrentTransactionEmpty() {
        return this.currentTransaction.isEmpty();
    }

    public void clean() {
        currentTransaction.clear();
    }
}
