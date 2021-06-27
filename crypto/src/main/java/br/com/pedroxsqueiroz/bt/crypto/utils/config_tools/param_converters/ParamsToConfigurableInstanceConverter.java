package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.ConfigurableDto;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;

public abstract class ParamsToConfigurableInstanceConverter<R extends Configurable> implements ParamConverter<ConfigurableDto, R> {

    protected abstract Class<? extends Configurable> getConfigurableClass();

    @Autowired
    public void setContext(ApplicationContext context)
    {
        this.context = context;
    }

    private ApplicationContext context;

    @Autowired
    public void setParamsUtils(ConfigurableParamsUtils paramsUtils)
    {
        this.paramsUtils = paramsUtils;
    }

    private ConfigurableParamsUtils paramsUtils;

    @Override
    public R convert(ConfigurableDto source) {

        String algorithmName = source.getName();

        R algorithm = (R) this.context.getBean(algorithmName, this.getConfigurableClass());

        Map<String, Object> resolvedParams = this.paramsUtils.extractConfigParamRawValuesMap(source.getParams(), algorithm);

        algorithm.config(resolvedParams);

        return algorithm;
    }

    @Override
    public Class<ConfigurableDto> convertFrom() {
        return ConfigurableDto.class;
    }

    @Override
    public Class<R> convertTo() {
        return (Class<R>) this.getConfigurableClass();
    }
}
