package br.com.pedroxsqueiroz.bt.crypto.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "serial_entry")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SerialEntryModel implements Comparable<SerialEntryModel>{

    @Id
    @Type(type = "uuid-char")
    @Column(name = "id_serial_entry", columnDefinition="uniqueidentifier")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "max_price")
    private Double max;

    @Column(name = "min_price")
    private Double min;

    @Column(name = "opening_price")
    private Double opening;

    @Column(name = "closing_price")
    private Double closing;

    @Column(name = "variance_price")
    private Double variance;

    @Column(name = "entry_date")
    private Instant time;

    @ManyToOne
    @JoinColumn(name = "id_bot")
    private BotModel bot;

    @Override
    public int compareTo(@NotNull SerialEntryModel serialEntryModel) {
        return this.getTime().compareTo(serialEntryModel.getTime());
    }
}
