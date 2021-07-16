package br.com.pedroxsqueiroz.bt.crypto.services.seriesUpdateListenerCallback;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryModel;
import br.com.pedroxsqueiroz.bt.crypto.repositories.SerialEntryRepository;
import br.com.pedroxsqueiroz.bt.crypto.services.SeriesUpdateListenerCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("DBSeriesUpdate")
public class DBPersistenceSeriesUpdateListenerCallback implements SeriesUpdateListenerCallback {

    @Autowired
    private SerialEntryRepository serialEntryRepository;

    @Override
    public void callback(List<SerialEntry> entries) {

        //FIXME: REFACTOR USING MODELMAPPER
        List<SerialEntryModel> models = entries.stream()
                .map(dto ->
                    SerialEntryModel
                            .builder()
                            .min(dto.getMin())
                            .max(dto.getMax())
                            .opening(dto.getOpening())
                            .closing(dto.getClosing())
                            .variance(dto.getVariance())
                            .time(dto.getDate().toInstant())
                            .build()
                ).collect(Collectors.toList());

        this.serialEntryRepository.saveAll(models);

    }
}
