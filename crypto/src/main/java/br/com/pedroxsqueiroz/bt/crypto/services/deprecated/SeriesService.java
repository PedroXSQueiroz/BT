package br.com.pedroxsqueiroz.bt.crypto.services.deprecated;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.Series;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;

import java.time.Instant;
import java.time.Period;
import java.util.List;

public interface SeriesService {

    List<SerialEntry> fecth(StockType type, Instant from, Instant to);

    void write(List<SerialEntry> series);

}
