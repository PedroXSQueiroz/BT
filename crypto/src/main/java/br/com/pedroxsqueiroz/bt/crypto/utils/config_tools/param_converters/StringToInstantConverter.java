package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class StringToInstantConverter implements ParamConverter<String, Instant> {

    @Override
    public Instant convert(String source) {

        return LocalDateTime.parse(
                    source,
                    DateTimeFormatter.ISO_DATE_TIME
                )
                .atZone(
                    ZoneId.systemDefault()
                )
                .toInstant();

    }

    @Override
    public Class<Instant> convertTo() {
        return Instant.class;
    }

    @Override
    public Class<String> convertFrom() {
        return String.class;
    }
}
