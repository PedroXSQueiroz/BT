package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.Wallet;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;

@InjectInConfigParam(alias = "dummy")
public class DummyEntryAmmountGetter implements Bot.EntryAmmountGetter {

    @Override
    public Double get(Wallet wallet) {
        return null;
    }

}
