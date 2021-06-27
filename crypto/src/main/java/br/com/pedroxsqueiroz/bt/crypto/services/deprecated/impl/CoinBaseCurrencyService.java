package br.com.pedroxsqueiroz.bt.crypto.services.deprecated.impl;

import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.factories.RequestFactory;
import br.com.pedroxsqueiroz.bt.crypto.services.deprecated.CurrencyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.logging.Logger;

@Service
public class CoinBaseCurrencyService implements CurrencyService {

    private static Logger LOGGER = Logger.getLogger( CoinBaseCurrencyService.class.getName() );

    @Autowired
    @Qualifier("coinBaseRequestFactory")
    private RequestFactory requestFactory;

    @Override
    public Double convert(StockType from, StockType to, Double value) {

        try {

            return HttpClients.createDefault().execute(
                    this.requestFactory
                        .setup("GET", String.format("exchangerate/%s/%s", from.getName(), to.getName() ))
                        .build(),
                    response -> {

                        InputStream responseContent = response.getEntity().getContent();

                        if(response.getStatusLine().getStatusCode() != 200)
                        {
                            LOGGER.info(IOUtils.toString ( responseContent, "UTF-8" ));
                            return null;
                        }

                        ObjectMapper deserializer = new ObjectMapper();

                        JsonNode jsonNodeReponseContent = deserializer.readTree(responseContent);

                        return jsonNodeReponseContent.get("rate").asDouble() * value;
                    });

        } catch (URISyntaxException | CloneNotSupportedException | IOException e) {
            e.printStackTrace();
        }

        return null;

    }
}
