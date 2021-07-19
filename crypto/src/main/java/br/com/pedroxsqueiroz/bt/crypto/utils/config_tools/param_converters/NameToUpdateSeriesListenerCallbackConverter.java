package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.SeriesUpdateListenerCallback;
import org.springframework.stereotype.Component;

@Component
public class NameToUpdateSeriesListenerCallbackConverter extends NameClassToInstanceConverter<SeriesUpdateListenerCallback>{

    @Override
    public Class<SeriesUpdateListenerCallback> convertTo() {
        return SeriesUpdateListenerCallback.class;
    }

    @Override
    public Class<String> convertFrom() {
        return String.class;
    }
}
