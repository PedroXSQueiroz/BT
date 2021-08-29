package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.converters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.ConfigurableDto;
import br.com.pedroxsqueiroz.bt.crypto.services.DummyMarketFacade;
import br.com.pedroxsqueiroz.bt.crypto.services.MarketFacade;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.ConfigurableDtoToMarketFacadeConverter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.HashSet;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParamsToMarketFacadeConverterTest extends AbstractParamConverterTest<ConfigurableDto, MarketFacade> {

    @Spy
    private DummyMarketFacade mockMarketFacade;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ApplicationContext mockContext;

    @Spy
    private ConfigurableParamsUtils mockParamsUtils;

    @InjectMocks
    private ConfigurableDtoToMarketFacadeConverter converter;

    @BeforeAll
    public void setup()
    {
        /*
        Mockito.doReturn(this.mockMarketFacade)
                .when(this.mockContext)
                .getBean("dummy", MarketFacade.class );
        */

        HashMap<String, Object> dummyExtractionParamsResult = new HashMap<>() {{
            put("key", "666");
        }};

        this.converter.setInjectedBeans(new HashSet<MarketFacade>() {{
            add(mockMarketFacade);
        }});

        Mockito.doCallRealMethod()
                .when(this.mockMarketFacade)
                .config(Mockito.any());
        
        Mockito.doReturn( new String[] {"dummy"} )
        		.when(this.mockContext)
        		.getBeanNamesForType( mockMarketFacade.getClass() );
        
        Mockito.doReturn(dummyExtractionParamsResult)
	        .when(this.mockParamsUtils)
	        .extractConfigParamRawValuesMap(Mockito.anyMap(), Mockito.any());

    }

    @Override
    public ParamConverter getConverter() {
        return this.converter;
    }

    @Override
    public ConfigurableDto getSource() {
        return ConfigurableDto.builder()
                .name("dummy")
                .params(new HashMap<>() {{
                    put("key", "666") ;
                }})
                .build();
    }

    @Override
    public MarketFacade getExpectedResult() {
        return this.mockMarketFacade;
    }
}
