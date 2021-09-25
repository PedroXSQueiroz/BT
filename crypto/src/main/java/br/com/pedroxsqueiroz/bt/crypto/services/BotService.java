package br.com.pedroxsqueiroz.bt.crypto.services;


import br.com.pedroxsqueiroz.bt.crypto.controllers.BotController;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStopException;
import br.com.pedroxsqueiroz.bt.crypto.models.BotModel;
import br.com.pedroxsqueiroz.bt.crypto.repositories.BotRepository;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class BotService {

    private static Map<UUID, Bot> REGISTERED_BOTS = new HashMap<UUID, Bot>();

    @Autowired
    private ConfigurableParamsUtils configurableParamsUtils;

    //FIXME: THIS SHOULD NOT BE NECESSARY
    @Autowired
    private SeriesService seriesService;

    @Autowired
    private BotRepository botRepository;

    private static Logger LOGGER = Logger.getLogger( BotController.class.getName() );

    public Bot create(Map<String, Object> botParamsDto) {

        Bot bot = new Bot();

        //FIXME: THIS SHOULD NOT BE NECESSARY
        bot.setSeriesService(this.seriesService);
        bot.setBotService(this);

        Map<String, Object> resolvedBotParams = this.configurableParamsUtils
                .extractConfigParamRawValuesMap(botParamsDto, bot);

        bot.config(resolvedBotParams);

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
