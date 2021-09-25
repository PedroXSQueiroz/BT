package br.com.pedroxsqueiroz.bt.crypto.dummies;

import java.time.temporal.ChronoUnit;

import br.com.pedroxsqueiroz.bt.crypto.services.MarketFacade;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.experimental.Delegate;

public abstract class DummyMarketFacade extends MarketFacade{

    @ConfigParam(name = "key")
    public String key;
    
    @ConfigParam(name = "intervalEntriesUnit", getFromParent = true)
    public ChronoUnit unit;

    @Delegate( types = Configurable.class )
    private AnnotadedFieldsConfigurer configurer = new AnnotadedFieldsConfigurer(this);

}
