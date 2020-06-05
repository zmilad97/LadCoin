package com.github.zmilad97.core.Controller;


import com.github.zmilad97.core.Module.Block;
import com.github.zmilad97.core.Module.Transaction;
import com.github.zmilad97.core.Module.Wallet;
import com.github.zmilad97.core.Service.CoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        coreService.addBlock(block);
    }


    @RequestMapping(value = "/transaction/new", method = RequestMethod.POST)
    public void newTransaction(@RequestBody Transaction transaction) {
        coreService.getCurrentTransaction().add(transaction);    //TODO : FIX TRANSACTION-ID PROBLEM (MAKE IT AUTO GENERATE)
    }

    @RequestMapping(value = "/wallet/status", method = RequestMethod.POST)
    public Wallet walletStatus(@RequestBody String pubKey) {
        return null;                                    //TODO : FIX THIS
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


}
