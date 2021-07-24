package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;


import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class StringToTemporalUnitConverter implements ParamConverter<String, TemporalUnit> {


    @Override
    public TemporalUnit convert(String source) {

        switch (source)
        {
            case "s":
                return ChronoUnit.SECONDS;
            case "m":
                return ChronoUnit.MINUTES;
            case "h":
                return ChronoUnit.HOURS;
            case "d":
                return ChronoUnit.DAYS;
        }

        return null;
    }

    @Override
    public Class<TemporalUnit> convertTo() {
        return TemporalUnit.class;
    }

    @Override
    public Class<String> convertFrom() {
        return String.class;
    }
}
