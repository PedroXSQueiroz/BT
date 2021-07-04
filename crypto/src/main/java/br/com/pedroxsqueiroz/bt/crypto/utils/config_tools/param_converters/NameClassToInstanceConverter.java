package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToIdentifyClassesOnConverterException;
import br.com.pedroxsqueiroz.bt.crypto.services.Bot;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.InjectInConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;
import io.github.classgraph.ClassGraph;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class NameClassToInstanceConverter<B> implements ParamConverter<String, B> {

    @Autowired(required = false)
    public void setBeansOfBaseClass(Set<B> beansOfBaseClass )
    {
        this.beansOfBaseClass = beansOfBaseClass;
    }

    private Set<B> beansOfBaseClass;

    @Autowired
    private ApplicationContext context;

    @Autowired
    public void setParamUtils( ConfigurableParamsUtils paramsUtils )
    {
        this.paramUtils = paramsUtils;
    }

    private ConfigurableParamsUtils paramUtils;

    @Override
    public B convert(String source) {

        if(Objects.nonNull(this.beansOfBaseClass))
        {
            Optional<B> injectedBeanFound = this.beansOfBaseClass
                    .stream()
                    .filter(getter -> {

                        String[] beanNamesForType = this.context.getBeanNamesForType(getter.getClass());

                        Optional<String> nameFound = Arrays
                                .asList(beanNamesForType)
                                .stream()
                                .filter(beanName -> beanName.contentEquals(source))
                                .findFirst();

                        return nameFound.isPresent();

                    }).findFirst();

            if(injectedBeanFound.isPresent())
            {
                return injectedBeanFound.get();
            }
        }

        /*
        Optional<? extends Class<?>> destinyClass = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan()
                .getClassesWithAnnotation(InjectInConfigParam.class.getName())
                .stream()
                .filter(clazz ->
                        {
                            String alias = (String) clazz
                                    .getAnnotationInfo(InjectInConfigParam.class.getName())
                                    .getParameterValues()
                                    .get("alias")
                                    .getValue();

                            return alias.contentEquals(source)
                                    && (
                                        clazz.extendsSuperclass(convertTo().getName())
                                        || clazz.implementsInterface(convertTo().getName()
                                    )
                            );
                        }
                ).map(clazzInfo -> {

                    Class<?> foundedClass = null;

                    try {
                        foundedClass = ClassUtils.getClass(clazzInfo.getName());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    return foundedClass;

                })
                .filter(Objects::nonNull)
                .findAny();

         */

        Class<? extends B> destinyClass = this.paramUtils.resolveConcreteClassOfParam(source, this.convertTo());

        if(Objects.nonNull(destinyClass))
        {

            try {

                return destinyClass.getConstructor().newInstance();

            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

        }

        return null;
    }
}
