


package com.github.zmilad97.core.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zmilad97.core.exceptions.NodeNotFoundException;
import com.github.zmilad97.core.module.Block;
import com.github.zmilad97.core.module.transaction.Transaction;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class CoreService {

    private static final Logger LOG = LoggerFactory.getLogger(CoreService.class);
    private static final int CHANGE_REWARD_AMOUNT_PER = 50;
    private final Map<String, Transaction> currentTransactions;
    private final Map<String, Transaction> chainIndex = new HashMap<>();
    private final Map<String, List<Transaction>> signatureIndex = new HashMap<>();
    private List<Block> chain = new ArrayList<>();
    private final Cryptography cryptography;
    private char conditionChar = 98;
    private final ReadWriteLock readWriteLock;
    @Value("${app.nodes.list}")
    private final List<String> nodes;

    public CoreService() {
        cryptography = new Cryptography();
        currentTransactions = new HashMap<>();
        readWriteLock = new ReentrantReadWriteLock();
        chain.add(generateGenesis());
        nodes = new ArrayList<>();
    }


    @NotNull
    private Block generateGenesis() {
        readWriteLock.writeLock().lock();
        try {
            Block genesis = new Block(0, new java.util.Date().toString(), new ArrayList<>());
            genesis.setPreviousHash(null);
            String stringToHash = "" + genesis.getIndex() + genesis.getDate() + genesis.getPreviousHash() + genesis.getTransactions();
            genesis.setReward(50.0);
            genesis.setDifficultyLevel("ab"); // TODO: Make it more challenging
            genesis.setHash(cryptography.toHexString(cryptography.getSha(stringToHash)));
            return genesis;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public String getTransactionId() {
        readWriteLock.readLock().lock();
        try {
            return "CHA" + (chain.size() - 1) + "TRX" + (currentTransactions.size() - 1) + "RAND" + (new Random(1024));
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void addBlock(Block block) {
        readWriteLock.writeLock().lock();
        try {
            if (validMine(block)) {
                LOG.debug(String.valueOf(block.getIndex()));
                doTransactions(block);
                chain.add(block);
                block.getTransactions().forEach(t -> this.chainIndex.put(t.getTransactionHash(), t));
                block.getTransactions().forEach(t -> {
                    if (signatureIndex.get(t.getTransactionOutput().getSignature()) == null) {
                        this.signatureIndex.put(t.getTransactionOutput().getSignature(), new ArrayList<>());
                    }
                    this.signatureIndex.get(t.getTransactionOutput().getSignature()).add(t);
                });
                block.getTransactions().forEach(t -> this.currentTransactions.remove(t.getTransactionHash()));
                LOG.info("Block has been added to chain");
            } else
                LOG.info("Block Is invalid");
        } finally {
            readWriteLock.writeLock().unlock();
        }
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
        if (transaction.getTransactionId().startsWith("REWARD"))
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
        return chainIndex.get(hash);
    }


    public List<Transaction> findUTXOs(String signature) {
        List<Transaction> unspentTransaction = this.signatureIndex.get(signature);
        unspentTransaction.stream()
                .filter(t -> !transactionIsUnspent(t, unspentTransaction))
                .forEach(unspentTransaction::remove);

        return unspentTransaction;
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

    public void resolveConflict() {
        readWriteLock.writeLock().lock();
        try {
            List<Block> newChain = null;
            List<Block> chain;
            List<Block> blockList;
            int maxSize = this.chain.size();
            int size;

            for (String node : this.nodes) {
                try {
                    blockList = findChain(node);

                    if (blockList != null) {
                        size = blockList.size();
                        chain = blockList;
                        if (size > maxSize && validChain(chain)) {
                            maxSize = size;
                            newChain = chain;
                        }
                    }
                } catch (NodeNotFoundException | IOException e) {
                    LOG.error(e.getLocalizedMessage());
                }
            }
            if (newChain != null) {
                this.chain = newChain;
                chainIndex.clear();
                this.chain.stream().flatMap(b -> b.getTransactions().stream()).forEach(t -> chainIndex.put(t.getTransactionHash(), t));
                LOG.info("Chain  has been replaced");
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public List<Block> findChain(String node) throws IOException, NodeNotFoundException {
        final OkHttpClient okHttpClient = new OkHttpClient();
        final ObjectMapper objectMapper = new ObjectMapper();
        String address = "http://" + node + "/chain";
        Request request = new Request.Builder().header("User-Agent", "Miner").url(address).build();
        Response response = okHttpClient.newCall(request).execute();

        if (response.code() == 404)
            throw new NodeNotFoundException();

        return Arrays.asList(objectMapper.readValue(Objects.requireNonNull(response.body()).toString(), Block[].class));
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


    private String getDifficultyLevel() {
        if (chain.size() % 5 == 0)
            return chain.get(chain.size() - 1).getDifficultyLevel() + (++conditionChar);
        return chain.get(chain.size() - 1).getDifficultyLevel();

    }

    public double getReward() {
        if (chain.size() % CHANGE_REWARD_AMOUNT_PER == 0)
            return chain.get(chain.size() - 1).getReward() / 2;
        return chain.get(chain.size() - 1).getReward();
    }

    public void addTransaction(Transaction transaction) {
        currentTransactions.put(transaction.getTransactionHash(), transaction);
    }

    public void addNode(String node) {
        this.nodes.add(node);
    }

    public Block getBlock() {
        readWriteLock.readLock().lock();
        try {
            if (this.currentTransactions.isEmpty())
                return null;
            Block block = new Block();
            block.setDate(new java.util.Date().toString());
            block.setIndex(this.chain.size());
            block.setPreviousHash(this.chain.get(this.chain.size() - 1).getPreviousHash());
            block.setTransactions(this.currentTransactions.values());
            block.setDifficultyLevel(getDifficultyLevel());
            block.setReward(getReward());
            return block;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }


    public List<Block> getChain() {
        return new ArrayList<>(chain);
    }

}
