package br.com.pedroxsqueiroz.bt.crypto.controllers;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import br.com.pedroxsqueiroz.bt.crypto.dtos.BackTestSerialEntryDto;
import br.com.pedroxsqueiroz.bt.crypto.dtos.ConfigurableDto;
import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
import br.com.pedroxsqueiroz.bt.crypto.factories.AlgorithmFactory;
import br.com.pedroxsqueiroz.bt.crypto.factories.MarketFacadeFactory;
import br.com.pedroxsqueiroz.bt.crypto.services.MarketFacade;
import br.com.pedroxsqueiroz.bt.crypto.services.TradeAlgorithm;
import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.services.deprecated.impl.BinanceWalletService;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;
import com.google.common.base.CharMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@RestController
@RequestMapping("backtest")
public class BacktestController {

    private static Logger LOGGER = Logger.getLogger( BacktestController.class.getName() );

    @Autowired
    private MarketFacadeFactory marketFacadeFactory;

    @Autowired
    private AlgorithmFactory algorithmFactory;

    @Autowired
    private ConfigurableParamsUtils configurableParamsUtils;

    @PostMapping("/")
    @ResponseBody
    public ResponseEntity<?> runBackTest(
            @RequestParam(name = "from", required = false) Date from,
            @RequestParam(name = "to", required = false) Date to,
            @RequestBody Map<String, Object> botParamsDto)
            throws ImpossibleToStartException, InterruptedException {

        Bot bot = new Bot();


        Map<String, Object> resolvedBotParams = this.configurableParamsUtils
                                                            .extractConfigParamRawValuesMap(botParamsDto, bot);

        bot.config(resolvedBotParams);


        List<BackTestSerialEntryDto> result = new ArrayList<BackTestSerialEntryDto>();
        BackTestSerialEntryDto finalSerialEntry = new BackTestSerialEntryDto();

        ArrayBlockingQueue<BackTestSerialEntryDto> entrySeriesQueue = new ArrayBlockingQueue<BackTestSerialEntryDto>(1);
        AtomicBoolean isAlive = new AtomicBoolean();
        isAlive.set(true);

        AtomicReference<BackTestSerialEntryDto> currentEntryReference = new AtomicReference<BackTestSerialEntryDto>();

        bot.addOpenTradeListener( serialEntry -> {

            BackTestSerialEntryDto currentEntry = currentEntryReference.get();

            if(Objects.nonNull(currentEntry))
            {
                currentEntry.setTradeMovementType(TradeMovementTypeEnum.ENTRY);
                currentEntry.setAmmount( serialEntry.getEntryAmmount() );
            }

        });

        bot.addCloseTradeListener( serialEntry -> {

            BackTestSerialEntryDto currentEntry = currentEntryReference.get();

            if(Objects.nonNull(currentEntry))
            {
                currentEntry.setAmmount( serialEntry.getExitAmmount() );
                currentEntry.setTradeMovementType(TradeMovementTypeEnum.EXIT);
            }


        });

        bot.addSeriesUpdateTradeListener( serialEntry -> {

            try {
                if(Objects.nonNull(serialEntry) && serialEntry.size() == 1 )
                {
                    entrySeriesQueue.put( new BackTestSerialEntryDto( serialEntry.get(0) ) );
                }
                else
                {
                    isAlive.set(false);
                    entrySeriesQueue.put(finalSerialEntry);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });

        Thread thread = new Thread(() -> {
            try {
                bot.start();
            } catch (ImpossibleToStartException e) {
                e.printStackTrace();
            }
        });
        thread.start();

        LOGGER.info("bot started");

        BackTestSerialEntryDto currentSerialEntry;

        while( isAlive.get() )
        {
            currentSerialEntry = entrySeriesQueue.take();

            if(finalSerialEntry != currentSerialEntry)
            {
                currentEntryReference.set(currentSerialEntry);

                result.add(currentSerialEntry);
                LOGGER.info("result updated");
            }
        }

        thread.join();

        LOGGER.info("bot finished");

        return new ResponseEntity<List<BackTestSerialEntryDto>>( result, HttpStatus.OK );

    }

}
