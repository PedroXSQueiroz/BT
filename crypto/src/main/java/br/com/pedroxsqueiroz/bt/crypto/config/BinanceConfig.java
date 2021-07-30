package br.com.pedroxsqueiroz.bt.crypto.config;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Configuration
@ConfigurationProperties(prefix = "binance")
@Data
public class BinanceConfig {

    private String apiKey;

    private String secretKey;

    private String apiRoot;

    private String websocketRoot;

    private static final String MAC_ALGORITHM = "HmacSHA256";

}
