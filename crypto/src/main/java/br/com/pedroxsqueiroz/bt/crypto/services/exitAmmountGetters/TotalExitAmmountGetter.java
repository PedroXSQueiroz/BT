package br.com.pedroxsqueiroz.bt.crypto.services.exitAmmountGetters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.services.ExitAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;

@InjectInConfigParam(alias = "total")
public class TotalExitAmmountGetter extends ExitAmmountGetter {

    @Override
    public Double get(TradePosition openTrade) {
        return openTrade.getEntryAmmount();
    }
}
