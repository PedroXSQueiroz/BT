package br.com.pedroxsqueiroz.bt.crypto.services.deprecated.impl;

import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.binance.BinanceAccount;
import br.com.pedroxsqueiroz.bt.crypto.dtos.binance.BinanceBalance;
import br.com.pedroxsqueiroz.bt.crypto.config.BinanceConfig;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.StockNotFoundException;
import br.com.pedroxsqueiroz.bt.crypto.factories.RequestFactory;
import br.com.pedroxsqueiroz.bt.crypto.services.deprecated.CurrencyService;
import br.com.pedroxsqueiroz.bt.crypto.services.deprecated.WalletService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class BinanceWalletService implements WalletService {

    private static Logger LOGGER = Logger.getLogger( BinanceWalletService.class.getName() );

    @Autowired
    private BinanceConfig config;

    @Qualifier("binanceRequestFactory")
    @Autowired
    private RequestFactory requestFactory;

    @Autowired
    private CurrencyService currencyService;

    @Override
    public void setup() {
        LOGGER.info("Creating Binance Wallet");

        StockType BTCStockType = new StockType();
        BTCStockType.setName("BTC");

        LOGGER.info( "Wallet actually contains BTC USD - %s"  );

        Double BTC = this.getTotal(BTCStockType);
        //Double BTC = 0.000711;
        LOGGER.info( String.format("BTC: %s", BTC) );

        StockType USDStockType = new StockType();
        USDStockType.setName("USD");

        LOGGER.info( String.format("USD: %s", this.currencyService.convert( BTCStockType, USDStockType, BTC ) ) );
    }

    @Override
    public Double getTotal(StockType type) {

        try{
            return this.getBalances(type).getFree();
        }catch (
                    IOException |
                    StockNotFoundException |
                    URISyntaxException |
                    CloneNotSupportedException exception )
        {
            exception.printStackTrace();
        }

        return null;
    }

    private BinanceBalance getBalances(StockType type) throws IOException, StockNotFoundException, URISyntaxException, CloneNotSupportedException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        BinanceAccount account = HttpClients.createDefault().execute(
                requestFactory
                        .withRequestParams("timestamp", Long.toString( new Date().getTime() ) )
                        .withRequestHeader( "recvWindow", Integer.toString(19000000) )
                        .setup("GET", "account")
                        .assign()
                        .build(),
                response -> {

                    InputStream content = response.getEntity().getContent();

                    if(response.getStatusLine().getStatusCode() != 200)
                    {
                        LOGGER.info(IOUtils.toString ( content, "UTF-8" ));
                        return null;
                    }

                    return  mapper.readValue(content, BinanceAccount.class) ;
                });

        Optional<BinanceBalance> balanceOpt = account
            .getBalances()
            .stream()
            .filter( balance -> balance.isStock(type) )
            .findAny();

        if(balanceOpt.isPresent())
        {
            return balanceOpt.get();
        }

        throw new StockNotFoundException();

    }

}
