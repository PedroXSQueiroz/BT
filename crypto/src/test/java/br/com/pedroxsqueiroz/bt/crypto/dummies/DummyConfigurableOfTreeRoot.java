package br.com.pedroxsqueiroz.bt.crypto.dummies;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.Data;
import lombok.experimental.Delegate;

@Data
public class DummyConfigurableOfTreeRoot extends Configurable {
	
	@ConfigParam(name = "rootParam")
	public Integer rootParam;
	
	@ConfigParam(name = "node")
	public DummyConfigurableOfTreeNode node;
	
	@Delegate(types = Configurable.class)
	private AnnotadedFieldsConfigurer<DummyConfigurableOfTreeRoot> configurer = new AnnotadedFieldsConfigurer(this);
	
}
