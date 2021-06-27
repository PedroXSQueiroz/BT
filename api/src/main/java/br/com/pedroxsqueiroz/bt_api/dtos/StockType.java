package br.com.pedroxsqueiroz.bt_api.dtos;

import lombok.Data;

@Data
public class StockType {

    private String name;

    @Override
    public boolean equals(Object other)
    {
        if( other.getClass() == StockType.class )
        {
            StockType otherType = (StockType) other;
            return otherType.getName().contentEquals( this.getName() );
        }

        return false;

    }

}
