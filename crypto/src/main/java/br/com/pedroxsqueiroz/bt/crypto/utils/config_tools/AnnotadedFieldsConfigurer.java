package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools;

import br.com.pedroxsqueiroz.bt.crypto.exceptions.ConfigParamNotFoundException;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnnotadedFieldsConfigurer<T> implements Configurable{

    final private T target;

    public AnnotadedFieldsConfigurer(T target)
    {
        this.target = target;
    }

    @Override
    public void config(Map<String, Object> configParams) {

        FieldUtils
                .getFieldsListWithAnnotation( this.target.getClass(), ConfigParam.class )
                .forEach( configFields -> {

                    ConfigParam annotation = configFields.getAnnotation(ConfigParam.class);

                    String configFieldName = annotation.name();

                    if( configParams.containsKey(configFieldName) )
                    {

                        try {
                            configFields.set( this.target, configParams.get(configFieldName) );
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }

                });

    }

    @Override
    public Map<String, Class<?>> getConfigParamsNameAndType() {
        return FieldUtils
                .getFieldsListWithAnnotation( this.target.getClass(), ConfigParam.class )
                .stream()
                .collect(
                    Collectors.toMap(
                        field -> field.getAnnotation(ConfigParam.class).name(),
                        field -> field.getType()
                    )
                );
    }

    @Override
    public boolean isConfigured() {

        return  FieldUtils
                .getFieldsListWithAnnotation( this.target.getClass(), ConfigParam.class )
                .stream()
                .map( field -> {

                    try {
                        return Objects.nonNull( field.get(this.target) );
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    return false;
                })
                .reduce(true , (result, current) -> result && current);
    }

    @Override
    public Object getConfigParamValue(String propertyFromSource) throws IllegalAccessException, ConfigParamNotFoundException {

        List<Field> fields = FieldUtils
                .getFieldsListWithAnnotation(this.target.getClass(), ConfigParam.class)
                .stream()
                .filter(field ->
                        field
                            .getAnnotation(ConfigParam.class)
                            .name()
                            .contentEquals(propertyFromSource)
                ).collect(Collectors.toList());

        if(fields.size() == 1)
        {
            Field field = fields.get(0);
            return field.get(this.target);
        }

        throw new ConfigParamNotFoundException();
    }
}
