package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.ResultSerialEntryDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SeriesService {

    private final static Map<UUID, Map< UUID, ResultSerialEntryDto>> SERIES = new HashMap< UUID, Map< UUID, ResultSerialEntryDto> >();

    public List<ResultSerialEntryDto> getResultSeries(UUID id)
    {
        ArrayList<ResultSerialEntryDto> series = new ArrayList<>(SERIES.get(id).values());
        series.sort(Comparable::compareTo);
        return series;
    }

    public UUID addEntryToSeries(UUID id, ResultSerialEntryDto entry)
    {
        Map<UUID, ResultSerialEntryDto> currentSeries = SERIES.get(id);

        if(Objects.isNull(currentSeries))
        {
            currentSeries = new HashMap<UUID, ResultSerialEntryDto>();
            SERIES.put(id, currentSeries);
        }

        UUID newId = UUID.randomUUID();
        currentSeries.put(newId, entry);

        return newId;
    }

    public void put( UUID seriesId, UUID entryId, ResultSerialEntryDto entry )
    {
        SERIES.get(seriesId).put(entryId, entry);
    }

    public ResultSerialEntryDto getEntry( UUID seriesId, UUID entryId )
    {
        Map<UUID, ResultSerialEntryDto> series = SERIES.get(seriesId);
        ResultSerialEntryDto entry = series.get(entryId);
        return entry;
    }

}