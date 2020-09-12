package com.github.zmilad97.core.Service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zmilad97.core.Module.Block;
import com.github.zmilad97.core.Module.Transaction.Transaction;
import com.github.zmilad97.core.Module.Transaction.TransactionOutput;
import com.github.zmilad97.core.Module.Wallet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private static final int CHANGE_REWARD_AMOUNT_PER = 5;
    private String difficultyLevel = "ab";
    private char conditionChar = 98;
    private double reward = 50;
    Cryptography cryptography;
    private List<Block> chain;
    private List<Transaction> currentTransaction;
    private List<Wallet> walletList;
    private List<String> nodes;

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
        genesis.setHash(cryptography.toHexString(cryptography.getSha(stringToHash)));
        return genesis;
    }


    public String getTransactionId() {
        return "CHA" + (chain.size() - 1) + "TRX" + (currentTransaction.size() - 1) + "RAND" + (new Random(1024));

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
            if (!(block.getTransactions().get(i).getTransactionId().equals("REWARD" + block.getIndex()))) {
                LOG.debug("i " + i);
                LOG.debug(block.getTransactions().get(0).getTransactionHash());
                if (!(validTransaction(block.getTransactions().get(i))))
                    block.getTransactions().remove(block.getTransactions().get(i));
            }
        }
    }


    public boolean validTransaction(@NotNull Transaction transaction) {
        if(transaction.getTransactionId().startsWith("REWARD"))
            return true;
        LOG.debug(transaction.getTransactionInput().getPubKey());
        if (transaction.getTransactionInput().getPubKey().equals("null"))
            return true;

        LOG.debug(transaction.getTransactionInput().getPubKey());
        boolean result = false;
        EncodedKeySpec encodedKeySpec = null;
        try {
            encodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(transaction.getTransactionInput().getPubKey()));
        } catch (IllegalArgumentException e) {
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
            for (int j = transaction.getTransactionInput().getPreviousTransactionHash().size() - 1; j >= 0; j--)
                if (UTXOsList.get(i).getTransactionHash()
                        .equals(transaction.getTransactionInput().getPreviousTransactionHash().get(j)))
                    return false;

        return true;
    }

    private boolean validMine(@NotNull Block block) {
        StringBuilder transactionStringToHash = new StringBuilder();
        for (int i = 0; i < block.getTransactions().size(); i++)
            transactionStringToHash.append(block.getTransactions().get(i).getTransactionHash());

        String stringToHash = block.getNonce() + block.getIndex() + block.getDate() + block.getPreviousHash() + transactionStringToHash;
        LOG.info(stringToHash);
        LOG.info("Block hash : {}", block.getHash());
        LOG.info("crypto : {} ", cryptography.toHexString(cryptography.getSha(stringToHash)));
        return cryptography.toHexString(cryptography.getSha(stringToHash)).equals(block.getHash());
    }

    private boolean validChain(List<Block> chain) {
        Block lastBlock = chain.get(0);
        Block block;
        for (int i = 1; i < chain.size(); i++) {
            block = chain.get(i);

            if (!(block.getPreviousHash().equals(computeHash(lastBlock).getHash())))
                return false;

            if (block.getNonce() != computeHash(block).getNonce())
                return false;

            lastBlock = block;
        }

        return true;
    }

    public List<Block> resolveConflict() {
        List<String> nodeList = this.nodes;
        List<Block> newChain = null;
        List<Block> chain;
        List<Block> blockList;
        int maxSize = this.chain.size();
        int size;

        for (String node : nodeList) {
            blockList = findChain(node);
            if (blockList != null) {
                size = blockList.size();
                chain = blockList;
                if (size > maxSize && validChain(chain)) {
                    maxSize = size;
                    newChain = chain;
                }
            }
        }
        if (newChain != null) {
            this.chain = newChain;
            LOG.info("Chain  has been replaced");
            return this.chain;
        }
        return null;
    }

    public List<Block> findChain(String node) {

        final HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        String address = "http://" + node + "/chain";

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(address))
                .setHeader("User-Agent", "Miner")
                .build();

        HttpResponse<String> response = null;
        try {

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404)
                return null;
            LOG.debug(response.body());
        } catch (IOException | InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }

        ArrayList<Block> blockList = null;
        try {
            if (response != null) {
                blockList = new ObjectMapper().readValue(response.body(), ArrayList.class);
            }
        } catch (NullPointerException | JsonProcessingException e) {
            LOG.error(e.getLocalizedMessage());
        }

        return blockList;
    }


    public Block computeHash(@NotNull Block block) {
        String hash;

        long nonce = -1;
        StringBuilder transactionStringToHash = new StringBuilder();

        for (int i = 0; i < block.getTransactions().size(); i++)
            transactionStringToHash.append(block.getTransactions().get(i).getTransactionHash());

        do {
            nonce++;
            String stringToHash = nonce + block.getIndex() + block.getDate() + block.getPreviousHash() + transactionStringToHash;
            Cryptography cryptography = new Cryptography();

            hash = cryptography.toHexString(cryptography.getSha(stringToHash));
            if (hash.startsWith(block.getDifficultyLevel())) {
                LOG.trace("string to hash {}", stringToHash);
                break;
            }

        } while (true);

        block.setNonce(nonce);
        block.setHash(hash);
        return block;
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
        if (chain.size() > CHANGE_REWARD_AMOUNT_PER)
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

    public List<String> getNodes() {
        return nodes;
    }

    public void addNode(String node) {
        this.nodes.add(node);
    }
}
