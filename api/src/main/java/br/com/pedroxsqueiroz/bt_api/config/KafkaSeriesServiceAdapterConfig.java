package br.com.pedroxsqueiroz.bt_api.config;

import br.com.pedroxsqueiroz.bt_api.services.SeriesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaSeriesServiceAdapterConfig {

    @Bean("kafkaSeriesService")
    SeriesService setup()
    {

        return null;

    }

}
