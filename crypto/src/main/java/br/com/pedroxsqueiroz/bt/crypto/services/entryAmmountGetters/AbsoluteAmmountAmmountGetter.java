package br.com.pedroxsqueiroz.bt.crypto.services.entryAmmountGetters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.Wallet;
import br.com.pedroxsqueiroz.bt.crypto.services.EntryAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;

import java.math.BigDecimal;

@InjectInConfigParam(alias = "absolute")
public class AbsoluteAmmountAmmountGetter extends EntryAmmountGetter {

    @ConfigParam(name = "ammountValue")
    public Double ammount;

    @Override
    public BigDecimal get(Wallet wallet) { return new BigDecimal( this.ammount );
    }

}
