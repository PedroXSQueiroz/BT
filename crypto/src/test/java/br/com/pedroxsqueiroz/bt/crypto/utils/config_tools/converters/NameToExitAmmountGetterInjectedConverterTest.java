package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.converters;

import br.com.pedroxsqueiroz.bt.crypto.services.ExitAmmountGetter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.NameToExitAmmountGetterConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.HashSet;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NameToExitAmmountGetterInjectedConverterTest extends AbstractParamConverterTest<String, ExitAmmountGetter>{

    @Mock
    private ExitAmmountGetter getter;

    @Mock
    private ApplicationContext context;

    @InjectMocks
    private NameToExitAmmountGetterConverter converter;

    @BeforeAll
    public void init(){

        Mockito
            .doReturn(new String[]{"dummy"})
            .when(this.context)
            .getBeanNamesForType( this.getter.getClass() );

        this.converter.setBeansOfBaseClass( new HashSet<ExitAmmountGetter>() {{
            add(getter);
        }} );

    }

    @Override
    public ParamConverter<String, ExitAmmountGetter> getConverter() {
        return this.converter;
    }

    @Override
    public String getSource() {
        return "dummy";
    }

    @Override
    public ExitAmmountGetter getExpectedResult() {
        return this.getter;
    }
}
