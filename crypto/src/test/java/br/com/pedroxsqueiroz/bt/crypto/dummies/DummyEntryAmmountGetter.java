package br.com.pedroxsqueiroz.bt.crypto.dummies;

import br.com.pedroxsqueiroz.bt.crypto.dtos.Wallet;
import br.com.pedroxsqueiroz.bt.crypto.services.EntryAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;

import java.math.BigDecimal;

@InjectInConfigParam(alias = "dummy")
public class DummyEntryAmmountGetter extends EntryAmmountGetter {

    @Override
    public BigDecimal get(Wallet wallet) {
        return null;
    }

}
