package com.github.zmilad97.core.Controller;


import com.github.zmilad97.core.Module.Block;
import com.github.zmilad97.core.Module.Transaction.Transaction;
import com.github.zmilad97.core.Module.Transaction.TransactionInput;
import com.github.zmilad97.core.Module.Transaction.TransactionOutput;
import com.github.zmilad97.core.Module.Wallet;
import com.github.zmilad97.core.Service.CoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@RestController
public class CoreController {
    private static final Logger LOG = LoggerFactory.getLogger(CoreController.class);

    private final CoreService coreService;

    @Autowired
    public CoreController(CoreService coreService) {
        this.coreService = coreService;

    }

    @GetMapping("/connectionTest")
    public void testConnection() {

    }

    @RequestMapping(value = "/pow", method = RequestMethod.POST)
    public void pow(@RequestBody Block block) {

        LOG.info("pow requested: {}", block);
//        coreService.addBlock(block);  TODO : FIX THIS
    }


    @RequestMapping(value = "/transaction/new", method = RequestMethod.POST)
    public void newTransaction(@RequestBody Transaction transaction) {
        coreService.getCurrentTransaction().add(transaction);    //TODO : FIX TRANSACTION-ID PROBLEM (MAKE IT AUTO GENERATE)
    }

    @RequestMapping(value = "/wallet/status", method = RequestMethod.POST)
    public Wallet walletStatus(@RequestBody String pubKey) {
        return null;                                    //TODO : FIX THIS
    }

    @RequestMapping(value = "/UTXOs", method = RequestMethod.POST)
    public ResponseEntity<Transaction> UTXOs(@RequestBody String publicKey) {
        LOG.info(publicKey);
//        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
        Transaction transaction = coreService.findUTXOs(publicKey);
        LOG.debug(transaction.getTransactionId());
        LOG.debug(transaction.getTransactionOutput().getPublicKeyScript());
        return ResponseEntity.ok(transaction) ;
    }

    @RequestMapping(value = "/block")
    public ResponseEntity<Block> sendBlock() {

        if (coreService.isCurrentTransactionEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Block block = new Block();
        block.setDate(new java.util.Date().toString());
        block.setIndex(coreService.getChain().size());
        block.setPreviousHash(coreService.getChain().get(coreService.getChain().size() - 1).getPreviousHash());
        block.setTransactions(coreService.getCurrentTransaction());
        block.setDifficultyLevel(coreService.getDifficultyLevel());
        block.setReward(coreService.getReward());
        return ResponseEntity.ok(block);


    }

    @RequestMapping(value = "/chain")
    public List<Block> chain() {
        return coreService.getChain();
    }

    @RequestMapping(value = "/wallet/add", method = RequestMethod.POST)
    public void addWallet(@RequestBody String wallet) {
        System.out.println(wallet);
        coreService.addWalletToWalletList(new Wallet(wallet, 0));

    }


    @RequestMapping(value = "/test/block", method = RequestMethod.GET)
    public void addTestBlock() {
        String pubKeyScript = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEF25QbMKZV5wJ/tw9BjBvx137bIQwbJR76bYkwAQeKbn9xRPPaMNpu0hWRlZt8MUxvGvn/ln5PxPHB+cmbmacZw==";
        Transaction transaction = new Transaction();
        TransactionInput transactionInput = new TransactionInput();
        TransactionOutput transactionOutput = new TransactionOutput();
        transactionInput.setIndexReferenced("12");
        transactionInput.setPreviousTransactionHash("null");
        transactionInput.setScriptSignature("null");
        transactionOutput.setAmount(50);
        transactionOutput.setPublicKeyScript(pubKeyScript);
        transaction.setTransactionId("80");
        transaction.setTransactionInput(transactionInput);
        transaction.setTransactionOutput(transactionOutput);
        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(transaction);
        LOG.debug(String.valueOf(transaction));
        Block block = new Block(10, String.valueOf(new java.util.Date()), transactionList);
        coreService.getChain().add(block);


    }



    @RequestMapping(value = "/test/transaction", method = RequestMethod.GET)
    public void addTestTransaction(){
        Transaction transaction = new Transaction();
        TransactionInput transactionInput = new TransactionInput();
        TransactionOutput transactionOutput = new TransactionOutput();

        transactionInput.setPreviousTransactionHash(null);
        transactionInput.setIndexReferenced("20");
        transactionInput.setScriptSignature(null);

        transactionOutput.setPublicKeyScript("s");
        transactionOutput.setAmount(200);

        transaction.setTransactionId("test");
        transaction.setTransactionOutput(transactionOutput);
        transaction.setTransactionInput(transactionInput);

        coreService.addTransaction(transaction);





    }


}
