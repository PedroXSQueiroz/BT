package br.com.pedroxsqueiroz.bt.crypto.repositories;

import br.com.pedroxsqueiroz.bt.crypto.models.TradeMovementModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TradeMovementRepository extends JpaRepository<TradeMovementModel, UUID>, JpaSpecificationExecutor<TradeMovementModel> {
}