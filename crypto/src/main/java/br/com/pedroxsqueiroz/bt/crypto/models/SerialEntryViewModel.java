package br.com.pedroxsqueiroz.bt.crypto.models;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bot_trade_results")
@Data
public class SerialEntryViewModel implements Comparable<SerialEntryViewModel> {

    @Id
    @Type(type = "uuid-char")
    @Column(name = "id_serial_entry", columnDefinition="uniqueidentifier")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "max_price")
    private BigDecimal max;

    @Column(name = "min_price")
    private BigDecimal min;

    @Column(name = "opening_price")
    private BigDecimal opening;

    @Column(name = "closing_price")
    private BigDecimal closing;

    @Column(name = "variance_price")
    private BigDecimal variance;

    @Column(name = "entry_date")
    private Instant time;

    @Column(name = "ammount")
    private BigDecimal ammount;

    @Column(name = "total_value")
    private BigDecimal totalValue;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "trade_movement_type")
    private TradeMovementTypeEnum movementType;

    @Column(name = "profit")
    private BigDecimal profit;

    @ManyToOne
    @JoinColumn(name = "id_bot")
    private BotModel bot;

    @Override
    public int compareTo(@NotNull SerialEntryViewModel serialEntryViewModel) {
        return this.time.compareTo(serialEntryViewModel.getTime());
    }
}
