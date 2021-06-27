package br.com.pedroxsqueiroz.bt.crypto.dtos;

import br.com.pedroxsqueiroz.bt.crypto.converters.CsvDateConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

@Data

public class SerialEntry {

    //@CsvCustomBindByName( column = "Data", converter = CsvDateConverter.class)
    @CsvDate("dd.MM.yyyy")
    @CsvBindByName( column = "Data" )
    private Date date;

    @CsvBindByName( column = "Abertura", locale = "en-US")
    private Double opening;

    @CsvBindByName( column = "último", locale = "en-US")
    private Double closing;

    @CsvBindByName( column = "Máxima", locale = "en-US")
    private Double max;

    @CsvBindByName( column = "Mínima", locale = "en-US")
    private Double min;

    @CsvBindByName( column = "Vol.", locale = "en-US")
    private Double volume;

    @CsvBindByName( column = "Var%", locale = "en-US")
    private Double variance;

}
