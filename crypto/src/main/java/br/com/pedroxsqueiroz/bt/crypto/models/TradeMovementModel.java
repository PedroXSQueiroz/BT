package br.com.pedroxsqueiroz.bt.crypto.models;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "trade_movement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeMovementModel {

    @Id
    @Type(type = "uuid-char")
    @Column(name = "id_trade_movement", columnDefinition="uniqueidentifier")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "ammount")
    private BigDecimal ammount;

    @Column(name = "total_value")
    private BigDecimal value;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "trade_movement_type")
    private TradeMovementTypeEnum type;

    @ManyToOne
    @JoinColumn(name = "id_serial_entry")
    private SerialEntryModel serialEntry;

    @ManyToOne
    @JoinColumn(name = "id_related_trade_movement")
    private TradeMovementModel relatedMovement;

    @Column(name = "profit")
    private BigDecimal profit;

    @Column(name = "market_id")
    private String marketId;
}
