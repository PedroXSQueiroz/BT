package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.OpenTradeListenerCallback;
import org.springframework.stereotype.Component;

@Component
public class NameToOpenTradeListenerCallbackConverter extends NameClassToInstanceConverter<OpenTradeListenerCallback>{

    @Override
    public Class<OpenTradeListenerCallback> convertTo() {
        return OpenTradeListenerCallback.class;
    }

    @Override
    public Class<String> convertFrom() {
        return String.class;
    }
}
