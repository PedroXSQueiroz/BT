package br.com.pedroxsqueiroz.bt_api.services;

import br.com.pedroxsqueiroz.bt_api.dtos.Series;
import br.com.pedroxsqueiroz.bt_api.dtos.StockType;

import java.time.Period;

public interface SeriesService {

    Series getSeries(StockType type, Period period);

}
