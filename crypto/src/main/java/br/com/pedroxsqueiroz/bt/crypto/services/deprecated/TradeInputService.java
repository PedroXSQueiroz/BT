package br.com.pedroxsqueiroz.bt.crypto.services.deprecated;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;

import java.util.List;

public interface TradeInputService {

    List<TradePosition> generate(List<SerialEntry> series, StockType stockType);

}
