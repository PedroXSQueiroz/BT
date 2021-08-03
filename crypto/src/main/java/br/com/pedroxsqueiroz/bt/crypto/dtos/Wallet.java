package br.com.pedroxsqueiroz.bt.crypto.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class Wallet {

    Map< StockType, BigDecimal> stocksToAmmounts;

    public void addAmmountToStock( StockType stock, BigDecimal ammount )
    {

        if(Objects.isNull(this.stocksToAmmounts))
        {
            this.stocksToAmmounts = new HashMap<StockType, BigDecimal>();
        }

        this.stocksToAmmounts.put( stock, ammount );
    }

}
