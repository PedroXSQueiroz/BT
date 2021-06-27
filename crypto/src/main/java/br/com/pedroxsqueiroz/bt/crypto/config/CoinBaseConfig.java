package br.com.pedroxsqueiroz.bt.crypto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "coin-base")
@Data
public class CoinBaseConfig {

    private String apiRoot;

}
