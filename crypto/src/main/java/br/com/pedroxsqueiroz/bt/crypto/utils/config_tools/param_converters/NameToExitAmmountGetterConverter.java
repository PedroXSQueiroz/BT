package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.ExitAmmountGetter;
import org.springframework.stereotype.Component;

@Component
public class NameToExitAmmountGetterConverter extends NameClassToInstanceConverter<ExitAmmountGetter> {

    @Override
    public Class<ExitAmmountGetter> convertTo() {
        return ExitAmmountGetter.class;
    }

    @Override
    public Class<String> convertFrom() {
        return String.class;
    }
}
