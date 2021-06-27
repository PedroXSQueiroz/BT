package br.com.pedroxsqueiroz.bt.crypto.services.deprecated;

import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;

public interface WalletService {

    void setup();

    Double getTotal(StockType type);

}
