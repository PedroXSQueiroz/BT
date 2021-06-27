package br.com.pedroxsqueiroz.bt.crypto.services.deprecated;

import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;

import java.io.OutputStream;
import java.util.List;

public interface TradeOutputService {

    void write(List<TradePosition> trades, OutputStream outputStream);

}
