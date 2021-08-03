package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.dtos.Wallet;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands.Finishable;
import br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands.Startable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public abstract class MarketFacade extends Configurable implements Startable, Finishable {

    public abstract List<StockType> getSupportedStocks();

    public abstract List<SerialEntry> fetch(StockType type, Instant from, Instant to );

    public abstract List<SerialEntry> fetchNext(StockType type);

    public abstract TradePosition entryPosition(BigDecimal ammount, StockType type);

    public abstract TradePosition exitPosition(TradePosition position, BigDecimal ammount, StockType type);

    public abstract BigDecimal exchangeValueRate(StockType type );

    public abstract Wallet getWallet();

    public abstract EntryValidator getEntryValidator();

}
