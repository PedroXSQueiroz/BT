package br.com.pedroxsqueiroz.bt.crypto.services.openTradeListenerCallbacks;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryModel;
import br.com.pedroxsqueiroz.bt.crypto.models.TradeMovementModel;
import br.com.pedroxsqueiroz.bt.crypto.repositories.SerialEntryRepository;
import br.com.pedroxsqueiroz.bt.crypto.repositories.TradeMovementRepository;
import br.com.pedroxsqueiroz.bt.crypto.services.OpenTradeListenerCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dbOpenTradeCallback")
public class DBPersistenceOpenTradeListenerCallback implements OpenTradeListenerCallback {

    @Autowired
    private TradeMovementRepository tradeMovementRepository;

    @Autowired
    private SerialEntryRepository serialEntryRepository;

    @Override
    public void callback(TradePosition trade) {

        SerialEntryModel serialEntry = this.serialEntryRepository.findTopByOrderByTimeDesc();

        TradeMovementModel tradeMovementModel = TradeMovementModel
                .builder()
                .marketId( trade.getMarketId() )
                .ammount( trade.getEntryAmmount() )
                .value( trade.getEntryValue() )
                .serialEntry(serialEntry)
                .type( TradeMovementTypeEnum.ENTRY )
                .build();

        this.tradeMovementRepository.save(tradeMovementModel);

    }
}
