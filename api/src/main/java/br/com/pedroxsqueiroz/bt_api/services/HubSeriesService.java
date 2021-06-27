package br.com.pedroxsqueiroz.bt_api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@ConfigurationProperties(prefix = "series-hub")
@Service
public class HubSeriesService {

    @Scheduled( cron = "#{interval}")
    void broadCast(@Autowired SimpMessagingTemplate simpMessagingTemplate )
    {

    }

}
