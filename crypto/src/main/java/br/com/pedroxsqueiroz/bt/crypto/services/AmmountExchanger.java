package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.experimental.Delegate;

import java.math.BigDecimal;


public abstract class AmmountExchanger extends Configurable {

    protected abstract StockType getFrom();

    protected abstract StockType getTo();

    protected abstract BigDecimal getRatio(StockType from, StockType to);

    public BigDecimal exchange(BigDecimal ammount)
    {
        return this.getRatio( this.getFrom(), this.getTo() ).multiply( ammount );
    }

    @Delegate(types = Configurable.class)
    public AnnotadedFieldsConfigurer configurer = new AnnotadedFieldsConfigurer(this);

}
