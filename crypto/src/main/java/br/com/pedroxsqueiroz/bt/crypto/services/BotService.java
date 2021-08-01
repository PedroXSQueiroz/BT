package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import br.com.pedroxsqueiroz.bt.crypto.controllers.BotController;
import br.com.pedroxsqueiroz.bt.crypto.dtos.ResultSerialEntryDto;
import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStopException;
import br.com.pedroxsqueiroz.bt.crypto.models.BotModel;
import br.com.pedroxsqueiroz.bt.crypto.repositories.BotRepository;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@Service
public class BotService {

    private static Map<UUID, Bot> REGISTERED_BOTS = new HashMap<UUID, Bot>();

    @Autowired
    private ConfigurableParamsUtils configurableParamsUtils;

    @Autowired
    private BotRepository botRepository;

    private static Logger LOGGER = Logger.getLogger( BotController.class.getName() );

    public List<ResultSerialEntryDto> backtest(Map<String, Object> botParamsDto) throws InterruptedException {

        Bot bot = this.create(botParamsDto);

        List<ResultSerialEntryDto> result = new ArrayList<ResultSerialEntryDto>();
        ResultSerialEntryDto finalSerialEntry = new ResultSerialEntryDto();

        ArrayBlockingQueue<ResultSerialEntryDto> entrySeriesQueue = new ArrayBlockingQueue<ResultSerialEntryDto>(1);
        AtomicBoolean isAlive = new AtomicBoolean();
        isAlive.set(true);

        AtomicReference<ResultSerialEntryDto> currentEntryReference = new AtomicReference<ResultSerialEntryDto>();

        bot.addOpenTradeListener( serialEntry -> {

            ResultSerialEntryDto currentEntry = currentEntryReference.get();

            if(Objects.nonNull(currentEntry))
            {
                currentEntry.setTradeMovementType(TradeMovementTypeEnum.ENTRY);
                currentEntry.setAmmount( serialEntry.getEntryAmmount() );
            }

        });

        bot.addCloseTradeListener( ( open, close) -> {

            ResultSerialEntryDto currentEntry = currentEntryReference.get();

            if(Objects.nonNull(currentEntry))
            {
                currentEntry.setAmmount( close.getExitAmmount() );
                currentEntry.setTradeMovementType(TradeMovementTypeEnum.EXIT);
            }


        });

        bot.addSeriesUpdateTradeListener(new SeriesUpdateListenerCallback() {
                                                 @Override
                                                 public void callback(List<SerialEntry> entries) {
                                                     try {
                                                         if (Objects.nonNull(entries) && entries.size() == 1) {
                                                             entrySeriesQueue.put(new ResultSerialEntryDto(entries.get(0)));
                                                         } else {
                                                             isAlive.set(false);
                                                             entrySeriesQueue.put(finalSerialEntry);
                                                         }

                                                     } catch (InterruptedException e) {
                                                         e.printStackTrace();
                                                     }

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

        ResultSerialEntryDto currentSerialEntry;

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

        return result;

    }


    public Bot create(Map<String, Object> botParamsDto) {

        Bot bot = new Bot();

        Map<String, Object> resolvedBotParams = this.configurableParamsUtils
                .extractConfigParamRawValuesMap(botParamsDto, bot);

        bot.config(resolvedBotParams);

        this.configurableParamsUtils.resolveConfigurableTree(bot, bot.getCurrentConfiguration());

        return bot;
    }

    public UUID register(Bot bot) {

        BotModel saved = this.botRepository.save(
                BotModel
                        .builder()
                        .state(Bot.State.STOPPED)
                        .name(bot.name)
                        .build());

        UUID id = saved.getId();
        REGISTERED_BOTS.put(id, bot);

        return id;
    }

    public Bot getInstance(UUID id) {
        return REGISTERED_BOTS.get(id);
    }

    public BotModel get(UUID id)
    {
        return this.botRepository.getById(id);
    }

    public void putState(Bot.State state, Bot bot) throws ImpossibleToStartException, ImpossibleToStopException {
        switch(state)
        {
            case STARTED:

                //TODO:SHOULD MANAGE THREADS? THIS REALOCATTED TO A MICROSERVICE
                new Thread( () -> {

                    LOGGER.info("bot started");

                    try {
                        bot.start();
                    } catch (ImpossibleToStartException e) {
                        e.printStackTrace();
                    }

                    LOGGER.info("bot stopped");

                }).start();

                break;

            case STOPPED:
                bot.stop();
                break;
        }
    }
}
