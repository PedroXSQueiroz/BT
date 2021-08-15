package br.com.pedroxsqueiroz.bt.crypto.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BackTestResult {

    private List<ResultSerialEntryDto> serialEntries;

    private BigDecimal initialAmmount;

    private BigDecimal initialValue;

    private BigDecimal finalAmmount;

    private BigDecimal finalValue;

    private BigDecimal comparingFinalValue;

    private int opnenedTradesCount;

    private int closedTradesCount;

}
