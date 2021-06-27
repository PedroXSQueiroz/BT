package br.com.pedroxsqueiroz.bt.crypto.services.deprecated.impl;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.services.deprecated.SeriesService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class InvestingComCsvSeriesService implements SeriesService {

    private static Logger LOGGER = Logger.getLogger( InvestingComCsvSeriesService.class.getName() );

    @Autowired
    private CSVTradeOutuptService csvTradeOutput;

    @Autowired
    private TFTradeInputService tfService;

    @Override
    public List<SerialEntry> fecth(StockType type, Instant from, Instant to) {

        try {

            CsvToBean<SerialEntry> csvToBeanParser = this.getCsvToBeanParser( new File( args.getOptionValues("file_database").get(0) ) );

            List<SerialEntry> currentSeries =  csvToBeanParser.parse();

            List<SerialEntry> selectedEntries = currentSeries.stream().filter( entry -> {

                Instant currentEntryDate = entry.getDate().toInstant();

                return from.isBefore(currentEntryDate) && to.isAfter(currentEntryDate);

            }).collect(Collectors.toList());

            return selectedEntries;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private CsvToBean<SerialEntry> getCsvToBeanParser(File currentFile) throws FileNotFoundException {

        FileReader currentFileReader = new FileReader(currentFile);
        BufferedReader bufferedReader = new BufferedReader(currentFileReader);

        return new CsvToBeanBuilder(bufferedReader)
                .withType(SerialEntry.class)
                .build();
    }

    @Override
    public void write(List<SerialEntry> series) {

    }

    @Autowired ApplicationArguments args;

    @PostConstruct
    public void processFileDatabase(   )
    {

        List<String> stocks = args.getOptionValues("file_database_stock");
        List<String> initialDates = args.getOptionValues("file_database_stock_initialdate");
        List<String> finalDates = args.getOptionValues("file_database_stock_finaldate");
        List<String> fileDatabase = args.getOptionValues("file_database");
        List<String> outputTrade = args.getOptionValues("output_trade");


        if(     (
                    Objects.nonNull(stocks) &&
                    Objects.nonNull(initialDates) &&
                    Objects.nonNull(finalDates) &&
                    Objects.nonNull(fileDatabase) &&
                    Objects.nonNull(outputTrade)
                )
                &&
                (
                    stocks.size() == 1 &&
                    initialDates.size() == 1 &&
                    finalDates.size() == 1 &&
                    fileDatabase.size() == 1 &&
                    outputTrade.size() == 1
                )
            )
        {
            try {

                String stock = stocks.get(0);
                String initialDateStr = initialDates.get(0);
                String finalDateStr = finalDates.get(0);

                StockType stockType = new StockType();
                stockType.setName(stock);

                Instant initialDate = LocalDateTime.parse(initialDateStr).atZone(ZoneId.systemDefault()).toInstant();
                Instant finalDate = LocalDateTime.parse(finalDateStr).atZone(ZoneId.systemDefault()).toInstant();

                List<SerialEntry> fecth = this.fecth(stockType, initialDate, finalDate);

                List<TradePosition> trades = this.tfService.generate(fecth, stockType);

                this.csvTradeOutput.write(trades, new FileOutputStream(outputTrade.get(0)));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else
        {
            LOGGER.info("No files to process");
        }


    }
}
