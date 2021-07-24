package br.com.pedroxsqueiroz.bt.crypto.services.seriesUpdateListenerCallback;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.models.SerialEntryModel;
import br.com.pedroxsqueiroz.bt.crypto.repositories.SerialEntryRepository;
import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.services.SeriesService;
import br.com.pedroxsqueiroz.bt.crypto.services.SeriesUpdateListenerCallback;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("dbUpdateSeriesCallback")
public class DBPersistenceSeriesUpdateListenerCallback extends SeriesUpdateListenerCallback  {

    @Autowired
    private SerialEntryRepository serialEntryRepository;

    @Autowired
    private SeriesService seriesService;

    @Override
    public void callback(List<SerialEntry> entries) {

        Bot bot = (Bot) this.getParent();

        entries.forEach( currentEntry -> this.seriesService.addEntryToSeries( bot.getId(), currentEntry ) );
    }

}
