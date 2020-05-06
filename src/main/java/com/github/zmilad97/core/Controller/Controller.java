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
    public void testConnection(){

    }

    @GetMapping("/config")
    public Map getCoreConfig() {
        Map newMap = new HashMap();

        newMap.put(this.coreService.getReward(), this.coreService.getDifficultyLevel());
        return newMap;

    }

    @RequestMapping(value = "/pow" , method = RequestMethod.POST)
    public void pow(@RequestBody Block block){
        coreService.addBlock(block,coreService);
    }


    @RequestMapping(value = "/transaction" , method = RequestMethod.POST)
    public void newTransaction(@RequestBody Transaction transaction){
        coreService.getCurrentTransaction().add(transaction);

    }

    @RequestMapping(value = "/block")
    public Block testTransaction(){
        Block block = new Block();
        block.setIndex(coreService.getChain().size()-1);
        block.setPreviousHash(coreService.getChain().get(coreService.getChain().size()-1).getPreviousHash());
        block.setTransactions(coreService.getCurrentTransaction());

        return block;
    }

    @GetMapping("/chain")
    public List<Block> chain(){
       return coreService.getChain();
    }

    @RequestMapping(value = "/wallet/add",method = RequestMethod.POST)
    public void addWallet(@RequestBody String wallet){
        System.out.println(wallet);
        coreService.addWalletToWalletList(new Wallet(wallet,0));
    }


}
