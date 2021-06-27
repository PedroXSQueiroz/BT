package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.experimental.Delegate;

public abstract class DummyAlgorithm extends TradeAlgorithm
{
    @ConfigParam(name = "EMA")
    public Integer ema;

    @ConfigParam(name = "stockType")
    public String stockType;

    @Delegate(types = Configurable.class)
    AnnotadedFieldsConfigurer configurer = new AnnotadedFieldsConfigurer(this);

}