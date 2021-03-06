package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.dtos.ConfigurableDto;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ConfigParamNotFoundException;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Modifier;
import java.util.*;

public abstract class ParamsToConfigurableInstanceConverter<R extends Configurable> implements ParamConverter<ConfigurableDto, R> {

    protected abstract Class<? extends Configurable> getConfigurableClass();

    //@Autowired
    //public void setContext(ApplicationContext context)
    //{
    //    this.context = context;
    //}

    @Autowired(required = false)
    public void setInjectedBeans(Set<R> injectedBeans)
    {
        this.injectedBeans = injectedBeans;
    }

    @Autowired
    private ApplicationContext context;
    
    private Set<R> injectedBeans;

    //private ApplicationContext context;

    @Autowired(required = false)
    public void setParamsUtils(ConfigurableParamsUtils paramsUtils)
    {
        this.paramsUtils = paramsUtils;
    }

    private ConfigurableParamsUtils paramsUtils;

    @SneakyThrows
    @Override
    public R convert(ConfigurableDto source) {

        Class<R> destinyClass = this.convertTo();

        Optional<R> foundedConfigBean = Objects.nonNull(this.injectedBeans) ?
                                                this.injectedBeans.stream()
                                                        .filter( bean ->

                                                            //CHECK CLASS OF CONFIGURABLE BEAN
                                                            destinyClass.isInstance(bean)

                                                            // FIND NAME OF CONFIBURABLE BEAN
                                                            && Arrays.stream(
                                                                this.getConfigurableClassName( bean.getClass() )
                                                            ).filter( beanName ->
                                                                    beanName.contentEquals( source.getName() )
                                                            ).findAny().isPresent()

                                                        ).findAny()
                                                :null;

        R configBean = null;

        if( Objects.nonNull(foundedConfigBean) && foundedConfigBean.isPresent())
        {
            configBean =foundedConfigBean.get();
        }
        else if(        Modifier.isAbstract( destinyClass.getModifiers() )
                    ||  Modifier.isInterface( destinyClass.getModifiers() ))
        {
            Class<? extends R> destinyConcreteClass = this.paramsUtils.resolveConcreteClassOfParam(source.getName(), this.convertTo());

            if(Objects.nonNull(destinyConcreteClass))
            {
                configBean = destinyConcreteClass.getConstructor().newInstance();
            }

        }else
        {
            configBean = destinyClass.getConstructor().newInstance();
        }

        if(Objects.nonNull(configBean))
        {
            Map<String, Object> resolvedParams = this.paramsUtils.extractConfigParamRawValuesMap(source.getParams(), configBean);
            configBean.config(resolvedParams);
        }

        return configBean;

    }

    private String[] getConfigurableClassName(Class<? extends Configurable> aClass) {

        return this.context.getBeanNamesForType(aClass);

    }

    @Override
    public Class<ConfigurableDto> convertFrom() {
        return ConfigurableDto.class;
    }

    @Override
    public Class<R> convertTo() {
        return (Class<R>) this.getConfigurableClass();
    }

    private Configurable parent;

    public void setParent( Configurable parent )
    {
        this.parent = parent;
    }

    public <R1> R1 getFromParent( String propertyFromSource ) throws IllegalAccessException, ConfigParamNotFoundException {
        return (R1) this.parent.getConfigParamValue(propertyFromSource);
    }
}
