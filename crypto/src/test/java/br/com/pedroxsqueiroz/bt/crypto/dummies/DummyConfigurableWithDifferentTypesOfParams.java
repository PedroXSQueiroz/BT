package br.com.pedroxsqueiroz.bt.crypto.dummies;

import java.util.List;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParamConverter;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.experimental.Delegate;


public class DummyConfigurableWithDifferentTypesOfParams extends Configurable{

	@ConfigParam(name = "intParam")
	public int intParam;
	
	@ConfigParam(name = "longParam")
	public long longParam;
	
	@ConfigParam(name = "stringParam")
	public String stringParam;
	
	@ConfigParam(name = "floatParam")
	public float floatParam;
	
	@ConfigParam(name = "doubleParam")
	public double doubleParam;
	
	@ConfigParam(name = "innerConfigurableParam")
	public DummyConfigurableWithDifferentTypesOfParams innerConfigurableParam;
	
	@ConfigParam(name = "innerConfigurableParamsList")
	public List<DummyConfigurableWithDifferentTypesOfParams> innerConfigurableParams;
	
	@ConfigParam(name = "integerList")
	public List<Integer> integersList;
	
	@ConfigParam(name = "doublesList")
	public List<Double> integersDoubles;
	
	
	@Delegate(types = Configurable.class)
	private AnnotadedFieldsConfigurer<DummyConfigurableWithDifferentTypesOfParams> configurer = new AnnotadedFieldsConfigurer(this);
	
}
