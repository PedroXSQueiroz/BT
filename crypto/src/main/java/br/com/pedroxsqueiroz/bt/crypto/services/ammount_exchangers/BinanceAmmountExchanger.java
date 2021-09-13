package br.com.pedroxsqueiroz.bt.crypto.services.ammount_exchangers;

import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.factories.RequestFactory;
import br.com.pedroxsqueiroz.bt.crypto.services.AmmountExchanger;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component("binanceAmmountExchanger")
public class BinanceAmmountExchanger extends AmmountExchanger {

	private static final Logger LOGGER = Logger.getLogger( BinanceAmmountExchanger.class.getName() );
	
    @Qualifier("binanceRequestFactory")
    @Autowired
    private RequestFactory requestFactory;

    @ConfigParam(name = "stockType", getFromParent = true)
    public StockType stockType;

    private StockType from;

    private StockType to;

    private void setRatioReferences()
    {
    	String stockTypeName = this.stockType.getName();

        try (CloseableHttpClient httpClient = HttpClients.createDefault() ){

            httpClient.execute(
                        this.requestFactory
                                .withRequestParams("symbol", stockTypeName)
                                .setup("GET", "exchangeInfo")
                                .build(),
                        response -> {

                            if(response.getStatusLine().getStatusCode() == 200)
                            {

                                InputStream content = response.getEntity().getContent();
                                ObjectMapper serializer = new ObjectMapper();
                                JsonNode json = serializer.readTree(content);

                                for( JsonNode currentSymbolNode : json.get("symbols") )
                                {

                                    String currentSymbolName = currentSymbolNode.get("symbol").asText();
                                    if( currentSymbolName.contentEquals(stockTypeName) )
                                    {

                                        this.from = StockType
                                                        .builder()
                                                        .name( currentSymbolNode.get("quoteAsset").asText() )
                                                        .build();

                                        this.to = StockType
                                                        .builder()
                                                        .name( currentSymbolNode.get("baseAsset").asText() )
                                                        .build();

                                    }

                                }

                            }

                            return null;
                        }
                    );

        } catch (CloneNotSupportedException | IOException e) {
        	LOGGER.log(Level.SEVERE, e.getMessage());
        } 

    }

    @Override
    protected StockType getFrom() {

        if(Objects.isNull(this.from))
        {
            this.setRatioReferences();
        }

        return this.from;
    }

    @Override
    protected StockType getTo() {

        if(Objects.isNull(this.to))
        {
            this.setRatioReferences();
        }

        return this.to;
    }

    @Override
    protected BigDecimal getRatio(StockType from, StockType to) {

        //TODO: SHOULD BE ALWAYS INVERTED?
        try ( CloseableHttpClient httpClient = HttpClients.createDefault() )
        {
            
            return httpClient
                .execute(
                    this.requestFactory
                            .withRequestParams("symbol", to.getName() + from.getName())
                            .setup("GET", "avgPrice")
                            .build(),
                    response -> {

                        if(response.getStatusLine().getStatusCode() == 200)
                        {

                            InputStream content = response.getEntity().getContent();

                            ObjectMapper serializer = new ObjectMapper();
                            JsonNode json = serializer.readTree(content);

                            double price = json.get("price").asDouble();

                            return BigDecimal.valueOf(1 / price );
                        }

                        return null;

                    }
                );

        } catch (CloneNotSupportedException | IOException  e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } 

        return null;
    }

}
