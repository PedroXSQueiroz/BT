package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.SeriesUpdateListenerCallback;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import org.springframework.stereotype.Component;

@Component
public class ConfigurableDtoToUpdateSeriesListenerCallback extends ParamsToConfigurableInstanceConverter<SeriesUpdateListenerCallback>{

    @Override
    protected Class<? extends Configurable> getConfigurableClass() {
        return SeriesUpdateListenerCallback.class;
    }

}
