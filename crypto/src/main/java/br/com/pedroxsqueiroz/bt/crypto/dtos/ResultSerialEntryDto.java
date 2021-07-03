package br.com.pedroxsqueiroz.bt.crypto.dtos;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Data
public class ResultSerialEntryDto extends SerialEntry implements Comparable{

    private Double ammount;

    private TradeMovementTypeEnum tradeMovementType;

    @JsonIgnore
    private ResultSerialEntryDto entryRelatedByTrade;

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

    //FIXME: REVIEW THE CALCULLUS
    private Double getAmmountValue()
    {
        return this.getAmmount() * this.getClosing();
    }

    public Double getProfit()
    {
        if(Objects.isNull(this.entryRelatedByTrade))
        {
            return null;
        }

        Double initialValue = this.getTradeMovementType() == TradeMovementTypeEnum.ENTRY ?
                this.getAmmountValue()
                : this.entryRelatedByTrade.getAmmountValue();

        Double finalValue = this.getTradeMovementType() == TradeMovementTypeEnum.EXIT ?
                this.getAmmountValue()
                : this.entryRelatedByTrade.getAmmountValue();

        return finalValue - initialValue;
    }
}
