package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.converters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.ConfigurableDto;
import br.com.pedroxsqueiroz.bt.crypto.services.DummyAlgorithm;
import br.com.pedroxsqueiroz.bt.crypto.services.TradeAlgorithm;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.ConfigurableDtoToTradeAlgorithmConverter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParamsToTradeMethodConverterTest
        extends AbstractParamConverterTest<ConfigurableDto, TradeAlgorithm>
{

    @Spy
    private DummyAlgorithm mockAlgorithm;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ApplicationContext mockContext;

    @Spy
    private ConfigurableParamsUtils mockParamsUtils;

    @InjectMocks
    private ConfigurableDtoToTradeAlgorithmConverter converter;

    @BeforeAll
    public void setup()
    {
        Mockito.doReturn(this.mockAlgorithm)
                .when(this.mockContext)
                .getBean("dummy", TradeAlgorithm.class );

        HashMap<String, Object> dummyExtractionParamsResult = new HashMap<>() {{
            put("stockType", "BTCUSDC");
            put("EMA", 4);
        }};

        Mockito.doReturn(dummyExtractionParamsResult)
                .when(this.mockParamsUtils)
                .extractConfigParamRawValuesMap(Mockito.anyMap(), Mockito.any());

        Mockito.doCallRealMethod()
                .when(this.mockAlgorithm)
                .config(Mockito.any());

    }

    @Override
    public ParamConverter<ConfigurableDto, TradeAlgorithm> getConverter() {
        return this.converter;
    }

   @Override
    public ConfigurableDto getSource() {

        ConfigurableDto configDto = ConfigurableDto.builder()
                .name("dummy")
                .params(new HashMap<>() {{
                    put("stockType", "BTCUSDC");
                    put("EMA", 4);
                }})
                .build();

        return configDto;
    }

    @Override
    public TradeAlgorithm getExpectedResult() {
        return this.mockAlgorithm;
    }
}
