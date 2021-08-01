package br.com.pedroxsqueiroz.bt.crypto.services.closeTradeListenerCallback;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.models.BotModel;
import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryModel;
import br.com.pedroxsqueiroz.bt.crypto.models.TradeMovementModel;
import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.services.BotService;
import br.com.pedroxsqueiroz.bt.crypto.services.CloseTradeListenerCallback;
import br.com.pedroxsqueiroz.bt.crypto.services.SeriesService;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("dbCloseTradeCallback")
public class DBPersistenceCloseTradeListenerCallback extends Configurable implements CloseTradeListenerCallback {

    /*
    @Autowired
    private SerialEntryRepository serialEntryRepository;

    @Autowired
    private TradeMovementRepository tradeMovementRepository;
    */

    @Autowired
    private SeriesService seriesService;

    @Autowired
    private BotService botService;

    @Override
    public void callback(TradePosition entry, TradePosition close) {

        Bot bot = (Bot) this.getParent();
        BotModel botModel = this.botService.get(bot.getId());

        TradeMovementModel entryModel = this.seriesService.getTradeMovementByMarketId( entry.getMarketId() );

        TradeMovementModel exitModel = this.seriesService.getTradeMovementByMarketId(close.getMarketId());

        if(Objects.nonNull(exitModel))
        {
            this.seriesService.putExitTradeMovementOnSerialEntry(entryModel, exitModel);
        }
        else
        {
            SerialEntryModel lastEntry = this.seriesService.getLastEntryFromSeries(botModel);
            this.seriesService.putExitTradeMovementOnSerialEntry( entryModel, close, lastEntry );
        }


    }

    @Delegate(types = Configurable.class)
    public AnnotadedFieldsConfigurer configurer = new AnnotadedFieldsConfigurer(this);

}
