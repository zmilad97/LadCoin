package com.github.zmilad97.core;

import com.github.zmilad97.core.module.transaction.Transaction;
import com.github.zmilad97.core.service.CoreService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestfulBlockChainCoreApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CoreService coreService;

    @AfterEach
    void cleanUp(){
        coreService.getChain();
    }



}
