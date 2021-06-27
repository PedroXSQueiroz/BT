package br.com.pedroxsqueiroz.bt.crypto.services.deprecated.impl;

import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.services.deprecated.TradeOutputService;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

@Service
public class CSVTradeOutuptService implements TradeOutputService {

    @Override
    public void write(List<TradePosition> trades, OutputStream output) {

        try {

            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(output));

            csvWriter.writeNext(new String[] { "Data", "Valor", "Cotas", "Tipo" });

            trades.forEach( trade -> {
                csvWriter.writeNext(trade.getEntryRow());
                csvWriter.writeNext(trade.getExitRow());
            });
            csvWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
