package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.converters;

import br.com.pedroxsqueiroz.bt.crypto.services.EntryAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.NameToEntryAmmountGetterConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.HashSet;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NameToEntryAmmountGetterInjectedConverterTest extends AbstractParamConverterTest<String, EntryAmmountGetter>{

    @Mock
    private EntryAmmountGetter getter;

    @Mock
    private ApplicationContext context;

    @Autowired
    private ConfigurableParamsUtils paramsUtils;

    @InjectMocks
    private NameToEntryAmmountGetterConverter converter;

    @BeforeAll
    public void init(){

        Mockito
            .doReturn(new String[]{"dummy"})
            .when(this.context)
            .getBeanNamesForType( this.getter.getClass() );

        this.converter.setParamUtils(this.paramsUtils);

        this.converter.setBeansOfBaseClass( new HashSet<EntryAmmountGetter>() {{
            add(getter);
        }} );

    }

    @Override
    public ParamConverter<String, EntryAmmountGetter> getConverter() {
        return this.converter;
    }

    @Override
    public String getSource() {
        return "dummy";
    }

    @Override
    public EntryAmmountGetter getExpectedResult() {
        return this.getter;
    }
}
