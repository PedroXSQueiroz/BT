package br.com.pedroxsqueiroz.bt.crypto.dtos;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class ResultSerialEntryDto extends SerialEntry implements Comparable{

    private Double ammount;

    private TradeMovementTypeEnum tradeMovementType;

    public ResultSerialEntryDto()
    {
    }

    public ResultSerialEntryDto(SerialEntry serialEntry )
    {
        this.setOpening(serialEntry.getOpening());
        this.setClosing(serialEntry.getClosing());
        this.setMax(serialEntry.getMax());
        this.setMin(serialEntry.getMin());
        this.setDate(serialEntry.getDate());
        this.setVariance(serialEntry.getVariance());
        this.setVolume(serialEntry.getVolume());
    }

    public ResultSerialEntryDto(SerialEntry serialEntry, Double ammount, TradeMovementTypeEnum movementType )
    {
        this(serialEntry);

        this.setAmmount(ammount);

        this.setTradeMovementType(movementType);

    }

    @Override
    public int compareTo(@NotNull Object o) {

        if(ResultSerialEntryDto.class.isAssignableFrom(o.getClass()))
        {
            ResultSerialEntryDto otherEntry = (ResultSerialEntryDto) o;

            return this.getDate().compareTo(otherEntry.getDate());
        }

        return 0;
    }
}
