package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;

import java.util.Map;

public interface EntryValidator {

    Map<Integer, String> validate(TradePosition trade);

}
