package br.com.pedroxsqueiroz.bt.crypto.dtos.binance;

import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


public class BinanceBalance {

    public void setAsset(String name)
    {
        StockType assetType = new StockType();
        assetType.setName(name);
        this.assetType = assetType;
    }

    public String getAsset()
    {
        return this.assetType.getName();
    }

    public boolean isStock(StockType stockType){
        return this.assetType.equals(stockType);
    }

    private StockType assetType;

    private @Getter @Setter Double free;
}
