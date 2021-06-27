package br.com.pedroxsqueiroz.bt.crypto.dtos;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import lombok.Data;

@Data
public class BackTestSerialEntryDto extends SerialEntry{

    private Double ammount;

    private TradeMovementTypeEnum tradeMovementType;

    public BackTestSerialEntryDto()
    {
    }

    public BackTestSerialEntryDto(SerialEntry serialEntry )
    {
        this.setOpening(serialEntry.getOpening());
        this.setClosing(serialEntry.getClosing());
        this.setMax(serialEntry.getMax());
        this.setMin(serialEntry.getMin());
        this.setDate(serialEntry.getDate());
        this.setVariance(serialEntry.getVariance());
        this.setVolume(serialEntry.getVolume());
    }

    public BackTestSerialEntryDto(SerialEntry serialEntry, Double ammount, TradeMovementTypeEnum movementType )
    {
        this(serialEntry);

        this.setAmmount(ammount);

        this.setTradeMovementType(movementType);

    }

}
