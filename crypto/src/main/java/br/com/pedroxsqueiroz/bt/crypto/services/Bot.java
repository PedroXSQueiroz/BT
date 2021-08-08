package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.Wallet;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStopException;
import br.com.pedroxsqueiroz.bt.crypto.services.markets.BinanceMarketFacade;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.*;
import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands.Startable;
import br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands.Stopable;
import lombok.experimental.Delegate;
import org.apache.logging.log4j.util.Strings;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
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

        this.putCallbacksToAlgorithm();

        this.marketFacade.start();

        this.algorithm.prepare();
        this.algorithm.start();

    }

    @Override
    public void stop() throws ImpossibleToStopException {

    }

    public void run(Instant from, Instant to)
    {
        this.putCallbacksToAlgorithm();

        this.algorithm.prepare();
    }

    private List<TradePosition> openendTrades;

    private void putCallbacksToAlgorithm() {

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

            List<TradePosition> profitableTrades = this.openendTrades.stream().filter(trade -> {

                BigDecimal openedTradeClosingPrice = trade.getEntrySerialEntry().getClosing();
                boolean currentClosingPriceIsGreaterThanOpenenTradeClosingPrice = currentClosingPrice.compareTo(openedTradeClosingPrice) > 0;

                if(currentClosingPriceIsGreaterThanOpenenTradeClosingPrice)
                {
                    BigDecimal currentExitAmmount = this.exitAmmountGetter.get(trade);

                    BigDecimal currentExitValue = currentExitAmmount.multiply( exchangeValueRate );
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

    public TradePosition exitTrade(TradePosition openTrade) {

        BigDecimal exitAmmount = this.exitAmmountGetter.get(openTrade);

        TradePosition trade = this.marketFacade.exitPosition(openTrade, exitAmmount, this.type );

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
