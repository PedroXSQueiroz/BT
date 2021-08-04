package br.com.pedroxsqueiroz.bt.crypto.dtos;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryModel;
import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryViewModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Data
public class ResultSerialEntryDto extends SerialEntry {

    private BigDecimal ammount;

    private TradeMovementTypeEnum tradeMovementType;

    private BigDecimal profit;

    //@JsonIgnore
    //private ResultSerialEntryDto entryRelatedByTrade;

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

    public ResultSerialEntryDto(SerialEntryViewModel serialEntryView)
    {
        this.setOpening(serialEntryView.getOpening());
        this.setClosing(serialEntryView.getClosing());
        this.setMax(serialEntryView.getMax());
        this.setMin(serialEntryView.getMin());
        this.setDate( Date.from( serialEntryView.getTime() ) );
        this.setVariance(serialEntryView.getVariance());
        //this.setVolume(serialEntryView.get());
        this.setProfit( serialEntryView.getProfit() );
        this.setTradeMovementType( serialEntryView.getMovementType() );
        this.setAmmount(serialEntryView.getAmmount());
    }

    public ResultSerialEntryDto(SerialEntryModel serialEntryModel)
    {
        this.setOpening(serialEntryModel.getOpening());
        this.setClosing(serialEntryModel.getClosing());
        this.setMax(serialEntryModel.getMax());
        this.setMin(serialEntryModel.getMin());
        this.setDate( Date.from( serialEntryModel.getTime() ) );
        this.setVariance(serialEntryModel.getVariance());
        //this.setVolume(serialEntryModel.get());
        //this.setProfit( serialEntryModel.get );
    }

    public ResultSerialEntryDto(SerialEntry serialEntry, BigDecimal ammount, TradeMovementTypeEnum movementType )
    {
        this(serialEntry);

        this.setAmmount(ammount);

        this.setTradeMovementType(movementType);

    }

    /*
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
    */

}
