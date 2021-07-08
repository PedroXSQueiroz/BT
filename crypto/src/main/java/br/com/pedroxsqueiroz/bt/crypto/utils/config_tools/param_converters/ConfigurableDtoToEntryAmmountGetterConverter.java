package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.EntryAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import org.springframework.stereotype.Component;

@Component
public class ConfigurableDtoToEntryAmmountGetterConverter extends ParamsToConfigurableInstanceConverter<EntryAmmountGetter>{

    @Override
    protected Class<? extends Configurable> getConfigurableClass() {
        return EntryAmmountGetter.class;
    }

}
