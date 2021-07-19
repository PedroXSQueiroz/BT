package br.com.pedroxsqueiroz.bt.crypto.models;

import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "bot")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BotModel {

    @Id
    @Type(type = "uuid-char")
    @Column(name = "id_bot", columnDefinition="uniqueidentifier")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "bot_name")
    private String name;

    @Column(name = "bot_state")
    private Bot.State state;

}
