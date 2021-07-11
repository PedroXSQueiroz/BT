package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.experimental.Delegate;


public abstract class AmmountExchanger extends Configurable {

    protected abstract StockType getFrom();

    protected abstract StockType getTo();

    protected abstract Double getRatio(StockType from, StockType to);

    public  Double exchange(Double ammount)
    {
        return this.getRatio( this.getFrom(), this.getTo() ) * ammount;
    }

    @Delegate(types = Configurable.class)
    public AnnotadedFieldsConfigurer configurer = new AnnotadedFieldsConfigurer(this);

}
