package br.com.pedroxsqueiroz.bt.crypto.services.impl;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStopException;
import br.com.pedroxsqueiroz.bt.crypto.services.AbstractTA4JTradeAlgorihtm;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Component("trendFollowing")
public class TrendFollowingTradeAlgorithm extends AbstractTA4JTradeAlgorihtm {

    private static Logger LOGGER = Logger.getLogger( TrendFollowingTradeAlgorithm.class.getName() );

    private Strategy strategy;

    private TradingRecord tradingRecord;

    private BarSeriesManager seriesManager;

    private Boolean positionIsOpen = false;

    @ConfigParam(name = "avoidNegativeProfit")
    public Boolean avoidNegativeProfit;

    @Override
    protected void prepare() {

        super.prepare();

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(barSeries);
        RSIIndicator rsiIndicator = new RSIIndicator(closePriceIndicator, 14);
        EMAIndicator emaIndicator = new EMAIndicator(rsiIndicator, 3);

        final int OVERBOUGHT_THRESHOLD  = 60;
        final int OVERSOLD_THRESHOLD    = 40;

        Rule entryRule = new OverIndicatorRule(rsiIndicator, emaIndicator)
                .and(new UnderIndicatorRule(rsiIndicator, OVERBOUGHT_THRESHOLD))
                .and(new OverIndicatorRule(rsiIndicator, OVERSOLD_THRESHOLD));


        Rule exitRule = new UnderIndicatorRule(rsiIndicator, emaIndicator)
                .or(new OverIndicatorRule(rsiIndicator, OVERBOUGHT_THRESHOLD))
                .or(new UnderIndicatorRule(rsiIndicator, OVERSOLD_THRESHOLD));

        Rule dynamicPQBuyRule = (index, tradingRecord) -> {

            Num ema = emaIndicator.getValue(index);
            Num rsi = rsiIndicator.getValue(index);

            if( Objects.isNull( tradingRecord ) )
            {
                LOGGER.info("Using live trading record");
                tradingRecord = this.tradingRecord;
            }

            if( Objects.nonNull( tradingRecord ) )
            {
                Trade lastTrade = tradingRecord.getLastEntry();

                LOGGER.info("Trading record created");

                if(rsi.isGreaterThan(ema))
                {
                    if(Objects.isNull(lastTrade) || lastTrade.isSell())
                    {
                        //open long position
                    }
                    else
                    {
                        //open shor position
                    }

                    return true;

                }
            }
            else
            {
                LOGGER.info("No trading record yet");
            }


            return false;
        };

        Rule dynamicPQSellRule = (index, tradingRecord) -> {

            Num ema = emaIndicator.getValue(index);
            Num rsi = rsiIndicator.getValue(index);

            if( Objects.isNull( tradingRecord ) )
            {
                LOGGER.info("Using live trading record");
                tradingRecord = this.tradingRecord;
            }

            if(Objects.nonNull(tradingRecord))
            {
                Trade lastTrade = tradingRecord.getLastEntry();

                if(rsi.isLessThan(ema))
                {
                    if(Objects.nonNull(lastTrade) && lastTrade.isBuy())
                    {
                        return true;
                    }
                }
            }
            else
            {
                LOGGER.info("No trading record yet");
            }


            return false;

        };

        this.strategy = new BaseStrategy(dynamicPQBuyRule, dynamicPQSellRule);

        this.seriesManager  = new BarSeriesManager( this.barSeries );

        this.tradingRecord  = this.seriesManager.run(this.strategy);

    }

    @Override
    protected void stopLogic() {

    }

    @Override
    protected void logic() {

        double DUMMY_ENTRY_EXIT_AMMOUNT_BTC = 0.00052D;

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
                        //if( Objects.isNull(lastPosition) || Objects.isNull(lastPosition.getExit() ) )
                        //{

                        if(!this.positionIsOpen)
                        {
                            this.tradingRecord.enter(endIndex, lastBar.getClosePrice(), DecimalNum.valueOf(DUMMY_ENTRY_EXIT_AMMOUNT_BTC));
                            this.entryPosition(DUMMY_ENTRY_EXIT_AMMOUNT_BTC);
                            this.positionIsOpen = true;
                            lastBarOpeningTrade = lastBar;
                        }

                        //}

                    }

                    if(shouldExit)
                    {

                        if(this.positionIsOpen)
                        {
                            boolean canClosePosition =  ( this.avoidNegativeProfit && lastBar.getClosePrice().isGreaterThan(lastBarOpeningTrade.getClosePrice() ) )
                                                        || (!this.avoidNegativeProfit);
                            if(canClosePosition)
                            {

                                this.tradingRecord.exit(endIndex, lastBar.getClosePrice(), DecimalNum.valueOf(DUMMY_ENTRY_EXIT_AMMOUNT_BTC));

                                Trade lastTrade = this.tradingRecord.getLastEntry();

                                if(Objects.nonNull(lastTrade))
                                {
                                    int entryTradeBarIndex = lastTrade.getIndex();
                                    TradePosition entryTradePosition = TradePosition
                                            .builder()
                                            .entryAmmount( lastTrade.getAmount().doubleValue() )
                                            .entryValue( lastTrade.getValue().doubleValue() )
                                            .entryTime( this.barSeries.getBar( entryTradeBarIndex ).getEndTime().toInstant() )
                                            .exitTime( lastBar.getEndTime().toInstant() )
                                            .exitAmmount(DUMMY_ENTRY_EXIT_AMMOUNT_BTC)
                                            .build();

                                    this.exitPosition( entryTradePosition, DUMMY_ENTRY_EXIT_AMMOUNT_BTC);
                                }

                                this.positionIsOpen = false;

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
    public void finish() {

    }
}
