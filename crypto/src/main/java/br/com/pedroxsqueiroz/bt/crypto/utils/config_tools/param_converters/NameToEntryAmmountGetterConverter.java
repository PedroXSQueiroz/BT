package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;
import org.springframework.stereotype.Component;

@Component
public class NameToEntryAmmountGetterConverter extends NameClassToInstanceConverter<Bot.EntryAmmountGetter> {

    @Override
    public Class<Bot.EntryAmmountGetter> convertTo() {
        return Bot.EntryAmmountGetter.class;
    }

    @Override
    public Class<String> convertFrom() {
        return String.class;
    }
}
