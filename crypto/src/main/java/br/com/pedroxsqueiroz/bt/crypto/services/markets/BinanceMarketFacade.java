package br.com.pedroxsqueiroz.bt.crypto.services.markets;

import br.com.pedroxsqueiroz.bt.crypto.config.BinanceConfig;
import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.dtos.Wallet;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
import br.com.pedroxsqueiroz.bt.crypto.factories.RequestFactory;
import br.com.pedroxsqueiroz.bt.crypto.services.EntryValidator;
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
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import java.math.RoundingMode;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Configuration
@Component("binance")
public class BinanceMarketFacade extends MarketFacade {

	private static final Logger LOGGER = Logger.getLogger( BinanceMarketFacade.class.getName() );	
	
	/*-----------------------------------------------------------------------------------
    STOCKYTYPE METADATA
    -------------------------------------------------------------------------------------*/

    private int stockyTypePrecision;

    /*-----------------------------------------------------------------------------------
    END OF STOCKYTYPE METADATA
    -------------------------------------------------------------------------------------*/

    BlockingQueue<SerialEntry> serialEntries;

    //FIXME: BETTER NAME AND BETTER LOGIC
    private SerialEntry currentIntervaledSerialEntry;

    private static final int MAX_BUFFER_CAPACITY = 6;

    private static final int SERIES_ENTRY_TIME = 0;
    private static final int SERIES_ENTRY_OPENING = 1;
    private static final int SERIES_ENTRY_MAX = 2;
    private static final int SERIES_ENTRY_MIN = 3;
    private static final int SERIES_ENTRY_CLOSING = 4;
    private static final int SERIES_ENTRY_VOLUME = 5;


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

    @ConfigParam(name = "entryMinAmmount")
    public Double entryMovementMinAmmount;

    private Double currentClosingPrice;

    ScheduledFuture scheduled;

    WebSocketSession currentWebSocketSession;

    @Autowired
    private BinanceConfig config;

    @Override
    public List<StockType> getSupportedStocks() {
        return null;
    }

    @SneakyThrows
    @Override
    public List<SerialEntry> fetch(StockType type, Instant from, Instant to) {

        /*
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
        */

        HttpUriRequest request = this.requestFactory
                .withRequestParams("symbol", type.getName())
                .withRequestParams("interval", String.format("%s%s", this.interval, this.intervalUnit) )
                .withRequestParams("endTime", Long.toString(to.toEpochMilli()))
                .withRequestParams("startTime", Long.toString(from.toEpochMilli()))
                .setup("GET", "klines")
                .build();

        return HttpClients.createDefault().execute(request, (response) -> {

            if(response.getStatusLine().getStatusCode() == 200)
            {
                ObjectMapper serializer = new ObjectMapper();

                InputStream content = response.getEntity().getContent();
                JsonNode jsonNode = serializer.readTree(content);

                List<SerialEntry> seriesSlice = new ArrayList<SerialEntry>();

                for( JsonNode currentEntryData : jsonNode )
                {
                    LOGGER.info(currentEntryData.toPrettyString());

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
                    currentEntry.setOpening( new BigDecimal( opening ) );
                    currentEntry.setMax( new BigDecimal( max ) );
                    currentEntry.setMin( new BigDecimal( min ) );
                    currentEntry.setClosing( new BigDecimal( closing ) );
                    currentEntry.setVolume( new BigDecimal( volume ) );

                    LOGGER.info( serializer.writeValueAsString(currentEntry) );

                    seriesSlice.add( currentEntry );
                }

                return seriesSlice.stream().sorted().collect(Collectors.toList());

            }

            return null;
        });

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
    public TradePosition entryPosition(BigDecimal ammount, StockType type) {

        TradePosition newTradePosition = TradePosition
                .builder()
                .entryTime(Instant.now())
                .entryAmmount(ammount)
                .entryValue( ammount.multiply( this.exchangeValueRate(type) ) )
                .build();

        JsonNode orderResponse = sendOrder(ammount, type, "BUY");

        if( Objects.isNull(orderResponse) )
        {
            LOGGER.info("Failed on send order to Binance");
            return null;
        }

        //double liquidAmmountExecuted = getAmmountExecuted(orderResponse);

        double executedQty = orderResponse.get("executedQty").asDouble();
        double value = orderResponse.get("cummulativeQuoteQty").asDouble();

        BigDecimal entryAmmount = new BigDecimal(executedQty);
        entryAmmount.setScale(this.stockyTypePrecision, RoundingMode.HALF_UP );
        newTradePosition.setEntryAmmount(entryAmmount);
        BigDecimal entryValue = new BigDecimal(value);
        entryValue.setScale(this.stockyTypePrecision, RoundingMode.HALF_UP);
        newTradePosition.setEntryValue(entryValue);

        newTradePosition.setMarketId( orderResponse.get("orderId").asText());

        return newTradePosition;

    }

    /*
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
    */

    private JsonNode sendOrder(BigDecimal ammount, StockType type, String orderSide) throws IOException, CloneNotSupportedException {

        ObjectMapper serializer = new ObjectMapper();
        String currentStockTypeName = type.getName();

        Long serverTime = this.getCurrentServerTime();

        ammount.setScale(this.stockyTypePrecision, RoundingMode.UP);

        Matcher ammountInRangeMatcher = Pattern
                .compile(String.format("([0-9]*\\.?[0-9]{0,%s})", this.stockyTypePrecision))
                .matcher(ammount.toPlainString());

        ammountInRangeMatcher.find();

        String adjustedAmmount = ammountInRangeMatcher.group(0);

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

        return HttpClients.createDefault().execute( buyOrderRequest, response -> {

            InputStream content = response.getEntity().getContent();

            JsonNode responseJson = serializer.readTree(content);

            LOGGER.info(String.format("Trade Binance:\n%s", responseJson.toPrettyString()));

            if( response.getStatusLine().getStatusCode() != 200 )
            {
                LOGGER.info("Failed on send order to binance");
                return null;
            }

            return responseJson;
        });
    }

    private String getPrescisionForStock(String currentStockTypeName) throws IOException, CloneNotSupportedException {

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
    public TradePosition exitPosition(TradePosition trade, BigDecimal ammount, StockType type) {

        trade.setExitTime(Instant.now());
        trade.setExitAmmount( ammount );
        trade.setExitValue( ammount.multiply( this.exchangeValueRate(type)  ) );

        JsonNode orderResponse = sendOrder(ammount, type, "SELL");

        //double liquidAmmountExecuted = getAmmountExecuted(orderResponse);
        double executedQty = orderResponse.get("executedQty").asDouble();
        double value = orderResponse.get("cummulativeQuoteQty").asDouble();

        trade.setMarketId(orderResponse.get("orderId").asText());
        BigDecimal exitAmmount = new BigDecimal(executedQty);
        exitAmmount.setScale(this.stockyTypePrecision, RoundingMode.HALF_UP);
        trade.setExitAmmount(exitAmmount);
        BigDecimal exitValue = new BigDecimal(value);
        exitValue.setScale(this.stockyTypePrecision, RoundingMode.HALF_UP);
        trade.setExitValue(exitValue);

        return trade;
    }

    @Override
    public BigDecimal exchangeValueRate(StockType type) {

        //return this.currentIntervaledSerialEntry.getClosing();


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

                        return new BigDecimal( jsonNode.get("price").asDouble() );

                    });

        } catch (CloneNotSupportedException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } 

