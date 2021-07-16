package br.com.pedroxsqueiroz.bt.crypto.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "serial_entry")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SerialEntryModel {

    @Id
    @Column(name = "id_serial_entry", columnDefinition="uniqueidentifier")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    private String id;

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
}
