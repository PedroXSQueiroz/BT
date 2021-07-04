package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.annotated_fields_configurer;

import br.com.pedroxsqueiroz.bt.crypto.dtos.ConfigurableDto;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.services.*;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.ConfigurableDtoToMarketFacadeConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.ConfigurableDtoToTradeAlgorithmConverter;
import lombok.SneakyThrows;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SpringBootTest
public class BotAnnotatedFieldsConfigurerTest
        extends AbstractAnnotatedFieldsConfigTest
{

    //@BeforeAll
    public void setup()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Override
    public Configurable getConfigurable() {
        return new Bot();
    }

    @Override
    public Map<String, Object> getRawParams() {
        return new HashMap<String, Object>(){{
            put(
                "algorithm", ConfigurableDto.builder()
                                .name("dummy")
                                .params(new HashMap<>(){{
                                    put("BTCUSDC", new StockType());
                                    put("EMA", 4);
                                }})
                                .build()
            );
            put(
                "market", ConfigurableDto.builder()
                                .name("dummy")
                                .params(new HashMap<>(){{
                                    put("interval", 1);
                                }})
                                .build()
            );
        }};
    }

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ApplicationContext mockContext;

    @Mock
    private DummyAlgorithm mockAlgorithm;

    @Mock
    private DummyMarketFacade mockMarketFacade;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ConfigurableParamsUtils mockParamsUtils;

    @InjectMocks
    private ConfigurableDtoToTradeAlgorithmConverter algorithmConverter;

    @InjectMocks
    private ConfigurableDtoToMarketFacadeConverter marketConverter;

    @InjectMocks
    private ConfigurableParamsUtils paramsUtils;

    @SneakyThrows
    @Override
    public ConfigurableParamsUtils getParamUtils()
    {

        /*
        Mockito.doReturn(this.mockAlgorithm)
                .when(this.mockContext)
                .getBean("dummy", TradeAlgorithm.class);

        Mockito.doReturn(this.mockMarketFacade)
                .when(this.mockContext)
                .getBean("dummy", MarketFacade.class);
         */

        this.marketConverter.setInjectedBeans(new HashSet<MarketFacade>(){{
            add(mockMarketFacade);
        }});

        this.algorithmConverter.setInjectedBeans(new HashSet<>(){{
            add(mockAlgorithm);
        }});

        Set<ParamConverter> converters = new HashSet<ParamConverter>(){{
            add(algorithmConverter);
            add(marketConverter);
        }};

        this.paramsUtils.setConvertersBeans(converters);

        return this.paramsUtils;
    }

    @Override
    public Map<String, Object> getResolvedParams() {

        Mockito.doCallRealMethod()
                .when(this.mockAlgorithm)
                .config(Mockito.anyMap());

        Mockito.doCallRealMethod()
                .when(this.mockMarketFacade)
                .config(Mockito.anyMap());

        return new HashMap<String, Object>(){{
            put("algorithm", mockAlgorithm);
            put("market", mockMarketFacade);
        }};
    }

}
