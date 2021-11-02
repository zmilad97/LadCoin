package com.github.zmilad97.core.service;

import com.github.zmilad97.core.module.Block;
import com.github.zmilad97.core.module.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class TransactionService {

    //removing unValid Transaction
    public void doTransactions(Block block) {
        log.debug("block transaction size " + block.getTransactions().size());
        int i = 0;
        for (Transaction transaction : block.getTransactions()) {
            if (!(transaction.getTransactionId().equals("REWARD" + block.getIndex()))) {
                log.debug("i : {}", i++);
                log.debug(block.getTransactions().get(0).getTransactionHash());
                try {
                    if (!(validTransaction(transaction)))
                        block.getTransactions().remove(transaction);
                } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | IllegalArgumentException | SignatureException e) {
                    log.debug("invalid transaction : {}", i);
                }
            }
        }
    }

    public boolean validTransaction(Transaction transaction) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (transaction.getTransactionId().startsWith("REWARD"))
            return true;
        log.debug(transaction.getTransactionInput().getPubKey());
        if (transaction.getTransactionInput().getPubKey().equals("null"))
            return true;

        log.debug(transaction.getTransactionInput().getPubKey());
        boolean result = false;

        EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode
                (transaction.getTransactionInput().getPubKey()));

        KeyFactory keyFactory = KeyFactory.getInstance("EC");

        PublicKey publicKey = keyFactory.generatePublic(encodedKeySpec);
        Signature signature = Signature.getInstance("SHA256withECDSA");

        for (int i = 0; i < transaction.getTransactionInput().getPreviousTransactionHash().size(); i++) {
            Transaction trx = findTransactionByTransactionHash(transaction.getTransactionInput().getPreviousTransactionHash().get(i));
            if (trx.getTransactionOutput().getSignature().equals(transaction.getTransactionOutput().getSignature())) {

                signature.initVerify(publicKey);
                signature.update(Base64.getEncoder().encode(publicKey.getEncoded()));
                result = signature.verify(Base64.getDecoder().decode(trx.getTransactionOutput().getSignature()));

            }
        }
        return result;
    }

    public Transaction findTransactionByTransactionHash(String hash) {
        return CoreService.chainIndex.get(hash);
    }


    public List<Transaction> findUTXOs(String signature) {
        List<Transaction> unspentTransaction = CoreService.signatureIndex.get(signature);
        unspentTransaction.stream()
                .filter(t -> !transactionIsUnspent(t, unspentTransaction))
                .forEach(unspentTransaction::remove);

        return unspentTransaction;
    }

    //Check the UTXOs unspent or not
    private boolean transactionIsUnspent(Transaction transaction, List<Transaction> UTXOsList) {
        log.debug(" unspent Transactions ");
        for (int i = UTXOsList.size() - 1; i >= 0; i--)
            for (int j = transaction.getTransactionInput().getPreviousTransactionHash().size() - 1; j >= 0; j--)
                if (UTXOsList.get(i).getTransactionHash()
                        .equals(transaction.getTransactionInput().getPreviousTransactionHash().get(j)))
                    return false;

        return true;
    }

}
