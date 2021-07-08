package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.converters;

import br.com.pedroxsqueiroz.bt.crypto.services.DummyEntryAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.services.EntryAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.NameToEntryAmmountGetterConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NameToEntryAmmountGetterNotInjectedConverterTest extends AbstractParamConverterTest<String, EntryAmmountGetter> {

    @Autowired
    private ConfigurableParamsUtils paramsUtils;

    @InjectMocks
    private NameToEntryAmmountGetterConverter getter;

    @Override
    public ParamConverter<String, EntryAmmountGetter> getConverter() {
        return this.getter;
    }

    @BeforeAll
    public void init()
    {
        this.getter.setParamUtils(this.paramsUtils);
    }

    @Override
    public String getSource() {
        return "dummy";
    }

    @Override
    public EntryAmmountGetter getExpectedResult() {
        return new DummyEntryAmmountGetter();
    }

    @Override
    public void validateResult(EntryAmmountGetter expected, EntryAmmountGetter result)
    {
        assertEquals( expected.getClass(), result.getClass() );
    }
}
