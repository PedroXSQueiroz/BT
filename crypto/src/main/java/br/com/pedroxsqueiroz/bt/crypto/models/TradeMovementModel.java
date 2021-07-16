package br.com.pedroxsqueiroz.bt.crypto.models;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "trade_movement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeMovementModel {

    @Id
    @Column(name = "id_trade_movement", columnDefinition="uniqueidentifier")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    private String id;

    @Column(name = "ammount")
    private Double ammount;

    @Column(name = "total_value")
    private Double value;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "trade_movement_type")
    private TradeMovementTypeEnum type;

    @ManyToOne
    @JoinColumn(name = "id_serial_entry")
    private SerialEntryModel serialEntry;

    @ManyToOne
    @JoinColumn(name = "id_related_trade_movement")
    private TradeMovementModel relatedMovement;
}
