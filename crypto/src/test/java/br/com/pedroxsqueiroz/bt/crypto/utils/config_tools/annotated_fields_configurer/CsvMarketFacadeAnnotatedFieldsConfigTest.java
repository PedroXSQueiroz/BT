package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.annotated_fields_configurer;

import br.com.pedroxsqueiroz.bt.crypto.services.markets.CsvMarketFacade;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class CsvMarketFacadeAnnotatedFieldsConfigTest
        extends AbstractAnnotatedFieldsConfigTest {

    //@BeforeAll
    public void setup()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Override
    public Configurable getConfigurable() {
        return new CsvMarketFacade();
    }

    @Override
    public Map<String, Object> getRawParams() {

        String dummySeriesFilePath = getClass().getClassLoader().getResource("dummy_series.csv").getPath();

        return new HashMap<String, Object>(){{
            put("source", dummySeriesFilePath);
        }};

    }

    @SneakyThrows
    @Override
    public Map<String, Object> getResolvedParams() {
        String dummySeriesFilePath = getClass().getClassLoader().getResource("dummy_series.csv").getPath();

        FileInputStream dummyInput = new FileInputStream(new File(dummySeriesFilePath));

        return new HashMap<String, Object>(){{
            put("source", dummyInput);
        }};
    }

    @Override
    public void validateResolvedParams(Map<String, Object> expected, Map<String, Object> result) {

        assertTrue( result.containsKey("source"), "Result not contains expected params");

        FileInputStream resultSource = (FileInputStream) result.get("source");

        FileInputStream expectedSource = (FileInputStream) expected.get("source");

        boolean sourcesAreEqual = false;

        try {
            sourcesAreEqual = IOUtils.contentEquals( expectedSource, resultSource );
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue( sourcesAreEqual, "Streams content are not equal or not valid" );
    }
}
