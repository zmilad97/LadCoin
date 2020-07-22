/*
package com.github.zmilad97.core;

import com.github.zmilad97.core.Module.Block;
import com.github.zmilad97.core.Service.CoreService;
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
        coreService.clean();
    }

    @Test
    void testAddTransaction() {
        Transaction transaction = new Transaction("test 1" , " test 2", 50);
        restTemplate.postForEntity("/transaction/new", transaction, Void.class);
        Assertions.assertEquals(1,coreService.getCurrentTransaction().size(),
                "Add transaction does not work"); ;
    }

    @Test
    void testEmptyGetBlock(){
        ResponseEntity<Block> res = restTemplate.getForEntity("/block", Block.class);
        Assertions.assertEquals(404,res.getStatusCodeValue());
    }

    @Test
    void testGetBlock(){
        Transaction transaction = new Transaction("test 1" , " test 2", 50);
        restTemplate.postForEntity("/transaction/new", transaction, Void.class);
        ResponseEntity<Block> res = restTemplate.getForEntity("/block", Block.class);
        Assertions.assertEquals(200,res.getStatusCodeValue());
        Block body = res.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(1,body.getTransactions().size());
        Assertions.assertEquals("test 1",body.getTransactions().get(0).getSource());
    }

}
*/
