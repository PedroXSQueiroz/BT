package br.com.pedroxsqueiroz.bt.crypto.controllers;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import br.com.pedroxsqueiroz.bt.crypto.dtos.ConfigurableDto;
import br.com.pedroxsqueiroz.bt.crypto.dtos.ResultSerialEntryDto;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStopException;
import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.services.BotService;
import br.com.pedroxsqueiroz.bt.crypto.services.SeriesService;
import br.com.pedroxsqueiroz.bt.crypto.services.closeTradeListenerCallback.DBPersistenceCloseTradeListenerCallback;
import br.com.pedroxsqueiroz.bt.crypto.services.openTradeListenerCallbacks.DBPersistenceOpenTradeListenerCallback;
import br.com.pedroxsqueiroz.bt.crypto.services.seriesUpdateListenerCallback.DBPersistenceSeriesUpdateListenerCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Stream;

@RestController
@RequestMapping("bot")
public class BotController {

    private static Logger LOGGER = Logger.getLogger( BotController.class.getName() );

    @Autowired
    private BotService botService;

    @Autowired
    private SeriesService seriesService;

    @PostMapping("/backtest")
    @ResponseBody
    public ResponseEntity<?> runBackTest(@RequestBody Map<String, Object> botParamsDto)
            throws  ImpossibleToStartException,
                    InterruptedException {

        List<ResultSerialEntryDto> backtest = this.botService.backtest(botParamsDto);

        return new ResponseEntity(backtest, HttpStatus.OK);
    }

    @PostMapping("/")
    @ResponseBody
    public ResponseEntity createBot(@RequestBody Map<String, Object> botParamsDto)
    {
        String[] defaultOpenTradeListener = {"dbOpenTradeCallback"};

        botParamsDto.put("openTradeListener",
                botParamsDto.containsKey("openTradeListener") ?
                    Stream
                        .concat(
                            ( (List<Object>) botParamsDto.get("openTradeListener") ).stream(),
                            Arrays.stream(defaultOpenTradeListener)
                        ).toArray(size -> (Object[]) Array.newInstance( Object.class, size )):
                        defaultOpenTradeListener);

        String[] defaultCloseTradeListener = {"dbCloseTradeCallback"};

        botParamsDto.put("closeTradeListerners",

                botParamsDto.containsKey("closeTradeListerners") ?
                        Stream
                            .concat(
                                ( (List<Object>) botParamsDto.get("closeTradeListerners") ).stream(),
                                Arrays.stream(defaultCloseTradeListener)
                            ).toArray(size -> (Object[]) Array.newInstance( Object.class, size )):
                        defaultCloseTradeListener);


        String[] defaultUpdateSeriesListener = {"dbUpdateSeriesCallback"};
        Object[] updateSeriesListener = botParamsDto.containsKey("seriesUpdateListeners") ?
                Stream
                        .concat(
                                ( (List<Object>) botParamsDto.get("seriesUpdateListeners") ).stream(),
                                Arrays.stream((Object[]) defaultUpdateSeriesListener)
                        ).toArray(size -> (Object[]) Array.newInstance(Object.class, size)) :
                defaultUpdateSeriesListener;

        botParamsDto.put("seriesUpdateListeners", updateSeriesListener);

        Bot bot = this.botService.create(botParamsDto);

        //TODO:ADD DEFAULT OBSERVERS FOR CHARTS
        UUID botId = this.botService.register(bot);

        bot.setId(botId);

        return new ResponseEntity(botId, HttpStatus.OK);
    }

    @Autowired
    private DBPersistenceSeriesUpdateListenerCallback dbUpdateCallback;

    @Autowired
    private DBPersistenceOpenTradeListenerCallback dbOpenCallback;

    @Autowired
    private DBPersistenceCloseTradeListenerCallback dbCloseCallback;

    private void putDBPersistenceResultTradeCallback(Bot bot) {

        bot.addSeriesUpdateTradeListener( this.dbUpdateCallback );
        bot.addOpenTradeListener( this.dbOpenCallback );
        bot.addCloseTradeListener( this.dbCloseCallback );

    }

    /*
    private void putStoreInMemoryResultTradeCallback(Bot bot, UUID botId) {

        AtomicReference<UUID> lastIDReference = new AtomicReference<UUID>();

        AtomicReference<ResultSerialEntryDto> lastOpening = new AtomicReference<ResultSerialEntryDto>();

        bot.addSeriesUpdateTradeListener( (entry) -> {
            UUID lastEntryId = this.seriesService.addEntryToSeries(botId, new ResultSerialEntryDto(entry.get(0)));
            lastIDReference.set(lastEntryId);
        });

        bot.addOpenTradeListener( (entry) -> {
            ResultSerialEntryDto openingEntry = this.seriesService.getEntry(botId, lastIDReference.get());
            openingEntry.setAmmount(entry.getEntryAmmount());
            openingEntry.setTradeMovementType(TradeMovementTypeEnum.ENTRY);
            this.seriesService.put(botId, lastIDReference.get(), openingEntry );

            lastOpening.set( openingEntry );

        });

        bot.addCloseTradeListener( (entry) -> {
            ResultSerialEntryDto closingEntry = this.seriesService.getEntry(botId, lastIDReference.get());
            closingEntry.setAmmount(entry.getExitAmmount());
            closingEntry.setTradeMovementType(TradeMovementTypeEnum.EXIT);
            this.seriesService.put(botId, lastIDReference.get(), closingEntry );

            ResultSerialEntryDto lastOpeningCurrentTrade = lastOpening.get();

            closingEntry.setProfit(
                    ( entry.getEntryAmmount() * closingEntry.getClosing() ) -
                    ( lastOpeningCurrentTrade.getAmmount() * lastOpeningCurrentTrade.getClosing() )
            );
        });
    }
    */

    @PutMapping("/{id}/state/{state}")
    @ResponseBody
    public ResponseEntity putState(
            @PathVariable("id") UUID id,
            @PathVariable("state") Bot.State state  )
            throws  ImpossibleToStartException,
                    ImpossibleToStopException {

        Bot bot = this.botService.getInstance(id);

        this.botService.putState(state, bot);

        return new ResponseEntity(HttpStatus.OK);
    }



    @GetMapping("/{id}/series/")
    @ResponseBody
    public ResponseEntity getGeneratedSeries(@PathVariable("id") UUID id)
    {
        return new ResponseEntity( this.seriesService.getResultSeries(id), HttpStatus.OK );
    }

}
