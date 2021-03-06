package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.services.EntryAmmountGetter;
import org.springframework.stereotype.Component;

@Component
public class NameToEntryAmmountGetterConverter extends NameClassToInstanceConverter<EntryAmmountGetter> {

    @Override
    public Class<EntryAmmountGetter> convertTo() {
        return EntryAmmountGetter.class;
    }

    @Override
    public Class<String> convertFrom() {
        return String.class;
    }
}
