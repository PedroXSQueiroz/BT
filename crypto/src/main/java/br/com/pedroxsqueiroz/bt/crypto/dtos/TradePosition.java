package br.com.pedroxsqueiroz.bt.crypto.dtos;

import com.opencsv.bean.CsvBindByName;
import lombok.Builder;
import lombok.Data;

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
    private Double entryValue;

    @CsvBindByName( column = "Total cotas entrada" )
    private Double entryAmmount;

    @CsvBindByName( column = "Data saída")
    private Instant exitTime;

    @CsvBindByName( column = "Valor saída")
    private Double exitValue;

    @CsvBindByName( column = "Total cotas saída" )
    private Double exitAmmount ;

    public String[] getEntryRow()
    {
        return new String[]{
                DateTimeFormatter.ISO_DATE_TIME.format( this.entryTime.atZone( ZoneId.systemDefault() ) ),
                Double.toString(this.entryValue),
                Double.toString(this.entryAmmount),
                "Entrada"
        };
    }

    public String[] getExitRow()
    {
        return new String[]{
                DateTimeFormatter.ISO_DATE_TIME.format( this.exitTime.atZone( ZoneId.systemDefault() ) ),
                Double.toString(this.exitValue),
                Double.toString(this.exitAmmount),
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
