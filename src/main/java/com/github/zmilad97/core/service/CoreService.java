


package com.github.zmilad97.core.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zmilad97.core.exceptions.InvalidBlockException;
import com.github.zmilad97.core.module.Block;
import com.github.zmilad97.core.module.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Slf4j
public class CoreService {

    private static double BLOCK_REWARD = 50;
    public static final Map<String, Transaction> chainIndex = new HashMap<>();
    public static final Map<String, List<Transaction>> signatureIndex = new HashMap<>();
    public static final Map<Block, Long> blockConfirmation = new HashMap<>();
    private final Map<String, Transaction> currentTransactions;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private List<Block> chain = new ArrayList<>();
    private final TransactionService transactionService;
    private final RequestHandler requestHandler;
    private final Cryptography cryptography;
    private final ReadWriteLock readWriteLock;
    @Value("${app.currentNodeAddress}")
    private final String currentNodeAddress;
    @Value("${app.nodes.list}")
    private final Set<String> hardNodes;
    private final Set<String> softNodes;


    public CoreService() {
        transactionService = new TransactionService();
        requestHandler = new RequestHandler();
        cryptography = new Cryptography();
        currentTransactions = new HashMap<>();
        readWriteLock = new ReentrantReadWriteLock();
        chain.add(generateGenesis());
        hardNodes = new HashSet<>();
        softNodes = new HashSet<>();
        currentNodeAddress = "";
        okHttpClient = new OkHttpClient();
        objectMapper = new ObjectMapper();
    }


    @NotNull
    private Block generateGenesis() {
        readWriteLock.writeLock().lock();
        try {
            Block genesis = new Block(0, new java.util.Date().toString(), new ArrayList<>());
            genesis.setPreviousHash(null);
            String stringToHash = "" + genesis.getIndex() + genesis.getDate() + genesis.getPreviousHash() + genesis.getTransactions();
            genesis.setReward(50.0);
            genesis.setDifficultyLevel("000");
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


    public void addBlock(Block block) throws InvalidBlockException {
        readWriteLock.writeLock().lock();
        try {
            if (validMine(block)) {
                transactionService.doTransactions(block);
                chain.add(block);
                block.getTransactions().forEach(t -> chainIndex.put(t.getTransactionHash(), t));
                block.getTransactions().forEach(t -> {
                    signatureIndex.computeIfAbsent(t.getTransactionOutput().getSignature(), k -> new ArrayList<>());
                    signatureIndex.get(t.getTransactionOutput().getSignature()).add(t);
                });
                block.getTransactions().forEach(t -> this.currentTransactions.remove(t.getTransactionHash()));
                blockConfirmation.put(block, 1L);
                log.info("Block {} with hash {} has been added to chain ", block.getIndex(), block.getHash());
                doConfirmations(block);
            } else
                throw new InvalidBlockException();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * @param block sends given block to nodes to get confirmations
     */

    private void doConfirmations(Block block) {
        List<String> nodes = new ArrayList<>();
        nodes.addAll(hardNodes);
        nodes.addAll(softNodes);

        nodes.forEach(node ->
                requestHandler.getBlockConfirmationAsync(node, block).thenAccept(response -> {
                    if (response.statusCode() == 202) {
                        Long currentConfirmation = blockConfirmation.get(block);
                        blockConfirmation.put(block, currentConfirmation + 1);
                    }

                }));
    }


    private boolean validMine(Block block) {
        if (!block.getHash().startsWith(getDifficultyLevel())) {
            return false;
        }
        StringBuilder transactionStringToHash = new StringBuilder();
        for (int i = 0; i < block.getTransactions().size(); i++)
            transactionStringToHash.append(block.getTransactions().get(i).getTransactionHash());

        String stringToHash = block.getNonce() + block.getIndex() + block.getDate() + block.getPreviousHash() + transactionStringToHash;
        String blockHash = block.getHash();
        String actualHash = cryptography.toHexString(cryptography.getSha(stringToHash));
        log.info(stringToHash);
        log.info("Block hash : {}", blockHash);
        log.info("Actual hash : {} ", actualHash);
        return blockHash.equals(actualHash);
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

            Set<String> softNodes;
            int maxSize = this.chain.size();
            int size;

            for (String node : this.hardNodes) {
                try {
                    blockList = findChain(node);
                    softNodes = findSoftNodes(node);
                    if (blockList != null) {
                        size = blockList.size();
                        chain = blockList;
                        if (size > maxSize && validChain(chain)) {
                            maxSize = size;
                            newChain = chain;
                        }
                    }
                    if (softNodes != null) {
                        this.softNodes.addAll(softNodes);
                    }

                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            if (newChain != null) {
                this.chain = newChain;
                chainIndex.clear();
                this.chain.stream().flatMap(b -> b.getTransactions().stream()).forEach(t -> chainIndex.put(t.getTransactionHash(), t));
                log.info("Chain has been replaced");
            }


        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public List<Block> findChain(String node) throws IOException {
        String address = node + "/chain";
        Request request = new Request.Builder().header("NodeAddress", currentNodeAddress).url(address).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String res = Objects.requireNonNull(response.body()).string();
            Block[] blocks = objectMapper.readValue(res, Block[].class);
            return Arrays.asList(blocks);

        }
    }

    public HashSet<String> findSoftNodes(String node) throws IOException {
        String address = node + "/nodes";
        Request request = new Request.Builder().header("NodeAddress", currentNodeAddress).url(address).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String res = Objects.requireNonNull(response.body()).string();
            String[] softNodes = objectMapper.readValue(res, String[].class);
            return new HashSet<>(Arrays.asList(softNodes));

        }
    }

    public Block computeHash(Block block) {
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
                log.trace("string to hash {}", stringToHash);
                break;
            }

        } while (true);

        block.setNonce(nonce);
        block.setHash(hash);
        return block;
    }


    private String getDifficultyLevel() {
        if (chain.size() % 5 == 0)
            return chain.get(chain.size() - 1).getDifficultyLevel() + ("0");
        return chain.get(chain.size() - 1).getDifficultyLevel();

    }

    public void setReward() {
        if (chain.size() % 5 == 0) {
            for (int i = 0; i < chain.size() / 5; i++) {
                BLOCK_REWARD = BLOCK_REWARD / 2;
            }
        } else
            BLOCK_REWARD = chain.get(chain.size() - 1).getReward();
    }

    public void addTransaction(Transaction transaction) {
        transaction.setTransactionHash(computeTransactionHash(transaction));
        currentTransactions.put(transaction.getTransactionHash(), transaction);
    }

    public void addNode(String node) {
        this.softNodes.add(node);
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
            block.setReward(BLOCK_REWARD);
            return block;
        } finally {
            setReward();
            readWriteLock.readLock().unlock();
        }
    }

    public String computeTransactionHash(Transaction transaction) {
        String toHashString = transaction.getTransactionId() +
                transaction.getTransactionInput().getPubKey() +
                transaction.getTransactionOutput().getSignature();
        return cryptography.toHexString(cryptography.getSha(toHashString));
    }


    public List<Block> getChain() {
        return new ArrayList<>(chain);
    }

    public Set<String> getSoftNodes() {
        return softNodes;
    }


    public ResponseEntity<Boolean> getConfirmation(Block block) {
        if (validMine(block))
            return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
        else
            return new ResponseEntity<>(false, HttpStatus.NOT_ACCEPTABLE);
    }
}
