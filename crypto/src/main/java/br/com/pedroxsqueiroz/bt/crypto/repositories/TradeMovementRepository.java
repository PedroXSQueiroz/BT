package br.com.pedroxsqueiroz.bt.crypto.repositories;

import br.com.pedroxsqueiroz.bt.crypto.models.TradeMovementModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeMovementRepository extends JpaRepository<TradeMovementModel, String>, JpaSpecificationExecutor<TradeMovementModel> {
}
