package br.com.pedroxsqueiroz.bt.crypto.repositories;

import br.com.pedroxsqueiroz.bt.crypto.models.BotModel;
import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryViewModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface SerialEntryViewRepository extends JpaRepository<SerialEntryViewModel, UUID> , JpaSpecificationExecutor<SerialEntryViewModel> {
    List<SerialEntryViewModel> findByBot(BotModel bot);
}
