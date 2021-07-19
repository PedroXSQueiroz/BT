package br.com.pedroxsqueiroz.bt.crypto.repositories;

import br.com.pedroxsqueiroz.bt.crypto.models.BotModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BotRepository extends JpaRepository<BotModel, UUID> , JpaSpecificationExecutor<BotModel> {
}
