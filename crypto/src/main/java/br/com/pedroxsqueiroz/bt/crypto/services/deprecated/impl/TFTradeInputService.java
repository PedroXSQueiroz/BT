package br.com.pedroxsqueiroz.bt.crypto.services.deprecated.impl;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.services.deprecated.TradeInputService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ConfigurationProperties( prefix = "tf-strategy")
@Service
public class TFTradeInputService implements TradeInputService {

    public void setUpwardThreshold( @Value("${upwardThreshold}") Double threshold  )
    {
        this.upwardThreshold = DoubleNum.valueOf(threshold);
    }

    private Num upwardThreshold;

    public void setDownwardThreshold( @Value("${downwardThreshold}") Double threshold )
    {
        this.downwardThreshold = DoubleNum.valueOf(threshold);
    }

    private Num downwardThreshold;

    @Override
    public List<TradePosition> generate(List<SerialEntry> series, StockType stockType) {

        BaseBarSeries barSeries = new BaseBarSeriesBuilder().withName(stockType.getName()).build();

        Collections.reverse( series );

        series.forEach( serialEntry -> barSeries.addBar(
            serialEntry.getDate().toInstant().atZone(ZoneId.of("UTC")),
            serialEntry.getOpening(),
            serialEntry.getMax(),
            serialEntry.getMin(),
            serialEntry.getClosing(),
            serialEntry.getVolume()
        ));

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(barSeries);
        RSIIndicator rsiIndicator = new RSIIndicator(closePriceIndicator, 14);
        EMAIndicator emaIndicator = new EMAIndicator(rsiIndicator, 3);

        Rule dynamicPQBuyRule = (index, tradingRecord) -> {

            Num ema = emaIndicator.getValue(index);
            Num rsi = rsiIndicator.getValue(index);

            Trade lastTrade = tradingRecord.getLastEntry();

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

            return false;
        };

        Rule dynamicPQSellRule = (index, tradingRecord) -> {

            Num ema = emaIndicator.getValue(index);
            Num rsi = rsiIndicator.getValue(index);

            Trade lastTrade = tradingRecord.getLastEntry();

            if(rsi.isLessThan(ema))
            {
                if(Objects.nonNull(lastTrade) && lastTrade.isBuy())
                {
                    return true;
                }
            }

            return false;

        };

         return new BarSeriesManager( barSeries )
                .run( new BaseStrategy(dynamicPQBuyRule, dynamicPQSellRule) )
                .getPositions()
                .stream()
                .map( position -> {

                    Trade entry = position.getEntry();
                    Trade exit = position.getExit();

                    return TradePosition
                                .builder()
                                .entryValue(entry.getValue().doubleValue())
                                .entryAmmount(entry.getAmount().doubleValue())
                                .entryTime( barSeries.getBar( entry.getIndex() ).getBeginTime().toInstant() )
                                .exitValue( exit.getValue().doubleValue() )
                                .exitAmmount( exit.getAmount().doubleValue() )
                                .exitTime( barSeries.getBar( exit.getIndex() ).getEndTime().toInstant() )
                                .build();

                }).collect( Collectors.toList() );
    }

}
