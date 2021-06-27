package br.com.pedroxsqueiroz.bt.crypto.services.deprecated;

import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;

public interface CurrencyService {

    Double convert(StockType from, StockType to, Double value );

}
