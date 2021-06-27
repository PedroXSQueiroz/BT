package br.com.pedroxsqueiroz.bt_api.dtos;

import lombok.Data;

import java.time.Period;
import java.util.Calendar;
import java.util.List;

@Data
public class Series {

    List<Double> itens;

    Period period;

    StockType stockType;

}
