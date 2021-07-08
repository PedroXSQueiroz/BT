package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.converters;

import br.com.pedroxsqueiroz.bt.crypto.services.DummyExitAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.services.ExitAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.NameToExitAmmountGetterConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NameToExitAmmountGetterNotInjectedConverterTest extends AbstractParamConverterTest<String, ExitAmmountGetter> {

    @Autowired
    private ConfigurableParamsUtils paramsUtils;

    @InjectMocks
    private NameToExitAmmountGetterConverter converter;

    @Override
    public ParamConverter<String, ExitAmmountGetter> getConverter() {
        return this.converter;
    }

    @BeforeAll
    public void init()
    {
        this.converter.setParamUtils(this.paramsUtils);
    }

    @Override
    public String getSource() {
        return "dummy";
    }

    @Override
    public ExitAmmountGetter getExpectedResult() {
        return new DummyExitAmmountGetter();
    }

    @Override
    public void validateResult(ExitAmmountGetter expected, ExitAmmountGetter result)
    {
        assertEquals( expected.getClass(), result.getClass() );
    }

}
