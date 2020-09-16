package com.github.zmilad97.core.controller;

import com.github.zmilad97.core.service.CoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
public class CoreStarter implements ApplicationRunner {

    private CoreService coreService;

    @Autowired
    public CoreStarter(CoreService coreService) {
        this.coreService = coreService;
    }

    @Override
    public void run(ApplicationArguments args) {
        coreService.resolveConflict();
        }
    }
