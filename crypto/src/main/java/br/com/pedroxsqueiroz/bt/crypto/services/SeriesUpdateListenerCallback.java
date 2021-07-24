package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.experimental.Delegate;

import java.util.EventListener;
import java.util.List;

public abstract class SeriesUpdateListenerCallback extends Configurable {

     public abstract void callback(List<SerialEntry> entries);

     @Delegate(types = Configurable.class)
     public AnnotadedFieldsConfigurer configurer = new AnnotadedFieldsConfigurer(this);
}
