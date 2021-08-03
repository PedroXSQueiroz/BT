package br.com.pedroxsqueiroz.bt.crypto.services.entryAmmountGetters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.Wallet;
import br.com.pedroxsqueiroz.bt.crypto.services.EntryAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.StringToStockTypeConverter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@InjectInConfigParam(alias = "percentage")
public class PercentageOfBaseStockAmmountGetter extends EntryAmmountGetter {

    @ConfigParamConverter(converters = StringToStockTypeConverter.class)
    @ConfigParam(name = "referenceStockType")
    public StockType referenceStockType;

    @ConfigParam(name = "percentage")
    public Double percentage;

    @Override
    public BigDecimal get(Wallet wallet) {

        Optional<BigDecimal> ammountFound = wallet
                .getStocksToAmmounts()
                .entrySet()
                .stream()
                .filter(entry ->
                        entry.getKey().equals(this.referenceStockType)
                ).map(Map.Entry::getValue)
                .findFirst();

        if(ammountFound.isPresent())
        {
            return ammountFound.get().multiply( new BigDecimal( this.percentage / 100 ) );
        }

        return null;
    }
}
