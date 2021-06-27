package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.StringToStockTypeConverter;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.time.ZoneId;
import java.util.List;

public abstract class AbstractTA4JTradeAlgorihtm extends TradeAlgorithm {

    //@ConfigParamConverter(converters = StringToStockTypeConverter.class)
    //@ConfigParam(name ="stockType")
    protected StockType stockType;

    public void setStockType(StockType type)
    {
        this.stockType = type;
    }

    protected BaseBarSeries barSeries;

    @Override
    protected void prepare() {

        this.barSeries  = new BaseBarSeriesBuilder().build();

        List<SerialEntry> series    = this.fetchNextSeriesEntry(this.stockType);
        addEntriesToSeries(series);
    }

    protected void addEntriesToSeries(List<SerialEntry> series) {
        series.forEach(serialEntry -> this.barSeries.addBar(
                serialEntry.getDate().toInstant().atZone(ZoneId.of("UTC")),
                serialEntry.getOpening(),
                serialEntry.getMax(),
                serialEntry.getMin(),
                serialEntry.getClosing(),
                serialEntry.getVolume()
        ));
    }
}
