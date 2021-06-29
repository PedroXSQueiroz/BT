package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import br.com.pedroxsqueiroz.bt.crypto.controllers.BacktestController;
import br.com.pedroxsqueiroz.bt.crypto.dtos.BotResultSerialEntryDto;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
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

    private static Logger LOGGER = Logger.getLogger( BacktestController.class.getName() );

    public List<BotResultSerialEntryDto> backtest(Map<String, Object> botParamsDto) throws InterruptedException {

        Bot bot = this.create(botParamsDto);

        List<BotResultSerialEntryDto> result = new ArrayList<BotResultSerialEntryDto>();
        BotResultSerialEntryDto finalSerialEntry = new BotResultSerialEntryDto();

        ArrayBlockingQueue<BotResultSerialEntryDto> entrySeriesQueue = new ArrayBlockingQueue<BotResultSerialEntryDto>(1);
        AtomicBoolean isAlive = new AtomicBoolean();
        isAlive.set(true);

        AtomicReference<BotResultSerialEntryDto> currentEntryReference = new AtomicReference<BotResultSerialEntryDto>();

        bot.addOpenTradeListener( serialEntry -> {

            BotResultSerialEntryDto currentEntry = currentEntryReference.get();

            if(Objects.nonNull(currentEntry))
            {
                currentEntry.setTradeMovementType(TradeMovementTypeEnum.ENTRY);
                currentEntry.setAmmount( serialEntry.getEntryAmmount() );
            }

        });

        bot.addCloseTradeListener( serialEntry -> {

            BotResultSerialEntryDto currentEntry = currentEntryReference.get();

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
                    entrySeriesQueue.put( new BotResultSerialEntryDto( serialEntry.get(0) ) );
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

        BotResultSerialEntryDto currentSerialEntry;

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

        this.configurableParamsUtils.resolveConfigurableTree(bot, resolvedBotParams);

        bot.config(resolvedBotParams);

        return bot;
    }

    public UUID register(Bot bot) {

        UUID identifier = UUID.randomUUID();

        REGISTERED_BOTS.put(identifier, bot);

        return identifier;
    }

}
