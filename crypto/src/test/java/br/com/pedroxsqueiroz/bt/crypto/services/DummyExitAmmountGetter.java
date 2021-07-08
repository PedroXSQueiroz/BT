package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;

@InjectInConfigParam(alias = "dummy")
public class DummyExitAmmountGetter extends ExitAmmountGetter {

    @Override
    public Double get(TradePosition openTrade) {
        return null;
    }

}