        return null;

    }

    @Override
    public Wallet getWallet() {
            
        try(CloseableHttpClient httpClient = HttpClients.createDefault())
        {
        	
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

        	return httpClient.execute(request, response -> {
        		
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
        				
        				wallet.putAmmountToStock( stockType, new BigDecimal( ammount ) );
        				
        			});
        			
        			return wallet;
        		}
        		
        		LOGGER.severe( String.format( "Error on request wallet to binance\n%s", responseJson.toPrettyString() ) );
        		
        		return null;
        	
        	});
        } catch (CloneNotSupportedException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } 

        return null;
    }

    @Override
    public EntryValidator getEntryValidator() {
        return (trade) -> trade.getEntryAmmount().compareTo( new BigDecimal(this.entryMovementMinAmmount) ) == -1  ?
                            new HashMap<Integer, String>(){{
                                put( 1, String.format(
                                            "Is required unless %s ammount to entry, was provided %s",
                                                new BigDecimal(entryMovementMinAmmount).toPlainString(),
                                                trade.getEntryAmmount().toPlainString()
                                            )
                                    );
                            }}: null;
    }

    private Long getCurrentServerTime() throws IOException, CloneNotSupportedException {
        
    	try(CloseableHttpClient httpClient = HttpClients.createDefault())
    	{
    		
    		return httpClient.execute(
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

                    currentIntervaledSerialEntry = this.currentSerialEntry;

                    timeLastCandle = closingTime;

                }

                BigDecimal opening =    new BigDecimal( klineRoot.get("o").asDouble() );
                BigDecimal closing =    new BigDecimal( klineRoot.get("c").asDouble() );
                BigDecimal max =        new BigDecimal( klineRoot.get("h").asDouble() );
                BigDecimal min =        new BigDecimal( klineRoot.get("l").asDouble() );
                BigDecimal volume =     new BigDecimal( klineRoot.get("v").asDouble() );

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

    @SneakyThrows
    @Override
    public void start() throws ImpossibleToStartException {


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

            String precisionMask = this.getPrescisionForStock(this.currentStockType.getName());
            this.stockyTypePrecision = precisionMask.split( "[.]" )[1].indexOf('1') + 1;
        
    }
}
