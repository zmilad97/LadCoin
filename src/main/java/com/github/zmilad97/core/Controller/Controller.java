package com.github.zmilad97.core.Controller;


import com.github.zmilad97.core.Module.Block;
import com.github.zmilad97.core.Module.Transaction;
import com.github.zmilad97.core.Module.Wallet;
import com.github.zmilad97.core.Service.CoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Controller {
    CoreService coreService;

    @Autowired
    public Controller(CoreService coreService) {
        this.coreService = coreService;
    }

    @GetMapping("/connectionTest")
    public void testConnection() {

    }

    @GetMapping("/config")
    public Map<String,String> getCoreConfig() {                    //TODO : BETTER TO CONFIG TYPE TO SOMETHING ELSE INSTEAD OF MAP
        Map<String,String> newMap = new HashMap<>();                 //TODO : NEED TO FIX REWARD PROBLEM
        newMap.put("reward", String.valueOf(this.coreService.getReward()));
        newMap.put("difficulty", this.coreService.getDifficultyLevel());
        return newMap;

    }

    @RequestMapping(value = "/pow", method = RequestMethod.POST)
    public void pow(@RequestBody Block block) {
        //TODO FIX DATE PROBLEM
        System.out.println(block.getDate());
        System.out.println(block.getNonce());
        coreService.addBlock(block, coreService);
    }


    @RequestMapping(value = "/transaction/new", method = RequestMethod.POST)
    public void newTransaction(@RequestBody Transaction transaction) {
        coreService.getCurrentTransaction().add(transaction);    //TODO : FIX TRANSACTION-ID PROBLEM (MAKE IT AUTO GENERATE)
    }

    @RequestMapping(value = "/wallet/status" , method = RequestMethod.POST)
    public Wallet walletStatus(@RequestBody String pubKey){
        return null;                                    //TODO : FIX THIS
    }

    @RequestMapping(value = "/block")
    public Block sendBlock() {

        Block block = new Block();
        block.setDate(new java.util.Date());
        block.setIndex(coreService.getChain().size() - 1);
        block.setPreviousHash(coreService.getChain().get(coreService.getChain().size() - 1).getPreviousHash());
        block.setTransactions(coreService.getCurrentTransaction());
        block.setDifficultyLevel(coreService.getDifficultyLevel());
        block.setReward(coreService.getReward());

        return block;
    }

    @GetMapping("/chain")
    public List<Block> chain() {
        return coreService.getChain();
    }

    @RequestMapping(value = "/wallet/add", method = RequestMethod.POST)
    public void addWallet(@RequestBody String wallet) {
        System.out.println(wallet);
        coreService.addWalletToWalletList(new Wallet(wallet, 0));

    }


}
