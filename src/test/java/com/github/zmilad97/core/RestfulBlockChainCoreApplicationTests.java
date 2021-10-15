package com.github.zmilad97.core;

import com.github.zmilad97.core.service.Cryptography;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestfulBlockChainCoreApplicationTests {


    private final Cryptography cryptography = new Cryptography();

    @Test
    void testCrypto() {
        String hash = toHex(cryptography.getSha("test"));
        Long i = 0L;
        while (!hash.startsWith("000000")) {
            hash = toHex(cryptography.getSha(i + "test"));
            i++;
        }
        System.out.println(i);
        System.out.println(hash);
    }

    public String toHex(byte[] hash) {

        return DatatypeConverter.printHexBinary(hash);
    }

}
