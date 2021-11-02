package com.github.zmilad97.core.controller;


import com.github.zmilad97.core.exceptions.InvalidBlockException;
import com.github.zmilad97.core.module.Block;
import com.github.zmilad97.core.module.transaction.Transaction;
import com.github.zmilad97.core.module.transaction.TransactionInput;
import com.github.zmilad97.core.module.transaction.TransactionOutput;
import com.github.zmilad97.core.service.CoreService;
import com.github.zmilad97.core.service.TransactionService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.*;

@RestController
@Slf4j
public class CoreController {
    private final CoreService coreService;
    private final TransactionService transactionService;

    @Autowired
    public CoreController(CoreService coreService, TransactionService transactionService) {
        this.coreService = coreService;
        this.transactionService = transactionService;
    }

    @GetMapping("/confirmation")
    public ResponseEntity<Boolean> confirmation(@RequestBody Block block) {
        return coreService.getConfirmation(block);
    }

    @PostMapping("/validMine")
    public ResponseEntity<String> validMine(HttpServletRequest request, @RequestBody Block block) {
        log.info("validating : {}", block);
        try {
            coreService.addBlock(block);
            if (request.getHeader("address") != null)
                coreService.addNode(request.getHeader("address"));
            return new ResponseEntity<>("successful", HttpStatus.ACCEPTED);
        } catch (InvalidBlockException e) {
            return new ResponseEntity<>("invalid", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping("/transaction/new")
    public void newTransaction(@RequestBody Transaction transaction) {
        transaction.setTransactionId(coreService.getTransactionId());
        coreService.addTransaction(transaction);
    }


    @PostMapping("/UTXOs")
    public List<Transaction> UTXOs(@RequestBody String signature) {
        log.info(signature);
        return transactionService.findUTXOs(signature);
    }

    @GetMapping("/block")
    public ResponseEntity<Block> getBlock() {
        log.debug("sending Block");
        Block block = coreService.getBlock();
        if (block == null)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return ResponseEntity.ok(block);
    }

    @GetMapping("/chain")
    public List<Block> chain(HttpServletRequest request) {

        if (request.getHeader("NodeAddress") != null) {
            coreService.addNode(request.getHeader("NodeAddress"));
        }

        return coreService.getChain();
    }

    @GetMapping("/nodes")
    public Set<String> getNodes() {
        return coreService.getSoftNodes();
    }

    @PostMapping("/node/register")
    public void registerNode(String url) {
        coreService.addNode(url);
    }


    @SneakyThrows
    @GetMapping("/test/block")
    public void addTestBlock() {
        String signature = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEF25QbMKZV5wJ/tw9BjBvx137bIQwbJR76bYkwAQeKbn9xRPPaMNpu0hWRlZt8MUxvGvn/ln5PxPHB+cmbmacZw==";
        Transaction transaction = new Transaction();
        transaction.setTransactionHash("test");
        TransactionInput transactionInput = new TransactionInput();
        TransactionOutput transactionOutput = new TransactionOutput();
        transactionInput.setIndexReferenced(12);
        transactionInput.addPreviousTransactionHash(0, "test");
        transactionInput.setPubKey("null");
        transactionOutput.setAmount(50);
        transactionOutput.setSignature(signature);
        transaction.setTransactionId("80");
        transaction.setTransactionInput(transactionInput);
        transaction.setTransactionOutput(transactionOutput);
        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(transaction);
        log.debug(String.valueOf(transaction));
        Block block = new Block(10, String.valueOf(new java.util.Date()), transactionList);
        coreService.addBlock(block);
    }

    @GetMapping("/test/transaction")
    public void addTestTransaction() {
        Transaction transaction = new Transaction();
        TransactionInput transactionInput = new TransactionInput();
        TransactionOutput transactionOutput = new TransactionOutput();

        transactionInput.addPreviousTransactionHash(0, "hash 0 test");
        transactionInput.setIndexReferenced(20);
        transactionInput.setPubKey("pubkey test");

        transactionOutput.setSignature("signature test");
        transactionOutput.setAmount(200);

        transaction.setTransactionId(String.valueOf(new Random()));
        transaction.setTransactionOutput(transactionOutput);
        transaction.setTransactionInput(transactionInput);
        coreService.addTransaction(transaction);
    }
}
