package br.com.pedroxsqueiroz.bt.crypto.controllers;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import br.com.pedroxsqueiroz.bt.crypto.dtos.ResultSerialEntryDto;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStopException;
import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.services.BotService;
import br.com.pedroxsqueiroz.bt.crypto.services.SeriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@RestController
@RequestMapping("bot")
public class BacktestController {

    private static Logger LOGGER = Logger.getLogger( BacktestController.class.getName() );

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
        Bot bot = this.botService.create(botParamsDto);

        //TODO:ADD DEFAULT OBSERVERS FOR CHARTS
        UUID botId = this.botService.register(bot);

        AtomicReference<UUID> lastIDReference = new AtomicReference<UUID>();


        putStoreInMemoryResultTradeCallback(bot, botId, lastIDReference);


        return new ResponseEntity(botId, HttpStatus.OK);
    }

    private void putStoreInMemoryResultTradeCallback(Bot bot, UUID botId, AtomicReference<UUID> lastIDReference) {

        bot.addSeriesUpdateTradeListener( (entry) -> {
            UUID lastEntryId = this.seriesService.addEntryToSeries(botId, new ResultSerialEntryDto(entry.get(0)));
            lastIDReference.set(lastEntryId);
        });

        bot.addOpenTradeListener( (entry) -> {
            ResultSerialEntryDto openingEntry = this.seriesService.getEntry(botId, lastIDReference.get());
            openingEntry.setAmmount(entry.getEntryAmmount());
            openingEntry.setTradeMovementType(TradeMovementTypeEnum.ENTRY);
            this.seriesService.put(botId, lastIDReference.get(), openingEntry );
        });

        bot.addCloseTradeListener( (entry) -> {
            ResultSerialEntryDto closingEntry = this.seriesService.getEntry(botId, lastIDReference.get());
            closingEntry.setAmmount(entry.getExitAmmount());
            closingEntry.setTradeMovementType(TradeMovementTypeEnum.EXIT);
            this.seriesService.put(botId, lastIDReference.get(), closingEntry );
        });
    }

    @PutMapping("/{id}/state/{state}")
    @ResponseBody
    public ResponseEntity putState(
            @PathVariable("id") UUID id,
            @PathVariable("state") Bot.State state  )
            throws  ImpossibleToStartException,
                    ImpossibleToStopException {

        Bot bot = this.botService.get(id);

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
