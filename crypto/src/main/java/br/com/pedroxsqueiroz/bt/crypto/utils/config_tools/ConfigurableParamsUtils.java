package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools;


import br.com.pedroxsqueiroz.bt.crypto.dtos.ConfigurableDto;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.ParamsToConfigurableInstanceConverter;
import com.google.common.collect.Lists;
import io.github.classgraph.ClassGraph;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ConfigurableParamsUtils {

    @NotNull
    private Map<String, Field> getParamsToFields(Class<? extends Configurable> configurableClass) {
        Map<String, Field> paramsToFields = FieldUtils
                                                .getFieldsListWithAnnotation(configurableClass, ConfigParam.class)
                                                .stream()
                                                .collect(Collectors.toMap(
                                                        annotatedField -> annotatedField.getAnnotation(ConfigParam.class).name(),
                                                        annotatedField -> annotatedField
                                                ));
        return paramsToFields;
    }

    public Map<String, Object> extractConfigParamRawValuesMap(Map<String, Object> rawValueMap, Configurable configurable )
    {
        Class<? extends Configurable> configurableClass = configurable.getClass();

        Map<String, Class<?>> configParamsNameAndType = configurable.getConfigParamsNameAndType();

        Map<String, Field> paramsToFields = getParamsToFields(configurableClass);

        return configParamsNameAndType
                .entrySet()
                .stream()
                .filter( entry -> rawValueMap.containsKey( entry.getKey() ) )
                .map( configEntry -> {

                    String currentConfigParamName = configEntry.getKey();

                    //OBTAINS THE RAW VALUES RECEIVED TO PARAMETER FIELD
                    Object rawValue = rawValueMap.get(currentConfigParamName);

                    //OBTAINS TYPE OF PARAMETER FIELD
                    Class<?> paramType = configEntry.getValue();

                    if( Configurable.class.isAssignableFrom(paramType) &&
                            Map.class.isAssignableFrom(rawValue.getClass()))
                    {
                        Map<String, Object> configurableDtoMap = (Map<String, Object>) rawValue;
                        String configName = (String) configurableDtoMap.get("name");
                        Map configInnerParams = (Map) configurableDtoMap.get("params");

                        rawValue = new ConfigurableDto( configName, configInnerParams );
                    }

                    Object paramResolved = resolveParam(
                            paramsToFields.get(currentConfigParamName),
                            paramType,
                            rawValue,
                            configurable);

                    return new AbstractMap.SimpleEntry<>(currentConfigParamName, paramResolved);

                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

    }

    public void resolveConfigurableTree(Configurable configurable, Map<String, Object> paramsWithConfigurables)
    {

        FieldUtils
                .getFieldsListWithAnnotation( configurable.getClass(), ConfigParam.class )
                .stream()
                .filter( field -> Configurable.class.isAssignableFrom( field.getType() ) )
                .forEach( field -> {

                    String name = field.getAnnotation(ConfigParam.class).name();

                    Configurable innerConfigurable = (Configurable) paramsWithConfigurables.get(name);

                    if( Objects.nonNull( innerConfigurable ) )
                    {
                        innerConfigurable.setParent(configurable);
                        resolveConfigurableTree( configurable, innerConfigurable.getCurrentConfiguration() );
                    }


                });

    }

    @Autowired(required = false)
    public void setConvertersBeans(Set<ParamConverter> convertersBeans)
    {
        this.convertersBeans = convertersBeans;
    }

    Set<ParamConverter> convertersBeans;

    private Object resolveParam(
            Field paramField,
            Class<?> paramType,
            Object rawValue,
            Configurable configurable) {

        List<? extends ParamConverter> compatibleConverters = getCompatibleConverters(paramField, rawValue);


        if(compatibleConverters.isEmpty())
        {
            return paramType.cast(rawValue);
        }

        //IPOSSIBLE TO DEFINE THE CONVERTER TO USE
        //FIXME: LOG THIS
        if(compatibleConverters.size() > 1)
        {
            return null;
        }

        ParamConverter converter = compatibleConverters.get(0);

        if( ParamsToConfigurableInstanceConverter.class.isAssignableFrom( converter.getClass() ) )
        {
            ParamsToConfigurableInstanceConverter configurableConverter = (ParamsToConfigurableInstanceConverter) converter;
            configurableConverter.setParent(configurable);
        }

        return converter.convert(rawValue);

    }

    @Nullable
    private List<? extends ParamConverter> getCompatibleConverters(Field paramField, Object rawValue) {
        ConfigParamConverter converterAnnotation = paramField.getAnnotation(ConfigParamConverter.class);

        if(Objects.isNull(converterAnnotation))
        {
            return Collections.EMPTY_LIST;
        }

        //TRY FIND THE COMPATIBLE CONVERTER TO RECEIVED AND THE FIELD
        List< Class<? extends ParamConverter> > converters = Arrays.asList( converterAnnotation.converters() );

        if(Objects.isNull(converters))
        {
            return Collections.EMPTY_LIST;
        }

        List<? extends ParamConverter> compatibleConverters = converters
                .stream()
                .map(converterClass -> {

                    if( Objects.nonNull(this.convertersBeans) )
                    {
                        Optional<ParamConverter> previousBeanInstance = this.convertersBeans
                                                                                .stream()
                                                                                .filter(converterBean -> converterBean.getClass().equals(converterClass))
                                                                                .findAny();

                        if(previousBeanInstance.isPresent())
                        {
                           return previousBeanInstance.get();
                        }

                    }

                    try {
                        return converterClass.getConstructor().newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .filter(
                        converter -> {

                            Class convertFrom = converter.convertFrom();
                            Class convertTo = converter.convertTo();

                            return  convertFrom.equals(rawValue.getClass())
                                &&  convertTo.equals(paramField.getType());

                        }
                ).collect(Collectors.toList());
        return compatibleConverters;
    }

    public <T> Class<? extends T>  resolveConcreteClassOfParam(String alias, Class<T> superClass)
    {

        return new ClassGraph()
                    .enableClassInfo()
                    .enableAnnotationInfo()
                    .scan()
                    .getClassesWithAnnotation(InjectInConfigParam.class.getName())
                    .stream()
                    .filter(clazz ->
                            {
                                String comparingAlias = (String) clazz
                                        .getAnnotationInfo(InjectInConfigParam.class.getName())
                                        .getParameterValues()
                                        .get("alias")
                                        .getValue();

                                return comparingAlias.contentEquals(alias)
                                        && (
                                        clazz.extendsSuperclass(superClass.getName())
                                                || clazz.implementsInterface(superClass.getName()
                                        )
                                );
                            }
                    ).map(clazzInfo -> {

                        Class<? extends T> foundedClass = null;

                        try {
                            foundedClass = (Class<? extends T>) ClassUtils.getClass(clazzInfo.getName());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        return foundedClass;

                    })
                    .filter(Objects::nonNull)
                    .findAny()
                    .get();


    }
}
