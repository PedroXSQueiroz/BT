package br.com.pedroxsqueiroz.bt.crypto.services.entryAmmountGetters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.Wallet;
import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;

import java.util.Map;
import java.util.Optional;

@InjectInConfigParam(alias = "percentage")
public class PercentageOfBaseStockAmmountGetter implements Bot.EntryAmmountGetter{

    @ConfigParam(name = "referenceStockType")
    private StockType referenceStockType;

    @ConfigParam(name = "percentage")
    private Double percentage;

    @Override
    public Double get(Wallet wallet) {

        Optional<Double> ammountFound = wallet
                .getStocksToAmmounts()
                .entrySet()
                .stream()
                .filter(entry ->
                        entry.getKey().equals(this.referenceStockType)
                ).map(Map.Entry::getValue)
                .findFirst();

        if(ammountFound.isPresent())
        {
            return ammountFound.get() * ( this.percentage / 100 );
        }

        return null;
    }
}
