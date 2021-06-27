package br.com.pedroxsqueiroz.bt.crypto.factories;

import br.com.pedroxsqueiroz.bt.crypto.config.BinanceConfig;
import br.com.pedroxsqueiroz.bt.crypto.config.CoinBaseConfig;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Configuration
public class RequestFactoryConfig {

    private static Logger LOGGER = Logger.getLogger( RequestFactoryConfig.class.getName() );

    @Bean(name = "binanceRequestFactory")
    public RequestFactory binanceRequestFactory (@Autowired final BinanceConfig config) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

        RequestFactory requestFactory = new RequestFactory();

        requestFactory.setRoot(config.getApiRoot());
        requestFactory.withDefaultRequestHeaders("X-MBX-APIKEY", config.getApiKey());

        requestFactory.setAssigner(( requestBuilder ) -> {

            try {

                Mac shaMac = Mac.getInstance("HmacSHA256");
                SecretKeySpec keySpec = new SecretKeySpec(config.getSecretKey().getBytes(), "HmacSHA256");
                shaMac.init(keySpec);

                List<String> params = requestBuilder
                        .getParameters()
                        .stream()
                        .map((nameToValue) -> String.format("%s=%s", nameToValue.getName(), nameToValue.getValue()))
                        .collect(Collectors.toList());

                String message = Strings.join(params, '&');
                byte[] messageBytes = message.getBytes();

                final byte[] macData = shaMac.doFinal(messageBytes);
                String sign = Hex.encodeHexString(macData);

                requestBuilder.addParameter("signature", sign);
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
            }

        });

        return requestFactory;
    }

    @Bean("coinBaseRequestFactory")
    public RequestFactory coinBaseRequestFactory(@Autowired final CoinBaseConfig config)
    {
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.setRoot( config.getApiRoot());
        return requestFactory;
    }

}
