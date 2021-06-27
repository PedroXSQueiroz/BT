package br.com.pedroxsqueiroz.bt.crypto.controllers;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import br.com.pedroxsqueiroz.bt.crypto.dtos.BotResultSerialEntryDto;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
import br.com.pedroxsqueiroz.bt.crypto.factories.AlgorithmFactory;
import br.com.pedroxsqueiroz.bt.crypto.factories.MarketFacadeFactory;
import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.services.BotService;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@RestController
@RequestMapping("bot")
public class BacktestController {

    private static Logger LOGGER = Logger.getLogger( BacktestController.class.getName() );

    @Autowired
    private MarketFacadeFactory marketFacadeFactory;

    @Autowired
    private AlgorithmFactory algorithmFactory;

    @Autowired
    private ConfigurableParamsUtils configurableParamsUtils;

    @Autowired
    private BotService botService;

    @PostMapping("/backtest")
    @ResponseBody
    public ResponseEntity<?> runBackTest(@RequestBody Map<String, Object> botParamsDto)
            throws  ImpossibleToStartException,
                    InterruptedException {

        List<BotResultSerialEntryDto> backtest = this.botService.backtest(botParamsDto);

        return new ResponseEntity(backtest, HttpStatus.OK);
    }

    @PostMapping("/")
    @ResponseBody
    public ResponseEntity createBot(@RequestBody Map<String, Object> botParamsDto)
    {
        Bot bot = this.botService.create(botParamsDto);

        UUID id = this.botService.register(bot);

        return new ResponseEntity(id, HttpStatus.OK);
    }

}
