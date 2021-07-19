package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.CloseTradeListenerCallback;
import org.springframework.stereotype.Component;

@Component
public class NameToCloseTradeListenerCallbackConverter extends NameClassToInstanceConverter<CloseTradeListenerCallback> {

    @Override
    public Class<CloseTradeListenerCallback> convertTo() {
        return CloseTradeListenerCallback.class;
    }

    @Override
    public Class<String> convertFrom() {
        return String.class;
    }
}
