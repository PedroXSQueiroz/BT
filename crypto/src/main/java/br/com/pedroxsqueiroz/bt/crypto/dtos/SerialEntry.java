package br.com.pedroxsqueiroz.bt.crypto.dtos;

import br.com.pedroxsqueiroz.bt.crypto.converters.CsvDateConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerialEntry {

    //@CsvCustomBindByName( column = "Data", converter = CsvDateConverter.class)
    @CsvDate("dd.MM.yyyy")
    @CsvBindByName( column = "Data" )
    private Date date;

    @CsvBindByName( column = "Abertura", locale = "en-US")
    private BigDecimal opening;

    @CsvBindByName( column = "último", locale = "en-US")
    private BigDecimal closing;

    @CsvBindByName( column = "Máxima", locale = "en-US")
    private BigDecimal max;

    @CsvBindByName( column = "Mínima", locale = "en-US")
    private BigDecimal min;

    @CsvBindByName( column = "Vol.", locale = "en-US")
    private BigDecimal volume;

    @CsvBindByName( column = "Var%", locale = "en-US")
    private BigDecimal variance;

}
