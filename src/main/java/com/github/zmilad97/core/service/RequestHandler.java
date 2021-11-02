package com.github.zmilad97.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zmilad97.core.module.Block;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class RequestHandler {
    private final ObjectMapper mapper;
    private final HttpClient client;


    public RequestHandler() {
        mapper = new ObjectMapper();
        client = HttpClient.newHttpClient();
    }

    public List<Block> getBlocksAsync(List<String> addresses) throws InterruptedException, TimeoutException {
        List<Block> blocks = new ArrayList<>();
        for (String address : addresses) {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(address + "/block")).build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).
                    thenAccept(response -> {
                        if (response.statusCode() == 200)
                            try {
                                blocks.add(mapper.readValue(response.body(), Block.class));

                            } catch (JsonProcessingException e) {
                                log.error(e.getMessage());
                            }
                    });
        }
        for (int i = 0; i < 10; i++) {
            if (blocks.size() == 0)
                Thread.sleep(1000);
            else
                return blocks;
        }

        if (blocks.size() == 0)
            throw new TimeoutException();
        else
            return blocks;
    }

    @SneakyThrows
    public HttpResponse<String> sendBlock(String address, Block block) {
        String body = mapper.writeValueAsString(block);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(address + "/validMine"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers
                        .ofString(body)).build();
        log.debug("send block to {}", address);
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @SneakyThrows
    public CompletableFuture<HttpResponse<String>> getBlockConfirmationAsync(String address, Block block) {
        String body = mapper.writeValueAsString(block);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(address + "/confirmation"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers
                        .ofString(body)).build();
        log.debug("send block to {}", address);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }


}
