package br.com.pedroxsqueiroz.bt.crypto.services.markets;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.dtos.Wallet;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
import br.com.pedroxsqueiroz.bt.crypto.services.MarketFacade;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.PathStringToInpuStreamParamConverter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.Instant;
import java.util.*;

@Component("csv")
public class CsvMarketFacade extends MarketFacade {

    @ConfigParamConverter(converters = {PathStringToInpuStreamParamConverter.class})
    @ConfigParam(name = "source")
    public InputStream source;

    private CsvToBean<SerialEntry> parser;

    private Iterator<SerialEntry> entries;

    private SerialEntry lastSerialEntry;

    @Override
    public List<StockType> getSupportedStocks() {
        return null;
    }

    @Override
    public List<SerialEntry> fetch(StockType type, Instant from, Instant to) {
        return null;
    }

    @Override
    public List<SerialEntry> fetchNext(StockType type) {

        if(this.entries.hasNext()){
            this.lastSerialEntry = entries.next();
            return  new ArrayList<SerialEntry>() {{ add(lastSerialEntry); }} ;
        }

        return null;
    }

    @Override
    public TradePosition entryPosition(Double ammount, StockType type) {

        return  TradePosition
                .builder()
                .entryTime(this.lastSerialEntry.getDate().toInstant())
                .entryAmmount(ammount)
                .entryValue( this.exchangeValueRate(type) * ammount )
                .build();
    }

    @Override
    public TradePosition exitPosition(TradePosition position, Double ammount, StockType type) {

        position.setExitAmmount( ammount );
        position.setExitValue( this.exchangeValueRate(type) * ammount );

        return position;
    }

    @Override
    public Double exchangeValueRate(StockType type) {
        return this.lastSerialEntry.getClosing();
    }

    @Override
    public Wallet getWallet() {
        return null;
    }

    private CsvToBean<SerialEntry> getCsvToBeanParser(InputStream source)  {

        BufferedReader bufferedReader   = new BufferedReader(new InputStreamReader(source));

        return new CsvToBeanBuilder(bufferedReader)
                .withType(SerialEntry.class)
                .build();
    }

    @Delegate( types = Configurable.class )
    private AnnotadedFieldsConfigurer<CsvMarketFacade> configurer = new AnnotadedFieldsConfigurer<CsvMarketFacade>(this);

    @Override
    public void finish() {

    }

    @Override
    public void start() throws ImpossibleToStartException {
        this.parser = this.getCsvToBeanParser(this.source);

        this.entries = this.parser.stream()
                .sorted(( current, comparing ) -> current.getDate().compareTo(comparing.getDate()))
                .iterator();
    }
}
