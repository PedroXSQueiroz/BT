package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools;


import br.com.pedroxsqueiroz.bt.crypto.dtos.ConfigurableDto;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters.ParamsToConfigurableInstanceConverter;
import io.github.classgraph.ClassGraph;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class ConfigurableParamsUtils {

    private static final Logger LOGGER = Logger.getLogger( ConfigurableParamsUtils.class.getName() );
    
    @NotNull
    private Map<String, Field> getParamsToFields(Class<? extends Configurable> configurableClass) {
        return FieldUtils
	                .getFieldsListWithAnnotation(configurableClass, ConfigParam.class)
	                .stream()
	                .collect(Collectors.toMap(
	                        annotatedField -> annotatedField.getAnnotation(ConfigParam.class).name(),
	                        annotatedField -> annotatedField
	                ));

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
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        configEntry -> {
	                        String currentConfigParamName = configEntry.getKey();
	
	                        //OBTAINS THE RAW VALUES RECEIVED TO PARAMETER FIELD
	                        Object rawValue = rawValueMap.get(currentConfigParamName);
	
	                        //OBTAINS TYPE OF PARAMETER FIELD
	                        Class<?> paramType = configEntry.getValue();
	
	                        if	( 
	                        		Configurable.class.isAssignableFrom(paramType) &&
	                        		Map.class.isAssignableFrom(rawValue.getClass())
                        		)
	                        {
	                        	try 
	                        	{
	                        		return buildConfigurableDto(rawValue, (Class<Configurable>) paramType);
	                        	} catch (IllegalAccessException | InvocationTargetException | InstantiationException | IllegalArgumentException | SecurityException e) {
	    							LOGGER.log(Level.SEVERE, e.getMessage());
	    						}
	                        }
	
	                        if( List.class.isAssignableFrom(paramType) )
	                        {
	                            List<Object> resolvedListValues = new ArrayList<Object>();
	                            Class<?> rawValueClass = rawValue.getClass();
	
	                            if( rawValueClass.isArray() || List.class.isAssignableFrom(rawValueClass)){
	
	                                Object[] rawValueIterator = rawValueClass.isArray() ?
	                                                                ( Object[] ) rawValue:
	                                                                ( (List<Object>) rawValue ).toArray();
	
	                                Type listGenericType = paramsToFields.get(currentConfigParamName).getGenericType();
	                                Class<?> listParamType = (Class<?>) ((ParameterizedType) listGenericType).getActualTypeArguments()[0];
	                                boolean listParamTypeIsConfigurable = Configurable.class.isAssignableFrom(listParamType);
	
	                                for( Object currentRawValue : rawValueIterator )
	                                {
	
	                                    if( listParamTypeIsConfigurable &&
	                                            Map.class.isAssignableFrom(currentRawValue.getClass()))
	                                    {
	
	                                        try 
	                                        {
	                                        	resolvedListValues.add( 
	                                        			buildConfigurableDto(
	                                        					currentRawValue, 
	                                        					(Class<Configurable>) listParamType)
	                                        			);
	                                        
	                                        } catch (IllegalAccessException | InvocationTargetException
	    											| InstantiationException e) {
	    										LOGGER.log(Level.SEVERE, e.getMessage());
	    									}
	                                    }
	                                    else
	                                    {
	                                    	resolvedListValues.add(
	                                    			resolveParam(
	                                    					paramsToFields.get(currentConfigParamName),
	                                    					listParamType,
	                                    					currentRawValue,
	                                    					configurable
                                    					)
	                                    			);
	                                    	
	                                    }
	
	
	                                }
	
	                                return resolvedListValues;
	                            }
	
	
	                        }
	
	                        return resolveParam(
	                                paramsToFields.get(currentConfigParamName),
	                                paramType,
	                                rawValue,
	                                configurable);                        
                        }
                ));

    }

	private ConfigurableDto buildConfigurableDto(Object rawValue, Class<? extends Configurable> paramType)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {
		ConfigurableDto dto = new ConfigurableDto();
		BeanUtils.populate(dto, (Map<String, Object>) rawValue );
		
		Map<String, Object> innerParams = dto.getParams();
		
		Object sampleInstance = paramType.getConstructors()[0].newInstance();
		Map<String, Object> extractedInnerParams = this.extractConfigParamRawValuesMap(innerParams, (Configurable) sampleInstance);
		dto.setParams(extractedInnerParams);
		return dto;
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
            return ConvertUtils.convert(rawValue, paramType);
        }

        //IMPOSSIBLE TO DEFINE THE CONVERTER TO USE
        //FIXME: LOG THIS
        if(compatibleConverters.size() > 1)
        {
            return null;
        }

        ParamConverter converter = compatibleConverters.get(0);

        //FIXME: PROBABLY THIS SHOUD BE MOVED TO TREE CONSTRUCTION
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
            return Collections.emptyList();
        }

        //TRY FIND THE COMPATIBLE CONVERTER TO RECEIVED AND THE FIELD
        List< Class<? extends ParamConverter> > converters = Arrays.asList( converterAnnotation.converters() );

        if(Objects.isNull(converters))
        {
            return Collections.emptyList();
        }

        return converters
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
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    	LOGGER.info(e.getMessage());
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .filter(
                        converter -> {
                            Class convertFrom = converter.convertFrom();
                            return  rawValue.getClass().equals(convertFrom);
                        }
                )
                .collect(Collectors.toList());
    }

    public <T> Class<? extends T>  resolveConcreteClassOfParam(String alias, Class<T> superClass)
    {

        Optional<?> resultantClass = new ClassGraph()
                    .enableClassInfo()
                    .enableAnnotationInfo()
                    .scan()
                    .getClassesWithAnnotation(InjectInConfigParam.class.getName())
                    .stream()
                    .filter( clazz -> {
	                                
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
                	
                    }).map(clazzInfo -> {

                        Class<? extends T> foundedClass = null;

                        try {
                            foundedClass = (Class<? extends T>) ClassUtils.getClass(clazzInfo.getName());
                        } catch (ClassNotFoundException e) {
                            LOGGER.info(e.getMessage());
                        }

                        return foundedClass;
                    })
                    .filter(Objects::nonNull)
                    .findAny();
		
        if( resultantClass.isPresent() ) 
        {
        	return (Class<? extends T>) resultantClass.get();
        	
        }
        
        return null;

    }
    
    public static void resolveInverseDependecies( Configurable parent, Configurable inner )
    {
        FieldUtils
                .getFieldsListWithAnnotation( inner.getClass(), ConfigParam.class )
                .forEach( field -> {

                    if( Configurable.class.isAssignableFrom( field.getType() ) )
                    {

                        try {
                            resolveInverseDependecies( inner, (Configurable) field.get(inner));
                        } catch (IllegalAccessException e) {
                            LOGGER.log(Level.WARNING, e.getMessage());
                        }

                    }

                    String fieldName = field.getAnnotation( ConfigParam.class ).name();

                    Optional<Field> parentFieldFound = FieldUtils
                            .getFieldsListWithAnnotation(parent.getClass(), ConfigParam.class)
                            .stream()
                            .filter(parentField -> parentField.getAnnotation(ConfigParam.class).name().contentEquals(fieldName))
                            .findAny();

                    if(parentFieldFound.isPresent())
                    {
                        try {
                            Field parentField = parentFieldFound.get();
                            Object parentFieldValue = parentField.get(parent);
                            field.set( inner, parentFieldValue );

                        } catch (IllegalAccessException e) {
                        	LOGGER.log(Level.WARNING, e.getMessage());
                        }

                    }

                });
    }
}
