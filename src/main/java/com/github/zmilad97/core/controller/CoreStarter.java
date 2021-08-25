package com.github.zmilad97.core.controller;

import com.github.zmilad97.core.exceptions.NodeNotFoundException;
import com.github.zmilad97.core.service.CoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CoreStarter implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(CoreStarter.class);
    private final CoreService coreService;

    @Autowired
    public CoreStarter(CoreService coreService) {
        this.coreService = coreService;
    }

    @Override
    public void run(ApplicationArguments args) {
        coreService.resolveConflict();
    }
}
