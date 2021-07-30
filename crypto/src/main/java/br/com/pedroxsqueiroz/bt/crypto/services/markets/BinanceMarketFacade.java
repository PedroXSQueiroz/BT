package br.com.pedroxsqueiroz.bt.crypto.services.markets;

import br.com.pedroxsqueiroz.bt.crypto.config.BinanceConfig;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import okhttp3.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    WebSocketSession currentWebSocketSession;

    @Autowired
    private BinanceConfig config;

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

    @SneakyThrows
    @Override
    public TradePosition entryPosition(Double ammount, StockType type) {

        TradePosition newTradePosition = TradePosition
                .builder()
                .entryTime(Instant.now())
                .entryAmmount(ammount)
                .entryValue(this.exchangeValueRate(type) * ammount)
                .build();

        JsonNode orderResponse = sendOrder(ammount, type, "BUY");

        double liquidAmmountExecuted = getAmmountExecuted(orderResponse);

        newTradePosition.setEntryAmmount(liquidAmmountExecuted);

        return newTradePosition;

    }

    private double getAmmountExecuted(JsonNode orderResponse) {

        double executedQty = orderResponse.get("executedQty").asDouble();

        JsonNode fills = orderResponse.get("fills");

        double commission = 0;

        for(JsonNode currentFill : fills)
        {
            commission += currentFill.get("commission").asDouble();
        }

        return  executedQty - commission;

    }

    private JsonNode sendOrder(Double ammount, StockType type, String orderSide) throws IOException, URISyntaxException, CloneNotSupportedException {

        ObjectMapper serializer = new ObjectMapper();
        String currentStockTypeName = type.getName();

        Long serverTime = this.getCurrentServerTime();

        String precisionMask = getPrescisionForStock(currentStockTypeName);
        int decimalNumberCount = precisionMask.split( "[.]" )[1].indexOf('1');

        Matcher ammountInRangeMatcher = Pattern
                .compile(String.format("([0-9]*\\.?[0-9]{0,%s})", decimalNumberCount))
                .matcher(new BigDecimal(ammount).toPlainString());
        ammountInRangeMatcher.find();
        String adjustedAmmount = ammountInRangeMatcher.group(1);

        LOGGER.info( String.format( "Sending %s order with %s ammount of %s to Binance", orderSide, adjustedAmmount, type.getName() ) );

        HttpUriRequest buyOrderRequest = this.requestFactory
                .withRequestParams("symbol", currentStockTypeName)
                .withRequestParams("side", orderSide)
                .withRequestParams("type", "MARKET")
                .withRequestParams("quantity", adjustedAmmount)
                .withRequestParams("timestamp", Long.toString(serverTime))
                .withRequestParams("recvWindow", Long.toString(30000))
                .setup("POST", "order")
                .assign()
                .build();

        return HttpClients.createDefault().execute(buyOrderRequest, response -> {

            InputStream content = response.getEntity().getContent();

            JsonNode responseJson = serializer.readTree(content);

            LOGGER.info(String.format("Trade Binance:\n%s", responseJson.toPrettyString()));

            return responseJson;
        });
    }

    private String getPrescisionForStock(String currentStockTypeName) throws IOException, URISyntaxException, CloneNotSupportedException {

        ObjectMapper serializer = new ObjectMapper();

        return HttpClients.createDefault().execute(
                this.requestFactory
                        .withRequestParams("symbol", currentStockTypeName)
                        .setup("GET", "exchangeInfo")
                        .build()
                , response -> {

                    if (response.getStatusLine().getStatusCode() == 200) {

                        JsonNode json = serializer.readTree(response.getEntity().getContent());

                        for (JsonNode currentSymbolNode : json.get("symbols")) {

                            String currentSymbolName = currentSymbolNode.get("symbol").asText();

                            if (currentSymbolName.contentEquals(currentStockTypeName)) {
                                for (JsonNode currentFilter : currentSymbolNode.get("filters")) {

                                    String currentFilterTypeName = currentFilter.get("filterType").asText();
                                    if (currentFilterTypeName.contentEquals("LOT_SIZE")) {
                                        return currentFilter.get("stepSize").asText();
                                    }

                                }

                            }

                        }

                    }

                    return null;
                });
    }

    @SneakyThrows
    @Override
    public TradePosition exitPosition(TradePosition trade, Double ammount, StockType type) {

        trade.setExitTime(Instant.now());
        trade.setExitAmmount( ammount );
        trade.setExitValue( this.exchangeValueRate(type) * ammount );

        JsonNode orderResponse = sendOrder(ammount, type, "SELL");

        double liquidAmmountExecuted = getAmmountExecuted(orderResponse);

        trade.setExitAmmount(liquidAmmountExecuted);

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

            Long serverTime = this.getCurrentServerTime();

            if(Objects.isNull(serverTime))
            {
                return null;
            }

            HttpUriRequest request = this.requestFactory
                    .withRequestParams("timestamp", Long.toString( serverTime ) )
                    .withRequestParams("recvWindow", Long.toString(30000) )
                    .setup("GET", "account")
                    .assign()
                    .build();

            return HttpClients.createDefault().execute(request, (response) -> {

                InputStream responseContent = response.getEntity().getContent();
                ObjectMapper serializer = new ObjectMapper();
                JsonNode responseJson = serializer.readTree(responseContent);

                if(response.getStatusLine().getStatusCode() == 200)
                {


                    Wallet wallet = new Wallet();

                    JsonNode balance = responseJson.get("balances");
                    balance.forEach( stock -> {

                        String stockName = stock.get("asset").asText();
                        double ammount = stock.get("free").asDouble();

                        StockType stockType = new StockType(stockName);

                        wallet.addAmmountToStock( stockType, ammount );

                    });

                    return wallet;
                }

                LOGGER.severe( String.format( "Error on request wallet to binance\n%s", responseJson.toPrettyString() ) );

                return null;

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

    private Long getCurrentServerTime() throws IOException, URISyntaxException, CloneNotSupportedException {
        return HttpClients.createDefault().execute(
                this.requestFactory
                        .setup("GET", "time")
                        .build(),
                (response) -> {

                    InputStream responseContent = response.getEntity().getContent();
                    ObjectMapper serializer = new ObjectMapper();
                    JsonNode responseJson = serializer.readTree(responseContent);

                    if (response.getStatusLine().getStatusCode() == 200) {

                        return responseJson.get("serverTime").asLong();

                    }

                    LOGGER.severe(String.format("Error on get syncronization Binance server time\n%s", responseJson));

                    return null;
                }
        );
    }

    @Delegate(types = Configurable.class)
    private AnnotadedFieldsConfigurer<BinanceMarketFacade> configurer = new AnnotadedFieldsConfigurer<BinanceMarketFacade>(this);

    @Override
    public void finish() {

    }

    public class BinanceSocketClient extends WebSocketListener {

        private ObjectMapper serializer = new ObjectMapper();

        private Long timeLastCandle;

        private SerialEntry currentSerialEntry;

        @Override
        public void onMessage(WebSocket socket, String message) {

            try
            {

                JsonNode klinePayload = this.serializer.readTree(message);

                JsonNode klineRoot = klinePayload.get("k");

                long closingTime = klineRoot.get("t").asLong();

                if(Objects.isNull(timeLastCandle))
                {
                    timeLastCandle = closingTime;
                }
                else if( closingTime > timeLastCandle ) {

                    LOGGER.info("Fetching Entry at closing");

                    serialEntries.put(this.currentSerialEntry);

                    timeLastCandle = closingTime;

                }

                double opening = klineRoot.get("o").asDouble();
                double closing = klineRoot.get("c").asDouble();
                double max = klineRoot.get("h").asDouble();
                double min = klineRoot.get("l").asDouble();
                double volume = klineRoot.get("v").asDouble();

                Date closingDatetime = Date.from(Instant.ofEpochMilli(closingTime));

                SerialEntry updatedSerialEntry = SerialEntry
                        .builder()
                        .date(closingDatetime)
                        .opening(opening)
                        .closing(closing)
                        .max(max)
                        .min(min)
                        .volume(volume)
                        .build();

                this.currentSerialEntry = updatedSerialEntry;

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }


    }

    @Override
    public void start() throws ImpossibleToStartException {



            /*
            WebSocketContainer webSocketContainer = ContainerProvider
                    .getWebSocketContainer();

            webSocketContainer
                    .connectToServer( BinanceSocketClient.class, URI.create(klineUrl) );
            */

            this.serialEntries = new ArrayBlockingQueue<SerialEntry>(MAX_BUFFER_CAPACITY);

            String websocketRoot = this.config.getWebsocketRoot();

            String klineUrl = String.format("%s/%s@kline_%s%s",
                    websocketRoot,
                    this.currentStockType.getName().toLowerCase(Locale.ROOT),
                    this.interval.toString(),
                    this.intervalUnit );

            Dispatcher dispatcher = new Dispatcher();
            OkHttpClient client = new OkHttpClient.Builder()
                    .dispatcher(dispatcher)
                    .pingInterval(20, TimeUnit.SECONDS)
                    .build();

            Request klineRequest = new Request.Builder().url(klineUrl).build();

            new Thread( () -> {

                client.newWebSocket(klineRequest, new BinanceSocketClient() );

                LOGGER.info("Socket started, connected to Binance");

            } ).start();



        /*
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
         */
    }
}
