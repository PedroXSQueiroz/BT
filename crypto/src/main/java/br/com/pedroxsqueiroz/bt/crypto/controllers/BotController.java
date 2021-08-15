package br.com.pedroxsqueiroz.bt.crypto.controllers;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import br.com.pedroxsqueiroz.bt.crypto.dtos.BackTestResult;
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

        this.addDBCallbacksToParams(botParamsDto);

        Bot bot = this.botService.create(botParamsDto);

        //TODO:ADD DEFAULT OBSERVERS FOR CHARTS
        UUID botId = this.botService.register(bot);
        bot.setId(botId);

        BackTestResult result = bot.run();

        return new ResponseEntity(result, HttpStatus.OK);
    }

    @PostMapping("/")
    @ResponseBody
    public ResponseEntity createBot(@RequestBody Map<String, Object> botParamsDto)
    {
        addDBCallbacksToParams(botParamsDto);

        Bot bot = this.botService.create(botParamsDto);

        //TODO:ADD DEFAULT OBSERVERS FOR CHARTS
        UUID botId = this.botService.register(bot);

        bot.setId(botId);

        return new ResponseEntity(botId, HttpStatus.OK);
    }

    private void addDBCallbacksToParams(Map<String, Object> botParamsDto) {
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
    }

    @Autowired
    private DBPersistenceSeriesUpdateListenerCallback dbUpdateCallback;

    @Autowired
    private DBPersistenceOpenTradeListenerCallback dbOpenCallback;

    @Autowired
    private DBPersistenceCloseTradeListenerCallback dbCloseCallback;

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
    public ResponseEntity getGeneratedSeries(@PathVariable("id") String id)
    {
        return new ResponseEntity( this.seriesService.getResultSeries( UUID.fromString( id ) ), HttpStatus.OK );
    }

}
