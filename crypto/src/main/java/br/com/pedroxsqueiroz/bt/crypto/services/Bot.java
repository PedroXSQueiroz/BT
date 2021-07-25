package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.Wallet;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStopException;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Bot extends Configurable implements Startable, Stopable {

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

    private void putCallbacksToAlgorithm() {
        this.algorithm.setEntryMethod( (tradePosition) -> {

            Wallet wallet = this.marketFacade.getWallet();
            Double entryAmmount = this.entryAmmountGetter.get(wallet);

            if( Objects.nonNull( this.ammountExchanger ) )
            {
                entryAmmount = this.ammountExchanger.exchange(entryAmmount);
            }

            TradePosition trade = this.marketFacade.entryPosition(entryAmmount, this.type);

            if( Objects.nonNull(this.openTradeListerners) )
            {
                this.openTradeListerners.forEach(listener -> listener.callback(trade) );
            }

            return trade;

        });

        this.algorithm.setExitMethod( this::exitTrade );

        this.algorithm.setFetchNextSeriesEntryMethod( (stockType) -> {

            List<SerialEntry> serialEntries = this.marketFacade.fetchNext(stockType);

            if (Objects.nonNull(this.seriesUpdateListeners)) {
                this.seriesUpdateListeners.forEach(listener -> listener.callback(serialEntries));
            }

            return serialEntries;
        });
    }

    public TradePosition exitTrade(TradePosition openTrade) {

        Double exitAmmount = this.exitAmmountGetter.get(openTrade);

        TradePosition trade = this.marketFacade.exitPosition(openTrade, exitAmmount, this.type );

        if(Objects.nonNull(this.closeTradeListerners))
        {
            this.closeTradeListerners.forEach( listener -> listener.callback(trade) );
        }

        this.algorithm.closeCurrentPosition();

        return trade;
    }

    @Delegate( types = Configurable.class )
    private AnnotadedFieldsConfigurer configurer = new AnnotadedFieldsConfigurer(this);

}
