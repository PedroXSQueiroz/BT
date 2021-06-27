package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;

public class StringToStockTypeConverter implements ParamConverter<String, StockType> {

    @Override
    public StockType convert(String source) {

        StockType stockType = new StockType();
        stockType.setName(source);

        return stockType;
    }

    @Override
    public Class convertTo() {
        return StockType.class;
    }

    @Override
    public Class convertFrom() {
        return String.class;
    }
}
