package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.Wallet;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.experimental.Delegate;

public abstract class EntryAmmountGetter extends Configurable {

    public abstract Double get(Wallet wallet);

    @Delegate(types = Configurable.class)
    private AnnotadedFieldsConfigurer configurer = new AnnotadedFieldsConfigurer(this);

}
