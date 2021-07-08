package br.com.pedroxsqueiroz.bt.crypto.dtos;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class Wallet {

    Map< StockType, Double > stocksToAmmounts;

    public void addAmmountToStock( StockType stock, Double ammount )
    {

        if(Objects.isNull(this.stocksToAmmounts))
        {
            this.stocksToAmmounts = new HashMap<StockType, Double>();
        }

        this.stocksToAmmounts.put( stock, ammount );
    }

}
