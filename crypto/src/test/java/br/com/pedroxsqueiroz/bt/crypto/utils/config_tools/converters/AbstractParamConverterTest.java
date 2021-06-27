package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.converters;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractParamConverterTest<S, R> {

    public abstract ParamConverter<S, R> getConverter();

    public abstract S getSource();

    public abstract R getExpectedResult();

    public void validateResult(R expected, R result)
    {
        assertEquals(expected, result);
    }

    @Test
    public void testConversion()
    {

        S source = this.getSource();

        ParamConverter<S, R> converter = this.getConverter();

        R result = converter.convert(source);

        this.validateResult(this.getExpectedResult(), result);

    }

}
