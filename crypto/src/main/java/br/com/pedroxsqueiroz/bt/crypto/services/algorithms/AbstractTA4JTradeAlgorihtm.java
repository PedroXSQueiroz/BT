package br.com.pedroxsqueiroz.bt.crypto.services.algorithms;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStopException;
import br.com.pedroxsqueiroz.bt.crypto.services.TradeAlgorithm;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.StringToStockTypeConverter;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public abstract class AbstractTA4JTradeAlgorihtm extends TradeAlgorithm {

    protected AbstractTA4JTradeAlgorihtm()
    {
        this.barSeries  = new BaseBarSeriesBuilder().build();
    }

    private static Logger LOGGER = Logger.getLogger( AbstractTA4JTradeAlgorihtm.class.getName() );

    //@ConfigParamConverter(converters = StringToStockTypeConverter.class)
    @ConfigParam(name ="stockType", getFromParent = true)
    public StockType stockType;

    public void setStockType(StockType type)
    {
        this.stockType = type;
    }

    protected BaseBarSeries barSeries;

    private Strategy strategy;

    protected TradingRecord tradingRecord;

    private BarSeriesManager seriesManager;

    private Boolean positionIsOpen = false;

    @ConfigParam(name = "avoidNegativeProfit")
    public Boolean avoidNegativeProfit;

    protected abstract Rule getEntryRule();

    protected abstract Rule getExitRule();

    @Override
    protected void prepare() {

        /*
        List<SerialEntry> series   = this.fetchNextSeriesEntry(this.stockType);
        addEntriesToSeries(series);
        */


        this.strategy = new BaseStrategy(this.getEntryRule(), this.getExitRule());

        this.seriesManager  = new BarSeriesManager( this.barSeries );

        this.tradingRecord  = this.seriesManager.run(this.strategy);
    }

    protected void addEntriesToSeries(List<SerialEntry> series) {

        if(Objects.nonNull(series))
        {

            series
                .stream()
                .filter(Objects::nonNull)
                .forEach(serialEntry ->
                    this.barSeries.addBar(
                    serialEntry.getDate().toInstant().atZone(ZoneId.systemDefault()),
                    serialEntry.getOpening(),
                    serialEntry.getMax(),
                    serialEntry.getMin(),
                    serialEntry.getClosing(),
                    serialEntry.getVolume()
                ));

        }

    }

    @Override
    protected void logic() {

        Bar lastBarOpeningTrade = null;

        while( this.isAlive() )
        {
            final List<SerialEntry> serialEntries = this.fetchNextSeriesEntry(this.stockType);

            if( Objects.nonNull(serialEntries) )
            {
                this.addEntriesToSeries(serialEntries);

                final int endIndex = this.barSeries.getEndIndex();

                final Bar lastBar = this.barSeries.getBar(endIndex);

                Position lastPosition = this.tradingRecord.getLastPosition();

                boolean shouldEnter = this.strategy.shouldEnter(endIndex);
                boolean shouldExit = this.strategy.shouldExit(endIndex);

                if(!shouldEnter || !shouldExit)
                {
                    if(shouldEnter)
                    {
                        TradePosition enteredTradePosition = this.entryPosition(
                                TradePosition
                                        .builder()
                                        .entryTime( lastBar.getEndTime().toInstant() )
                                        .entrySerialEntry( serialEntries.get(0) )
                                        .build()
                        );

                        if( Objects.nonNull( enteredTradePosition ) )
                        {

                            this.tradingRecord.enter(
                                    endIndex,
                                    lastBar.getClosePrice(),
                                    DecimalNum.valueOf(enteredTradePosition.getEntryAmmount())
                            );

                            this.positionIsOpen = true;

                            lastBarOpeningTrade = lastBar;

                        }

                    }

                    if(shouldExit)
                    {

                        if(this.positionIsOpen)
                        {
                            //boolean canClosePosition =  ( this.avoidNegativeProfit && lastBar.getClosePrice().isGreaterThan(lastBarOpeningTrade.getClosePrice() ) )
                            //        || (!this.avoidNegativeProfit);

                            boolean canClosePosition = true;

                            if(canClosePosition)
                            {

                                Trade lastTrade = this.tradingRecord.getLastEntry();

                                if(Objects.nonNull(lastTrade))
                                {
                                    int entryTradeBarIndex = lastTrade.getIndex();
                                    TradePosition entryTradePosition = TradePosition
                                            .builder()
                                            .entryAmmount( new BigDecimal( lastTrade.getAmount().doubleValue() ) )
                                            .entryValue( new BigDecimal( lastTrade.getValue().doubleValue() ) )
                                            .entryTime( this.barSeries.getBar( entryTradeBarIndex ).getEndTime().toInstant() )
                                            .exitTime( lastBar.getEndTime().toInstant() )
                                            .exitSerialEntry( serialEntries.get(0) )
                                            .build();

                                    TradePosition exitedTradePosition = this.exitPosition( entryTradePosition );

                                    if(Objects.nonNull(exitedTradePosition))
                                    {
                                        DecimalNum exitAmmount = DecimalNum.valueOf(exitedTradePosition.getExitAmmount());
                                        Num currentClosePrice = lastBar.getClosePrice();
                                        this.tradingRecord.exit(endIndex, currentClosePrice, exitAmmount);
                                    }


                                }

                                //this.positionIsOpen = false;

                            }
                            else
                            {
                                LOGGER.info("Should close position, but the profit is negative, so will be ignored");
                            }

                        }
                    }
                }

            }
            else
            {
                try {
                    this.stop();
                } catch (ImpossibleToStopException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void closeCurrentPosition()
    {
        Trade lastTrade = this.tradingRecord.getLastEntry();

        final int endIndex = this.barSeries.getEndIndex();

        final Bar lastBar = this.barSeries.getBar(endIndex);

        if(Objects.nonNull(lastTrade))
        {
            int entryTradeBarIndex = lastTrade.getIndex();
            TradePosition entryTradePosition = TradePosition
                    .builder()
                    .entryAmmount( new BigDecimal( lastTrade.getAmount().doubleValue() ) )
                    .entryValue( new BigDecimal( lastTrade.getValue().doubleValue() ) )
                    .entryTime( this.barSeries.getBar( entryTradeBarIndex ).getEndTime().toInstant() )
                    .exitTime( lastBar.getEndTime().toInstant() )
                    .exitAmmount( new BigDecimal( lastTrade.getAmount().doubleValue() ) )
                    .exitValue( new BigDecimal( lastTrade.getAmount().doubleValue() * lastBar.getClosePrice().doubleValue() ) )
                    .build();

            //TradePosition exitedTradePosition = this.exitPosition( entryTradePosition );

            this.positionIsOpen = false;

            this.tradingRecord.exit(endIndex, lastBar.getClosePrice(), DecimalNum.valueOf(entryTradePosition.getExitAmmount()));
        }
    }
}
