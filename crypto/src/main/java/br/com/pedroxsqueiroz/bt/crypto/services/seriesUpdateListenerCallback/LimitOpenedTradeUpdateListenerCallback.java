package br.com.pedroxsqueiroz.bt.crypto.services.seriesUpdateListenerCallback;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.models.TradeMovementModel;
import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.services.SeriesService;
import br.com.pedroxsqueiroz.bt.crypto.services.SeriesUpdateListenerCallback;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.StringToTemporalUnitConverter;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;

@Component("timeIntervalLimitCallback")
public class LimitOpenedTradeUpdateListenerCallback extends SeriesUpdateListenerCallback {

    @Autowired
    private SeriesService seriesService;

    @ConfigParam(name = "interval")
    public Integer interval;

    @ConfigParamConverter(converters = StringToTemporalUnitConverter.class)
    @ConfigParam(name = "intervalType")
    public TemporalUnit intervalType;

    @Override
    public void callback(List<SerialEntry> entries) {

        Bot bot = (Bot) this.getParent();
        List<TradeMovementModel> trades = this.seriesService.getOpenendTradesOnBot(bot.getId());

        Instant now = Instant.now();
        Instant limit = now.minus(this.interval, this.intervalType);

        if(Objects.nonNull(entries) && !entries.isEmpty())
        {

            SerialEntry lastTrade = entries.get(0);

            trades.stream().filter( currentTrade -> {
                Instant tradeTime = currentTrade.getSerialEntry().getTime();
                return tradeTime.isBefore(limit);
            }).forEach( lossTrade -> {

                TradePosition position = TradePosition
                        .builder()
                        .entryAmmount( lossTrade.getAmmount() )
                        .entryTime( lossTrade.getSerialEntry().getTime() )
                        .exitSerialEntry(lastTrade)
                        .build();

                bot.exitTrade(position);

            });

        }


    }
}
