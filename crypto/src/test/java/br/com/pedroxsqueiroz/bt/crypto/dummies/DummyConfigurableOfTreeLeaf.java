package br.com.pedroxsqueiroz.bt.crypto.dummies;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.Data;
import lombok.experimental.Delegate;

@Data
public class DummyConfigurableOfTreeLeaf extends Configurable {
	
	@ConfigParam(name = "nodeParam", getFromParent = true )
	public Integer dataFromNode;
	
	@Delegate(types = Configurable.class)
	private AnnotadedFieldsConfigurer<DummyConfigurableOfTreeLeaf> configurer = new AnnotadedFieldsConfigurer(this);
	
}
