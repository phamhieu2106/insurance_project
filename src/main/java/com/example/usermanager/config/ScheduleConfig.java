package com.example.usermanager.config;

import com.example.usermanager.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

@Configuration
@EnableScheduling
public class ScheduleConfig {

    private final ContractService contractService;

    @Autowired
    public ScheduleConfig(ContractService contractService) {
        this.contractService = contractService;
    }

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Ho_Chi_Minh")
    private void contractStatusSchedule() {
        Date now = new Date();
        contractService.updateContractStatusNotEffect(now);
        contractService.updateContractStatusEffected(now);
    }
}
