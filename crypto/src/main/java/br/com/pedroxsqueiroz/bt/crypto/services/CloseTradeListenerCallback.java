package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;

import java.util.EventListener;

public interface CloseTradeListenerCallback extends EventListener {
    void callback(TradePosition trade);
}
