package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.ConfigurableDto;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.services.AbstractTA4JTradeAlgorihtm;
import br.com.pedroxsqueiroz.bt.crypto.services.TradeAlgorithm;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class ConfigurableDtoToTradeAlgorithmConverter extends ParamsToConfigurableInstanceConverter<TradeAlgorithm> {

    @Override
    protected Class<? extends Configurable> getConfigurableClass() {
        return TradeAlgorithm.class;
    }

    @SneakyThrows
    @Override
    public TradeAlgorithm convert(ConfigurableDto source) {

        StockType stockType = this.getFromParent( "stockType" );

        TradeAlgorithm algorihtm = super.convert(source);

        if( algorihtm instanceof AbstractTA4JTradeAlgorihtm )
        {
            AbstractTA4JTradeAlgorihtm ta4jAlgorithm = (AbstractTA4JTradeAlgorihtm) algorihtm;
            ta4jAlgorithm.setStockType(stockType);
        }

        return algorihtm;
    }
}
