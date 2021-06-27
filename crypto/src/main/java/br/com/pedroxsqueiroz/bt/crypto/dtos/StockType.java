package br.com.pedroxsqueiroz.bt.crypto.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
