package br.com.pedroxsqueiroz.bt.crypto.services.closeTradeListenerCallback;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.models.BotModel;
import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryModel;
import br.com.pedroxsqueiroz.bt.crypto.models.TradeMovementModel;
import br.com.pedroxsqueiroz.bt.crypto.repositories.SerialEntryRepository;
import br.com.pedroxsqueiroz.bt.crypto.repositories.TradeMovementRepository;
import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.services.BotService;
import br.com.pedroxsqueiroz.bt.crypto.services.CloseTradeListenerCallback;
import br.com.pedroxsqueiroz.bt.crypto.services.SeriesService;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
    public void callback(TradePosition trade) {

        /*
        SerialEntryModel serialEntry = this.serialEntryRepository.findTopByOrderByTimeDesc();

        TradeMovementModel previousTradeMovement = this.tradeMovementRepository.findAll(
                (root, query, cb) -> {
                    query.orderBy( cb.desc( root.get("serialEntry").get("time") ) );
                    return cb.equal( root.get("type"), TradeMovementTypeEnum.ENTRY );
        }, Pageable.ofSize(1) ).getContent().get(0);

        //TradeMovementModel previousTradeMovement = previousTradeMovementOpt.get();

        TradeMovementModel tradeMovementModel = TradeMovementModel
                .builder()
                .ammount(trade.getExitAmmount())
                .value( trade.getExitAmmount() * serialEntry.getClosing() )
                .serialEntry(serialEntry)
                .relatedMovement(previousTradeMovement)
                .type(TradeMovementTypeEnum.EXIT)
                .build();

        this.tradeMovementRepository.save(tradeMovementModel);

        previousTradeMovement.setRelatedMovement(tradeMovementModel);

        this.tradeMovementRepository.save(previousTradeMovement);
        */

        Bot bot = (Bot) this.getParent();
        BotModel botModel = this.botService.get(bot.getId());

        SerialEntryModel serialEntry = this.seriesService.getLastEntryFromSeries(botModel);
        this.seriesService.putTradeMovementOnEntry(serialEntry, TradeMovementTypeEnum.EXIT, trade.getExitAmmount() );

        /*
        TradeMovementModel previousTradeMovement = this.seriesService.getLastTradeEntryMovement(botModel);

        TradeMovementModel tradeMovementModel = TradeMovementModel
                .builder()
                .ammount(trade.getExitAmmount())
                .value( trade.getExitAmmount() * serialEntry.getClosing() )
                .serialEntry(serialEntry)
                .relatedMovement(previousTradeMovement)
                .type(TradeMovementTypeEnum.EXIT)
                .build();

        tradeMovementModel
        */

        //ADD TRADEMOVEMENT TO ENTRY
        //this.seriesService.

    }

    @Delegate(types = Configurable.class)
    public AnnotadedFieldsConfigurer configurer = new AnnotadedFieldsConfigurer(this);

}
