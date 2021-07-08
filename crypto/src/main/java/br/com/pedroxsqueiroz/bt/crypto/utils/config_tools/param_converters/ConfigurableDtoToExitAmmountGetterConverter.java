package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.ExitAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import org.springframework.stereotype.Component;

@Component
public class ConfigurableDtoToExitAmmountGetterConverter extends ParamsToConfigurableInstanceConverter<ExitAmmountGetter>{

    @Override
    protected Class<? extends Configurable> getConfigurableClass() {
        return ExitAmmountGetter.class;
    }

}
