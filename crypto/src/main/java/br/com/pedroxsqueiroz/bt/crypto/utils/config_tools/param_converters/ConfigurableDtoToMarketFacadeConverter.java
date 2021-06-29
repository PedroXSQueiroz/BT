package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.ConfigurableDto;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.services.AbstractTA4JTradeAlgorihtm;
import br.com.pedroxsqueiroz.bt.crypto.services.MarketFacade;
import br.com.pedroxsqueiroz.bt.crypto.services.TradeAlgorithm;
import br.com.pedroxsqueiroz.bt.crypto.services.impl.BinanceMarketFacade;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConfigurableDtoToMarketFacadeConverter extends ParamsToConfigurableInstanceConverter<MarketFacade> {

    @Override
    protected Class<? extends Configurable> getConfigurableClass() {
        return MarketFacade.class;
    }

    @Override
    public Class<MarketFacade> convertTo() {
        return MarketFacade.class;
    }
}
