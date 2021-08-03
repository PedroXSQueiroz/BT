package br.com.pedroxsqueiroz.bt.crypto.services.exitAmmountGetters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.services.ExitAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;

import java.math.BigDecimal;

@InjectInConfigParam(alias = "total")
public class TotalExitAmmountGetter extends ExitAmmountGetter {

    @Override
    public BigDecimal get(TradePosition openTrade) {
        return openTrade.getEntryAmmount();
    }
}
