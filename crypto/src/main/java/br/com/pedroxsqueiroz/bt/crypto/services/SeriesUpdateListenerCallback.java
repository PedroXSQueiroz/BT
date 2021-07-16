package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;

import java.util.EventListener;
import java.util.List;

public interface SeriesUpdateListenerCallback extends EventListener {
    void callback(List<SerialEntry> entries);
}
