package br.com.pedroxsqueiroz.bt.crypto.services.algorithms;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStopException;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Component("trendFollowing")
public class TrendFollowingTradeAlgorithm extends AbstractTA4JTradeAlgorihtm {

    private static Logger LOGGER = Logger.getLogger( TrendFollowingTradeAlgorithm.class.getName() );

    /*
    private Strategy strategy;

    private TradingRecord tradingRecord;

    private BarSeriesManager seriesManager;

    private Boolean positionIsOpen = false;
    */
    private final int OVERBOUGHT_THRESHOLD  = 60;
    private final int OVERSOLD_THRESHOLD    = 40;

    ClosePriceIndicator closePriceIndicator;
    RSIIndicator rsiIndicator;
    EMAIndicator emaIndicator;

    //@ConfigParam(name = "avoidNegativeProfit")
    //public Boolean avoidNegativeProfit;

    @Override
    protected void prepare() {

        closePriceIndicator = new ClosePriceIndicator(barSeries);
        rsiIndicator = new RSIIndicator(closePriceIndicator, 14);
        emaIndicator = new EMAIndicator(rsiIndicator, 3);

        int BAR_COUNT_PER_MINUTE_IN_A_DAY = 1440;
        barSeries.setMaximumBarCount(BAR_COUNT_PER_MINUTE_IN_A_DAY);

        super.prepare();
    }

    @Override
    protected Rule getEntryRule()
    {
        return (index, tradingRecord) -> {

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

                boolean isOutOfLimits = rsi.isLessThan(DecimalNum.valueOf(this.OVERBOUGHT_THRESHOLD)) &&
                                        rsi.isGreaterThan(DecimalNum.valueOf(this.OVERSOLD_THRESHOLD)) ;

                if( rsi.isGreaterThan(ema) && !isOutOfLimits )
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
    }

    @Override
    protected Rule getExitRule()
    {
        return (index, tradingRecord) -> {

            Num ema = emaIndicator.getValue(index);
            Num rsi = rsiIndicator.getValue(index);

            if( Objects.isNull( tradingRecord ) )
            {
                LOGGER.info("Using live trading record");
                tradingRecord = this.tradingRecord;
            }


            if(Objects.nonNull(tradingRecord))
            {
                boolean isOutOfLimits = rsi.isGreaterThan(DecimalNum.valueOf(this.OVERBOUGHT_THRESHOLD)) ||
                        rsi.isLessThan(DecimalNum.valueOf(this.OVERSOLD_THRESHOLD));

                Trade lastTrade = tradingRecord.getLastEntry();

                if(rsi.isLessThan(ema) && !isOutOfLimits)
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
    }

    @Override
    protected void stopLogic() {

    }

    @Override
    public void finish() {

    }
}
