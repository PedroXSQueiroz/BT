package br.com.pedroxsqueiroz.bt.crypto.services.markets;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.dtos.Wallet;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
import br.com.pedroxsqueiroz.bt.crypto.factories.RequestFactory;
import br.com.pedroxsqueiroz.bt.crypto.services.MarketFacade;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.Delegate;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.sql.Date;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

@Configuration
@Component("binance")
public class BinanceMarketFacade extends MarketFacade {

    BlockingQueue<SerialEntry> serialEntries;

    private static final int MAX_BUFFER_CAPACITY = 6;

    private static final int SERIES_ENTRY_TIME = 0;
    private static final int SERIES_ENTRY_OPENING = 1;
    private static final int SERIES_ENTRY_MAX = 2;
    private static final int SERIES_ENTRY_MIN = 3;
    private static final int SERIES_ENTRY_CLOSING = 4;
    private static final int SERIES_ENTRY_VOLUME = 5;

    private static Logger LOGGER = Logger.getLogger( BinanceMarketFacade.class.getName() );

    @Autowired
    @Qualifier("binanceRequestFactory")
    private RequestFactory requestFactory;

    @ConfigParam(name = "stockType", getFromParent = true)
    public StockType currentStockType;

    @ConfigParam(name = "fecthStockInterval" )
    public Integer interval;

    //FIXME:SHOULD DBE CONFIGURABLE
    @ConfigParam(name = "fecthStockIntervalUnit")
    public String intervalUnit;

    ScheduledFuture scheduled;

    @Override
    public List<StockType> getSupportedStocks() {
        return null;
    }

    @Override
    public List<SerialEntry> fetch(StockType type, Instant from, Instant to) {

        List<SerialEntry> serialEntries = new ArrayList<SerialEntry>();

        while( !this.serialEntries.isEmpty() )
        {
            try {
                SerialEntry serialEntry = this.serialEntries.take();

                Instant currentSerialEntryInstant = serialEntry.getDate().toInstant();

                boolean isOnInterval =  from.isAfter(currentSerialEntryInstant)
                                        && to.isBefore(currentSerialEntryInstant);

                if(isOnInterval)
                {
                    serialEntries.add(serialEntry);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return serialEntries;
    }

    @Override
    public List<SerialEntry> fetchNext(StockType type) {

        try {

            SerialEntry currentSerialEntry = this.serialEntries.take();

            return new ArrayList<SerialEntry>() {{
                this.add(currentSerialEntry);
            }};

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public TradePosition entryPosition(Double ammount, StockType type) {

        //EXECUTE TRADE ON BINANCE

        return  TradePosition
                        .builder()
                        .entryTime(Instant.now())
                        .entryAmmount(ammount)
                        .entryValue( this.exchangeValueRate(type) * ammount )
                        .build();

    }

    @Override
    public TradePosition exitPosition(TradePosition trade, Double ammount, StockType type) {

        //EXECUTE TRADE ON BINANCE

        trade.setExitTime(Instant.now());
        trade.setExitAmmount( ammount );
        trade.setExitValue( this.exchangeValueRate(type) * ammount );

        return trade;
    }

    @Override
    public Double exchangeValueRate(StockType type) {

        try {

            return HttpClients.createDefault().execute(
                    this.requestFactory
                            .withRequestParams("symbol",type.getName())
                            .setup("GET", "avgPrice")
                            .build(),
                    (response) -> {

                        if(response.getStatusLine().getStatusCode() != 200)
                        {
                            LOGGER.info("Error on get exchange data");
                            return null;
                        }

                        ObjectMapper serializer = new ObjectMapper();

                        InputStream content = response.getEntity().getContent();
                        JsonNode jsonNode = serializer.readTree(content);
                        return jsonNode.get("price").asDouble();

                    });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Wallet getWallet() {

        try {
            HttpUriRequest request = this.requestFactory
                    .setup("GET", "account")
                    .assign()
                    .build();

            return HttpClients.createDefault().execute(request, (response) -> {

                InputStream responseContent = response.getEntity().getContent();

                ObjectMapper serializer = new ObjectMapper();
                JsonNode responseJson = serializer.readTree(responseContent);

                Wallet wallet = new Wallet();

                JsonNode balance = responseJson.get("balance");
                balance.forEach( stock -> {

                    String stockName = stock.get("asset").asText();
                    double ammount = stock.get("free").asDouble();

                    StockType stockType = new StockType(stockName);

                    wallet.addAmmountToStock( stockType, ammount );

                });

                return wallet;

            });

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Delegate(types = Configurable.class)
    private AnnotadedFieldsConfigurer<BinanceMarketFacade> configurer = new AnnotadedFieldsConfigurer<BinanceMarketFacade>(this);

    @Override
    public void finish() {

    }

    @Override
    public void start() throws ImpossibleToStartException {

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        this.scheduled = scheduledExecutorService.scheduleAtFixedRate(() -> {

            try {

                HttpClients.createDefault().execute(
                        this.requestFactory
                                .withRequestParams("interval", String.format("%s%s", this.interval.toString(), this.intervalUnit ) )
                                .withRequestParams("symbol",this.currentStockType.getName())
                                .withRequestParams("limit","1")
                                .setup("GET", "klines")
                                .build(),
                        (response) -> {

                            InputStream responseContent = response.getEntity().getContent();

                            if(response.getStatusLine().getStatusCode() != 200)
                            {
                                LOGGER.severe(String.format( "error on request candle to Binance\n%s", IOUtils.toString(responseContent, Charset.forName("UTF-8")) ) );
                                return null;
                            }

                            ObjectMapper serializer = new ObjectMapper();
                            JsonNode jsonNode = serializer.readTree(responseContent);

                            Map<Instant, SerialEntry> seriesSlice = new HashMap<Instant, SerialEntry>();

                            for( JsonNode currentEntryData : jsonNode )
                            {
                                LOGGER.info(currentEntryData.toPrettyString());

                                try {
                                    SerialEntry currentEntry = new SerialEntry();

                                    long dateTimestamp  = currentEntryData.get(SERIES_ENTRY_TIME).asLong();
                                    Instant dateInstant = Instant.ofEpochMilli(dateTimestamp);
                                    java.util.Date date = Date.from(dateInstant);
                                    double opening      = currentEntryData.get(SERIES_ENTRY_OPENING).asDouble();
                                    double max          = currentEntryData.get(SERIES_ENTRY_MAX).asDouble();
                                    double min          = currentEntryData.get(SERIES_ENTRY_MIN).asDouble();
                                    double closing      = currentEntryData.get(SERIES_ENTRY_CLOSING).asDouble();
                                    double volume       = currentEntryData.get(SERIES_ENTRY_VOLUME).asDouble();

                                    currentEntry.setDate(date);
                                    currentEntry.setOpening(opening);
                                    currentEntry.setMax(max);
                                    currentEntry.setMin(min);
                                    currentEntry.setClosing(closing);
                                    currentEntry.setVolume(volume);

                                    LOGGER.info( serializer.writeValueAsString(currentEntry) );

                                    this.serialEntries.put( currentEntry );
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            return null;
                        }
                );

            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }



        }, 1, 1, TimeUnit.MINUTES);


        this.serialEntries = new ArrayBlockingQueue<SerialEntry>(MAX_BUFFER_CAPACITY);
    }
}
