package br.com.pedroxsqueiroz.bt.crypto.dummies;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import lombok.Data;
import lombok.experimental.Delegate;

@Data
public class DummyConfigurableOfTreeNode extends Configurable{
	
	@ConfigParam(name = "nodeParam")
	public Integer nodeParam;
	
	@ConfigParam(name = "rootParam", getFromParent = true)
	public Integer dataFromRoot;
	
	@ConfigParam(name = "leaf")
	public DummyConfigurableOfTreeLeaf leaf;
	
	@Delegate(types = Configurable.class)
	private AnnotadedFieldsConfigurer<DummyConfigurableOfTreeNode> configurer = new AnnotadedFieldsConfigurer(this);
	
}
