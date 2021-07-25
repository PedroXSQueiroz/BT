package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.constants.TradeMovementTypeEnum;
import br.com.pedroxsqueiroz.bt.crypto.dtos.ResultSerialEntryDto;
import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.models.BotModel;
import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryModel;
import br.com.pedroxsqueiroz.bt.crypto.models.TradeMovementModel;
import br.com.pedroxsqueiroz.bt.crypto.repositories.SerialEntryRepository;
import br.com.pedroxsqueiroz.bt.crypto.repositories.SerialEntryViewRepository;
import br.com.pedroxsqueiroz.bt.crypto.repositories.TradeMovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeriesService {

    @Autowired
    private SerialEntryRepository entriesRepository;

    @Autowired
    private TradeMovementRepository tradeMovementRepository;

    @Autowired
    private SerialEntryViewRepository entriesViewRepository;

    @Autowired
    private BotService botService;

    public List<ResultSerialEntryDto> getResultSeries(UUID id)
    {
        BotModel bot = this.botService.get(id);

        return this.entriesViewRepository
                        .findByBot(bot)
                        .stream()
                        .sorted()
                        .map( ResultSerialEntryDto::new )
                        .collect(Collectors.toList());
    }

    public UUID addEntryToSeries(UUID id, ResultSerialEntryDto entry)
    {
        BotModel bot = this.botService.get(id);

        UUID newEntryUUID = UUID.randomUUID();

        saveSerialEntry( bot, newEntryUUID, entry );

        return newEntryUUID;
    }

    public UUID addEntryToSeries(UUID id, SerialEntry entry)
    {
        BotModel bot = this.botService.get(id);

        UUID newEntryUUID = UUID.randomUUID();

        saveSerialEntry( bot, newEntryUUID, entry );

        return newEntryUUID;
    }

    private void saveSerialEntry(
            BotModel bot,
            UUID entryId,
            ResultSerialEntryDto entry) {

        SerialEntryModel entryModel = SerialEntryModel
                .builder()
                .id(entryId)
                .bot(bot)
                .min(entry.getMin())
                .max(entry.getMax())
                .opening(entry.getOpening())
                .closing(entry.getClosing())
                .time(entry.getDate().toInstant())
                .variance(entry.getVariance())
                .build();

        this.entriesRepository.save(entryModel);

        TradeMovementTypeEnum tradeMovementType = entry.getTradeMovementType();
        if( Objects.nonNull(tradeMovementType) )
        {
            putTradeMovementOnEntry(entry, entryModel, tradeMovementType);
        }
    }

    private void saveSerialEntry(
            BotModel bot,
            UUID entryId,
            SerialEntry entry)
    {
        //FIXME: REFACTOR USING MODELMAPPER

        SerialEntryModel entryModel = SerialEntryModel
                .builder()
                .id(entryId)
                .bot(bot)
                .min(entry.getMin())
                .max(entry.getMax())
                .opening(entry.getOpening())
                .closing(entry.getClosing())
                .variance(entry.getVariance())
                .time(entry.getDate().toInstant())
                .build();

        this.entriesRepository.save(entryModel);
    }


    public void putTradeMovementOnEntry(ResultSerialEntryDto entry, SerialEntryModel entryModel, TradeMovementTypeEnum tradeMovementType) {

        Double ammount = entry.getAmmount();

        putTradeMovementOnEntry(entryModel, tradeMovementType, ammount);
    }

    public void putTradeMovementOnEntry(
            SerialEntryModel entryModel,
            TradeMovementTypeEnum tradeMovementType,
            Double ammount) {

        TradeMovementModel movementModel = TradeMovementModel
                .builder()
                .type(tradeMovementType)
                .ammount(ammount)
                .serialEntry(entryModel)
                .value(entryModel.getClosing() * ammount) //OBTAINS VALUR FROM ENTRY
                .build();

        if(tradeMovementType == TradeMovementTypeEnum.EXIT)
        {

            TradeMovementModel previousTradeMovementModel = this.tradeMovementRepository.findAll(
                (root, query, cb) -> {
                    query.orderBy( cb.desc( root.get("serialEntry").get("time") ) );
                    return cb.equal( root.get("type"), TradeMovementTypeEnum.ENTRY );
                }, Pageable.ofSize(1) ).getContent().get(0);

            movementModel.setRelatedMovement(previousTradeMovementModel);
            movementModel.setProfit(movementModel.getValue() - previousTradeMovementModel.getValue());

            this.tradeMovementRepository.save(movementModel);

            previousTradeMovementModel.setRelatedMovement(movementModel);

            this.tradeMovementRepository.save(previousTradeMovementModel);

        }
        else
        {
            this.tradeMovementRepository.save(movementModel);
        }
    }

    public void put( UUID seriesId, UUID entryId, ResultSerialEntryDto entry )
    {

        TradeMovementTypeEnum tradeMovementType = entry.getTradeMovementType();
        if( Objects.nonNull(tradeMovementType) )
        {
            SerialEntryModel currentEntry = this.entriesRepository.getById(entryId);
            this.putTradeMovementOnEntry(entry, currentEntry, tradeMovementType);
        }
    }

    public ResultSerialEntryDto getEntry( UUID seriesId, UUID entryId )
    {
        //TODO: SHOULD FILTER BY BOT TO?
        return new ResultSerialEntryDto( this.entriesViewRepository.getById(entryId) );
    }

    public SerialEntryModel getLastEntryFromSeries(BotModel bot) {

        return this.entriesRepository.findTopByBotOrderByTimeDesc(bot);

    }

    /*
    public TradeMovementModel getLastTradeEntryMovement(BotModel botModel) {

        return this.tradeMovementRepository.findAll( (root, query, cb) -> {
            return null;
        }, Pageable.ofSize(1)).getContent().get(0);

    }
     */

    public List<TradeMovementModel> getOpenendTradesOnBot(UUID id) {

        BotModel botModel = this.botService.get(id);

        return this.tradeMovementRepository.findAll( (root, query, cb) ->
            cb.and(
                    cb.equal(root.get("serialEntry").get("bot"), botModel),
                    cb.equal(root.get("type"), TradeMovementTypeEnum.ENTRY),
                    cb.isNull(root.get("relatedMovement"))
            )
        );

    }
}
