package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.experimental.Delegate;

public abstract class DummyMarketFacade extends MarketFacade{

    @ConfigParam(name = "key")
    public String key;

    @Delegate( types = Configurable.class )
    private AnnotadedFieldsConfigurer configurer = new AnnotadedFieldsConfigurer(this);

}
