package br.com.pedroxsqueiroz.bt.crypto.services.exitAmmountGetters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;

@InjectInConfigParam(alias = "total")
public class TotalExitAmmountGetter implements Bot.ExitAmmountGetter {

    @Override
    public Double get(TradePosition openTrade) {
        return openTrade.getEntryAmmount();
    }
}
