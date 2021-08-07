package br.com.pedroxsqueiroz.bt.crypto.dtos;

import com.opencsv.bean.CsvBindByName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class TradePosition {

    private String marketId;

    @CsvBindByName( column = "Data entrada")
    private Instant entryTime;

    @CsvBindByName( column = "Valor entrada" )
    private BigDecimal entryValue;

    @CsvBindByName( column = "Total cotas entrada" )
    private BigDecimal entryAmmount;

    @CsvBindByName( column = "Data saída")
    private Instant exitTime;

    @CsvBindByName( column = "Valor saída")
    private BigDecimal exitValue;

    @CsvBindByName( column = "Total cotas saída" )
    private BigDecimal exitAmmount ;

    private SerialEntry entrySerialEntry;

    private SerialEntry exitSerialEntry;

    public String[] getEntryRow()
    {
        return new String[]{
                DateTimeFormatter.ISO_DATE_TIME.format( this.entryTime.atZone( ZoneId.systemDefault() ) ),
                this.entryValue.toPlainString(),
                this.entryAmmount.toPlainString(),
                "Entrada"
        };
    }

    public String[] getExitRow()
    {
        return new String[]{
                DateTimeFormatter.ISO_DATE_TIME.format( this.exitTime.atZone( ZoneId.systemDefault() ) ),
                this.exitValue.toPlainString(),
                this.exitAmmount.toPlainString(),
                "Saída"
        };
    }

    public String[][] toEntryAndExitTable()
    {

        return new String[][] {
                this.getEntryRow(),
                this.getExitRow()
        };

    }

}
