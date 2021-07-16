package br.com.pedroxsqueiroz.bt.crypto.repositories;

import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface SerialEntryRepository extends JpaRepository<SerialEntryModel, String> {

    SerialEntryModel findByTime(Instant entryTime);

    SerialEntryModel findTopByOrderByTimeDesc();
}
