package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;
import org.springframework.stereotype.Component;

@Component
public class NameToExitAmmountGetterConverter extends NameClassToInstanceConverter<Bot.ExitAmmountGetter> {

    @Override
    public Class<Bot.ExitAmmountGetter> convertTo() {
        return Bot.ExitAmmountGetter.class;
    }

    @Override
    public Class<String> convertFrom() {
        return String.class;
    }
}
