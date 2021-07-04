package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.TradeAlgorithm;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;

import org.springframework.stereotype.Component;

@Component
public class ConfigurableDtoToTradeAlgorithmConverter extends ParamsToConfigurableInstanceConverter<TradeAlgorithm> {

    @Override
    protected Class<? extends Configurable> getConfigurableClass() {
        return TradeAlgorithm.class;
    }

}
