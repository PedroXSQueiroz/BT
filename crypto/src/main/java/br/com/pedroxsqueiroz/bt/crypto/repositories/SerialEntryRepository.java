package br.com.pedroxsqueiroz.bt.crypto.repositories;

import br.com.pedroxsqueiroz.bt.crypto.models.BotModel;
import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryModel;
import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryViewModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SerialEntryRepository extends JpaRepository<SerialEntryModel, UUID> {

    SerialEntryModel findByTime(Instant entryTime);

    SerialEntryModel findTopByOrderByTimeDesc();

    List<SerialEntryViewModel> findByBot(UUID id);

    SerialEntryModel findTopByBotOrderByTimeDesc(BotModel bot);
}
