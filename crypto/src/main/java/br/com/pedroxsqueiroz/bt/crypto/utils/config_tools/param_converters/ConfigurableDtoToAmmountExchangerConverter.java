package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.AmmountExchanger;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;

public class ConfigurableDtoToAmmountExchangerConverter extends ParamsToConfigurableInstanceConverter<AmmountExchanger>  {
    @Override
    protected Class<? extends Configurable> getConfigurableClass() {
        return AmmountExchanger.class;
    }
}
