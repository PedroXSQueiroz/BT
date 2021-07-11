package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.AmmountExchanger;
import org.springframework.stereotype.Component;

@Component
public class NameToAmmountExchangerConverter extends NameClassToInstanceConverter<AmmountExchanger>{

    @Override
    public Class<AmmountExchanger> convertTo() {
        return AmmountExchanger.class;
    }

    @Override
    public Class<String> convertFrom() {
        return String.class;
    }
}
