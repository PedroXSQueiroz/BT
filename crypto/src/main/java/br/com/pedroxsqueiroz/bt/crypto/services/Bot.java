package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.*;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStopException;
import br.com.pedroxsqueiroz.bt.crypto.models.BotModel;
import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryModel;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.*;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands.Startable;
import br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands.Stopable;
import lombok.experimental.Delegate;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Bot extends Configurable implements Startable, Stopable {

    private static Logger LOGGER = Logger.getLogger( Bot.class.getName() );

    public static enum State
    {
        STARTED,
        STOPPED
    }

    @ConfigParam(name = "botName")
    public String name;

    private UUID id;

    public UUID getId()
    {
        return this.id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    //FIXME: SHOULD BE OBTAINED RELATED TO MARKETFACADE
    @ConfigParamConverter( converters = {
                                            NameToAmmountExchangerConverter.class,
                                            ConfigurableDtoToAmmountExchangerConverter.class
                                        })
    @ConfigParam(name = "ammountExchanger", priority = 1)
    public AmmountExchanger ammountExchanger;

    @ConfigParamConverter( converters = ConfigurableDtoToTradeAlgorithmConverter.class)
    @ConfigParam(name = "algorithm", priority = 1)
    public TradeAlgorithm algorithm;

    @ConfigParamConverter( converters = ConfigurableDtoToMarketFacadeConverter.class)
    @ConfigParam(name = "market", priority = 1)
    public MarketFacade marketFacade;

    @ConfigParamConverter( converters = StringToStockTypeConverter.class)
    @ConfigParam(name = "stockType", priority = 0)
    public StockType type;

    @ConfigParamConverter( converters = NameToOpenTradeListenerCallbackConverter.class)
    @ConfigParam(name = "openTradeListener")
    public List<OpenTradeListenerCallback> openTradeListerners;

    @ConfigParamConverter( converters = {
                                            NameToEntryAmmountGetterConverter.class,
                                            ConfigurableDtoToEntryAmmountGetterConverter.class
                                        })
    @ConfigParam(name = "entryAmmountGetter")
    public EntryAmmountGetter entryAmmountGetter;

    @ConfigParamConverter( converters = {
                                            NameToExitAmmountGetterConverter.class,
                                            ConfigurableDtoToExitAmmountGetterConverter.class
                                        })
    @ConfigParam(name = "exitAmmountGetter")
    public ExitAmmountGetter exitAmmountGetter;

    public void addOpenTradeListener(OpenTradeListenerCallback listener)
    {
        if(Objects.isNull(this.openTradeListerners))
        {
            this.openTradeListerners = new ArrayList<OpenTradeListenerCallback>();
        }

        this.openTradeListerners.add(listener);
    }

    @ConfigParamConverter( converters = NameToCloseTradeListenerCallbackConverter.class)
    @ConfigParam(name = "closeTradeListerners")
    public List<CloseTradeListenerCallback> closeTradeListerners;

    public void addCloseTradeListener(CloseTradeListenerCallback listener)
    {
        if(Objects.isNull(this.closeTradeListerners))
        {
            this.closeTradeListerners = new ArrayList<CloseTradeListenerCallback>();
        }

        this.closeTradeListerners.add(listener);
    }

    @ConfigParamConverter( converters = {
            ConfigurableDtoToUpdateSeriesListenerCallback.class,
            NameToUpdateSeriesListenerCallbackConverter.class

    })

    @ConfigParam(name = "seriesUpdateListeners")
    public List<SeriesUpdateListenerCallback> seriesUpdateListeners;

    @ConfigParamConverter(converters = StringToInstantConverter.class)
    @ConfigParam(name = "startInterval")
    public Instant startInterval;

    @ConfigParamConverter(converters = StringToInstantConverter.class)
    @ConfigParam(name = "endInterval")
    public Instant endInterval;

    @ConfigParamConverter(converters = StringToTemporalUnitConverter.class)
    @ConfigParam(name = "intervalEntriesUnit")
    public TemporalUnit intervalEntriesUnit;

    @ConfigParam(name = "initialAmmount")
    public Double initialAmmount;

    //FIXME: THIS SHOULD NOT BE NECESSARY
    private SeriesService seriesService;
    public void setSeriesService(SeriesService service)
    {
        this.seriesService = service;
    }

    //FIXME: THIS SHOULD NOT BE NECESSARY
    private BotService botService;
    public void setBotService(BotService botService)
    {
        this.botService = botService;
    }

    private Wallet currentWallet;

    private boolean isTesting = false;

    public void addSeriesUpdateTradeListener(SeriesUpdateListenerCallback listener)
    {
        if(Objects.isNull(this.seriesUpdateListeners))
        {
            this.seriesUpdateListeners = new ArrayList<SeriesUpdateListenerCallback>();
        }

        this.seriesUpdateListeners.add(listener);
    }

    @Override
    public void start() throws ImpossibleToStartException {

        this.putLiveCallbacksToAlgorithm();

        this.marketFacade.start();

        this.algorithm.prepare();
        this.algorithm.start();

    }

    @Override
    public void stop() throws ImpossibleToStopException {

    }

    public BackTestResult test() throws ImpossibleToStartException {

        this.isTesting = true;

        Wallet testWallet = new Wallet();
        testWallet.putAmmountToStock( this.type, new BigDecimal(this.initialAmmount) );
        Wallet comparingWallet = new Wallet();
        comparingWallet.putAmmountToStock( this.type, new BigDecimal(this.initialAmmount) );

        this.currentWallet = testWallet;

        AtomicInteger openedTradesCount = new AtomicInteger(0);
        AtomicInteger closedTradesCount = new AtomicInteger(0);

        this.putBackTestCallbacksToAlgorithm(testWallet, comparingWallet, openedTradesCount, closedTradesCount);

        this.algorithm.prepare();

        ArrayBlockingQueue<SerialEntry> processingEntriesQueue = new ArrayBlockingQueue<>(1);
        AtomicBoolean isAlive = new AtomicBoolean(true);

        SerialEntry finalEntry = new SerialEntry();

        AtomicReference<SerialEntry> firstEntry = new AtomicReference<>();
        AtomicReference<SerialEntry> lastEntry = new AtomicReference<>();

        this.addSeriesUpdateTradeListener(new SeriesUpdateListenerCallback() {
            @Override
            public void callback(List<SerialEntry> entries) {

                if( Objects.isNull( firstEntry.get() ) )
                {
                    //DEFINE BETTER CRIERIA
                    firstEntry.set( entries.get(0) );
                }

                if( Objects.isNull(entries) || entries.isEmpty())
                {
                    try {
                        isAlive.set(false);
                        processingEntriesQueue.put(finalEntry);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    for(SerialEntry currentEntry : entries)
                    {
                        try {
                            processingEntriesQueue.put(currentEntry);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        });

        Thread thread = new Thread(() -> {
            try {
                this.algorithm.start();
            } catch (ImpossibleToStartException e) {
                e.printStackTrace();
            }
        });
        thread.start();

        try
        {
            SerialEntry currentEntry = null;

            while( !( currentEntry = processingEntriesQueue.take() ).equals(finalEntry) )
            {
                LOGGER.info("Istill processing");
                lastEntry.set(currentEntry);
            }

            thread.interrupt();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BackTestResult backTestResult = new BackTestResult();

        BigDecimal initialAmmountBD = new BigDecimal(this.initialAmmount);
        BigDecimal initialValue = getExchangeValueFromCurrentSerialEntry(firstEntry).multiply( initialAmmountBD );
        backTestResult.setInitialAmmount(initialAmmountBD);
        backTestResult.setInitialValue(initialValue);


        int currentOpenenedTradesCount = openedTradesCount.get();
        backTestResult.setOpnenedTradesCount(currentOpenenedTradesCount);
        int currentClosedTradesCount1 = closedTradesCount.get();
        backTestResult.setClosedTradesCount(currentClosedTradesCount1);

        this.exitAllOpened();

        backTestResult.setForcedClosedTradesCount(currentOpenenedTradesCount - currentClosedTradesCount1);


        BigDecimal finalAccumulatedValue = testWallet.getAmmountsOfStock(this.type);
        BigDecimal finalExchange = getExchangeValueFromCurrentSerialEntry(lastEntry);
        BigDecimal finalValue = finalExchange.multiply(finalAccumulatedValue);
        backTestResult.setFinalAmmount(finalAccumulatedValue);
        backTestResult.setFinalValue(finalValue);


        BigDecimal initialComparingAmmount = initialAmmountBD.multiply(getExchangeValueFromCurrentSerialEntry(firstEntry));
        BigDecimal finalComparingValue = initialComparingAmmount.multiply(lastEntry.get().getClosing());
        backTestResult.setComparingFinalValue(finalComparingValue);



        return backTestResult;

    }

    private List<TradePosition> openendTrades;

    private void putBackTestCallbacksToAlgorithm(Wallet testWallet, Wallet comparingWallet, AtomicInteger openedTradesCount, AtomicInteger closedTradesCount ) {

        this.openendTrades = new ArrayList<TradePosition>();

        AtomicReference<SerialEntry> currentEntry = new AtomicReference<>();

        this.addOpenTradeListener( trade -> {
            openedTradesCount.set( openedTradesCount.get() + 1 );
        });

        this.addCloseTradeListener( ( opended, closed ) -> {
            closedTradesCount.set( closedTradesCount.get() + 1 );
        });

        this.algorithm.setEntryMethod( tradePosition -> {

            BigDecimal entryAmmountBase = this.entryAmmountGetter.get(testWallet);

            BigDecimal exchangeRate = getExchangeValueFromCurrentSerialEntry(currentEntry);
            BigDecimal entryAmmountTarget = entryAmmountBase.multiply( exchangeRate );

            tradePosition.setEntryAmmount( entryAmmountTarget );
            tradePosition.setEntryValue( entryAmmountBase );
            tradePosition.setMarketId(UUID.randomUUID().toString());

            BigDecimal currentAmmount = testWallet.getAmmountsOfStock(this.type);
            BigDecimal remainingAmmount = currentAmmount.subtract(entryAmmountBase);

            if(remainingAmmount.signum() >= 0)
            {
                testWallet.putAmmountToStock( this.type, remainingAmmount );

                if( Objects.nonNull(this.openTradeListerners) )
                {
                    this.openTradeListerners.forEach(listener -> listener.callback(tradePosition) );
                }

                openendTrades.add(tradePosition);

                return tradePosition;
            }

            return null;

        });

        this.algorithm.setExitMethod( tradePosition -> {

            BigDecimal currentClosing = currentEntry.get().getClosing();
            BigDecimal exchangeValueRate = getExchangeValueFromCurrentSerialEntry(currentEntry);

            List<TradePosition> profitableTrades = this.getProfitableTradePositions(currentClosing, currentClosing);

            if(!profitableTrades.isEmpty())
            {

                BigDecimal resultantExitAmmount = profitableTrades
                        .stream()
                        .map(this.exitAmmountGetter::get)
                        .reduce(BigDecimal::add)
                        .get();

                BigDecimal baseResultantAmmount = resultantExitAmmount.multiply(currentClosing);

                tradePosition.setExitAmmount( resultantExitAmmount );
                tradePosition.setExitValue( baseResultantAmmount );
                tradePosition.setMarketId(UUID.randomUUID().toString());


                BigDecimal currentTotalAmmount = testWallet.getAmmountsOfStock(this.type).add(baseResultantAmmount);
                testWallet.putAmmountToStock(this.type, currentTotalAmmount);

                /*
                tradePosition.setExitAmmount(currentTotalAmmount);
                tradePosition.setExitValue(currentTotalAmmount.multiply( currentClosing ));
                */

                if(Objects.nonNull(this.closeTradeListerners))
                {
                    profitableTrades.forEach( openTrade ->
                            this.closeTradeListerners.forEach( listener -> listener.callback(openTrade, tradePosition) )
                        );
                }


                this.openendTrades.removeAll(profitableTrades);

                return tradePosition;
                
            }
            
            return null;
        });

        Duration durationInterval = Duration.between(this.startInterval, this.endInterval);

        long totalEntriesToFetch = this.getCountEntriesToFetch(durationInterval);

        int PAGE_SIZE = 500;
        int currentPageIndex = 0;

        Pageable initialPageRequest = PageRequest.of( currentPageIndex, PAGE_SIZE );
        PageImpl<SerialEntry> pageEntries = getSerialEntriesPage( totalEntriesToFetch, PAGE_SIZE, initialPageRequest,currentEntry.get() );

        AtomicReference<Page<SerialEntry>> pageEntriesReference = new AtomicReference<Page<SerialEntry>>();
        pageEntriesReference.set(pageEntries);

        Iterator<SerialEntry> entriesIterator = pageEntries.get().iterator();

        AtomicReference< Iterator <SerialEntry> > entriesIteratorReference = new AtomicReference< Iterator< SerialEntry > >();
        entriesIteratorReference.set(entriesIterator);

        AtomicInteger currentEntryIndex = new AtomicInteger(0);

        this.algorithm.setFetchNextSeriesEntryMethod( stockType -> {

            //int currentIndex = currentEntryIndex.getAndIncrement();


            /*
            if(currentIndex < entries.size())
            {
                currentEntry.set(entries.get(currentIndex));
                fetchedEntries.addAll(new ArrayList<>() {{ add( currentEntry.get() ); }});
            }
            */

            SerialEntry currentFetchedEntry = null;
            Iterator currentEntriesIterator = entriesIteratorReference.get();

            if(currentEntriesIterator.hasNext())
            {
                currentFetchedEntry = (SerialEntry) currentEntriesIterator.next();
            }
            else
            {
                Page<SerialEntry> currentPageEntries = pageEntriesReference.get();
                if ( currentPageEntries.hasNext() )
                {

                    Pageable pageable = currentPageEntries.nextPageable();
                    PageImpl<SerialEntry> nextCurrentPage = this.getSerialEntriesPage(totalEntriesToFetch, PAGE_SIZE, pageable, currentEntry.get());
                    pageEntriesReference.set(nextCurrentPage);

                    Iterator<SerialEntry> forwardEntriesIterator = nextCurrentPage.iterator();
                    entriesIteratorReference.set(forwardEntriesIterator);
                    currentFetchedEntry = forwardEntriesIterator.next();

                }

            }

            final List<SerialEntry> fetchedEntries = new ArrayList<SerialEntry>();

            fetchedEntries.add(currentFetchedEntry);
            currentEntry.set(currentFetchedEntry);

            Boolean entryWasFetched = Objects.nonNull( currentFetchedEntry );

            if ( Objects.nonNull( this.seriesUpdateListeners ) )
            {
                this.seriesUpdateListeners.forEach(listener -> listener.callback(
                                                                                    entryWasFetched ?
                                                                                    fetchedEntries:
                                                                                    null)
                                                                                );
            }

            if( Objects.isNull( currentFetchedEntry ) )
            {
                return null;
            }

            return fetchedEntries;
        });

        this.algorithm.setFetchSeriesEntriesOnIntervalMethod( (type, from, end) -> new ArrayList<SerialEntry>() );

    }

    private Long getCountEntriesToFetch(Duration durationInterval) {

        switch( (ChronoUnit) this.intervalEntriesUnit)
        {
            case MINUTES:
                return durationInterval.toMinutes();

            case HOURS:
                return durationInterval.toHours();

            //FIXME: ADD ANOTHER TIMES UNITS
        }

        return null;
    }

    @NotNull
    private PageImpl<SerialEntry> getSerialEntriesPage(long totalEntriesToFetch, int PAGE_SIZE, Pageable currentPageRequest, SerialEntry lastEntry)
    {

        int startIntervalOffset = currentPageRequest.getPageNumber() * PAGE_SIZE;

        Instant currentPageStartInterval = this.startInterval.plus(startIntervalOffset, this.intervalEntriesUnit);

        Instant currentPageEndInterval = currentPageStartInterval.plus(PAGE_SIZE, this.intervalEntriesUnit);

        currentPageEndInterval = currentPageEndInterval.isAfter(this.endInterval) ?
                                    this.endInterval :
                                    currentPageEndInterval;

        List<SerialEntry> entries = this.marketFacade.fetch(this.type,
                                                            currentPageStartInterval,
                                                            currentPageEndInterval
                                                            ).stream()
                                                            .sorted()
                                                            .collect(Collectors.toList());

        //FIXME: THIS SHOULD NOT BE NECESSARY
        if(Objects.nonNull(lastEntry))
        {

            //FIXME: THIS SHOULD NOT BE NECESSARY
            Optional<SerialEntry> intersectionLimit = entries
                    .stream()
                    .filter(entry -> entry.getDate().after(lastEntry.getDate()))
                    .findFirst();

            int intersectionLimitIndex = entries.indexOf(intersectionLimit.get());

            if(intersectionLimitIndex < 0)
            {
                return new PageImpl<>(entries, currentPageRequest, totalEntriesToFetch);
            }

            List<SerialEntry> adjustedEntries = entries.stream().skip(intersectionLimitIndex).collect(Collectors.toList());
            //FIXME: END THIS SHOULD NOT BE NECESSARY

            return new PageImpl<>(adjustedEntries, currentPageRequest, totalEntriesToFetch);
        }

        return new PageImpl<>(entries, currentPageRequest, totalEntriesToFetch);
    }

    @NotNull
    private BigDecimal getExchangeValueFromCurrentSerialEntry(AtomicReference<SerialEntry> currentEntry) {
        BigDecimal divisor = BigDecimal.valueOf(1.0D).setScale(10);
        BigDecimal exchangeRate = divisor.divide(currentEntry.get().getClosing(), 10, RoundingMode.HALF_UP);
        return exchangeRate;
    }

    private void putLiveCallbacksToAlgorithm() {

        this.openendTrades = new ArrayList<TradePosition>();

        this.algorithm.setEntryMethod( (tradePosition) -> {

            Wallet wallet = this.marketFacade.getWallet();
            BigDecimal entryAmmount = this.entryAmmountGetter.get(wallet);

            if( Objects.nonNull( this.ammountExchanger ) )
            {
                entryAmmount = this.ammountExchanger.exchange(entryAmmount);
            }

            tradePosition.setEntryAmmount( entryAmmount );

            EntryValidator entryValidator = this.marketFacade.getEntryValidator();

            if(Objects.nonNull(entryValidator))
            {
                Map<Integer, String> errors = entryValidator.validate(tradePosition);

                if(Objects.nonNull(errors))
                {
                    String errorMessage = String.format("The trade at %s has errors: %s",
                            tradePosition.getEntryTime(),
                            Strings.join(errors.values(), ',')
                    );

                    LOGGER.info(errorMessage);

                    return null;
                }
            }

            TradePosition trade = this.marketFacade.entryPosition(entryAmmount, this.type);

            if(Objects.isNull(trade))
            {
                LOGGER.info("Failed on entry position");
                return null;
            }

            //trade.setMarketId(UUID.randomUUID().toString());

            if( Objects.nonNull(this.openTradeListerners) )
            {
                this.openTradeListerners.forEach(listener -> listener.callback(trade) );
            }

            trade.setEntrySerialEntry(tradePosition.getEntrySerialEntry());

            this.openendTrades.add(trade);

            return trade;

        });

        this.algorithm.setExitMethod( (tradePosition) ->
        {

            BigDecimal exchangeValueRate = this.marketFacade.exchangeValueRate(this.type);
            BigDecimal currentClosingPrice = tradePosition.getExitSerialEntry().getClosing();

            List<TradePosition> profitableTrades = getProfitableTradePositions(exchangeValueRate, currentClosingPrice);

            BigDecimal resultantExitAmmount = profitableTrades
                                                .stream()
                                                .map(this.exitAmmountGetter::get)
                                                .reduce(BigDecimal::add)
                                                .orElseGet(() -> null);




            //TradePosition exitedTrade = this.exitTrade(tradePosition);

            if(!profitableTrades.isEmpty())
            {
                tradePosition.setExitAmmount( resultantExitAmmount );
                tradePosition.setExitValue( resultantExitAmmount.multiply(exchangeValueRate) );

                TradePosition exitedTrade = this.marketFacade.exitPosition(tradePosition, resultantExitAmmount, this.type );

                //exitedTrade.setMarketId(UUID.randomUUID().toString());

                if( Objects.nonNull( this.closeTradeListerners ) )
                {
                    profitableTrades.forEach( trade ->
                                                    this.closeTradeListerners.forEach( listener ->
                                                                                        listener.callback(trade, exitedTrade)
                                                                                    )
                                            );

                }

                this.openendTrades.removeAll(profitableTrades);

                return exitedTrade;
            }

            LOGGER.info("Profitable trade not found");

            return null;
        });

        this.algorithm.setFetchSeriesEntriesOnIntervalMethod( this.marketFacade::fetch );

        this.algorithm.setFetchNextSeriesEntryMethod( (stockType) -> {

            List<SerialEntry> serialEntries = this.marketFacade.fetchNext(stockType);

            if (Objects.nonNull(this.seriesUpdateListeners)) {
                this.seriesUpdateListeners.forEach(listener -> listener.callback(serialEntries));
            }

            return serialEntries;
        });
    }

    private List<TradePosition> getProfitableTradePositions(BigDecimal exchangeValueRate, BigDecimal currentClosingPrice) {
        
        List<TradePosition> profitableTrades = this.openendTrades.stream().filter(trade -> {

            BigDecimal openedTradeClosingPrice = trade.getEntrySerialEntry().getClosing();
            boolean currentClosingPriceIsGreaterThanOpenenTradeClosingPrice = currentClosingPrice.compareTo(openedTradeClosingPrice) > 0;

            if(currentClosingPriceIsGreaterThanOpenenTradeClosingPrice)
            {
                BigDecimal currentExitAmmount = this.exitAmmountGetter.get(trade);

                BigDecimal currentExitValue = currentExitAmmount.multiply(exchangeValueRate);
                BigDecimal profit =     currentExitValue.subtract(trade.getEntryValue());

                LOGGER.info(String.format("Trade %s Entered with ( ammount:%f, value: %f ) could exit with ( ammount: %f, value: %f, current axchange rate: %f, profit: %f )",
                        trade.getMarketId(),
                        trade.getEntryAmmount(),
                        trade.getEntryValue(),
                        currentExitAmmount,
                        currentExitValue,
                        exchangeValueRate,
                        profit
                ));

                return  profit.signum() > 0D;

            }

            return false;

        }).collect(Collectors.toList());
        return profitableTrades;
    }

    public List<TradePosition> exitAllOpened()
    {
        return this.openendTrades.stream().map(this::exitTrade).collect(Collectors.toList());
    }

    //FIXME: ON BACKTEST, SHOULD NOT INVOKE EXITPOSITION FROM MARKET
    public TradePosition exitTrade(TradePosition openTrade) {

        BigDecimal exitAmmount = this.exitAmmountGetter.get(openTrade);

        TradePosition trade;

        if(!this.isTesting)
        {
             trade = this.marketFacade.exitPosition(openTrade, exitAmmount, this.type );
        }
        else
        {

            BotModel botModel = this.botService.get(this.id);
            SerialEntryModel lastEntryFromSeries = this.seriesService.getLastEntryFromSeries(botModel);

            BigDecimal ammount = openTrade.getEntryAmmount();
            BigDecimal value = ammount.multiply(lastEntryFromSeries.getClosing());

            trade = TradePosition
                    .builder()
                    .exitAmmount(ammount)
                    .exitValue(value)
                    .marketId(UUID.randomUUID().toString())
                    .build();

            BigDecimal newAmmountInWallet = this.currentWallet.getAmmountsOfStock(this.type).add(value);
            this.currentWallet.putAmmountToStock(this.type, newAmmountInWallet);

        }

        if(Objects.nonNull(this.closeTradeListerners))
        {
            this.closeTradeListerners.forEach( listener -> listener.callback(openTrade, trade) );
        }

        this.algorithm.closeCurrentPosition();

        return trade;
    }

    @Delegate( types = Configurable.class )
    private AnnotadedFieldsConfigurer configurer = new AnnotadedFieldsConfigurer(this);

}
