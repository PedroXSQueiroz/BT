package br.com.pedroxsqueiroz.bt.crypto.services.deprecated.impl;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.factories.RequestFactory;
import br.com.pedroxsqueiroz.bt.crypto.services.deprecated.SeriesService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
//import org.springframework.web.socket.client.WebSocketClient;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.sql.Date;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class BinanceSeriesService implements SeriesService {

    private static final Integer SERIES_ENTRY_TIME = 0;
    private static final Integer SERIES_ENTRY_OPENING = 1;
    private static final Integer SERIES_ENTRY_MAX = 2;
    private static final Integer SERIES_ENTRY_MIN = 3;
    private static final Integer SERIES_ENTRY_CLOSING = 4;
    private static final Integer SERIES_ENTRY_VOLUME = 5;

    private static Logger LOGGER = Logger.getLogger( BinanceSeriesService.class.getName() );

    private static Map<Instant, SerialEntry> SERIES = new HashMap<Instant, SerialEntry>();

    @Autowired
    @Qualifier("binanceRequestFactory")
    private RequestFactory requestFactory;


    // @Value("")
    // private Long intervalFecthToGetData;

    @Override
    public List<SerialEntry> fecth(StockType type, Instant from, Instant to) {

        return SERIES.keySet()
                .stream()
                .filter( currentEntryDate ->
                            from.isAfter(currentEntryDate) &&
                            to.isBefore(currentEntryDate) )
                .map( currentEntryDate -> SERIES.get(currentEntryDate) )
                .collect(Collectors.toList());
    }

    @Value("${binance.binanceSocketUrlRoot}")
    private String binanceSocketUrlRoot;

    @Scheduled( cron = "${binance.updateSeriesScheduled}")
    private void updateSeries() throws URISyntaxException, CloneNotSupportedException, IOException {

        HttpClients.createDefault().execute(
                this.requestFactory
                        .withRequestParams("interval", "1m")
                        .withRequestParams("symbol","BTCUSDT")
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

                        seriesSlice.put( dateInstant, currentEntry );
                    }

                    return null;
                }
        );

    }

    @Override
    public void write(List<SerialEntry> series) {

    }

}
