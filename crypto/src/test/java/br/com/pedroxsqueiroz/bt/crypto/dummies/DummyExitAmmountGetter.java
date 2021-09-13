package br.com.pedroxsqueiroz.bt.crypto.dummies;

import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.services.ExitAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;

import java.math.BigDecimal;

@InjectInConfigParam(alias = "dummy")
public class DummyExitAmmountGetter extends ExitAmmountGetter {

    @Override
    public BigDecimal get(TradePosition openTrade) {
        return null;
    }

}
