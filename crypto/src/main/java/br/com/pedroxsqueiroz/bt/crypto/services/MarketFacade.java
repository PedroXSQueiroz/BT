package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands.Finishable;
import br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands.Startable;

import java.io.Closeable;
import java.time.Instant;
import java.util.List;

public interface MarketFacade extends Configurable, Startable, Finishable {

    List<StockType> getSupportedStocks();

    List<SerialEntry> fetch(StockType type, Instant from, Instant to );

    List<SerialEntry> fetchNext(StockType type);

    TradePosition entryPosition(Double ammount, StockType type);

    TradePosition exitPosition(TradePosition position, Double ammount, StockType type);

    Double exchangeValueRate( StockType type );

}
