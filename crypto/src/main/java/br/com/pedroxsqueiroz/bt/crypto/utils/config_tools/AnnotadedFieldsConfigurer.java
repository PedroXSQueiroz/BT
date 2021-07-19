package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools;

import br.com.pedroxsqueiroz.bt.crypto.exceptions.ConfigParamNotFoundException;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnnotadedFieldsConfigurer<T extends Configurable> extends Configurable{

    final private T target;

    public AnnotadedFieldsConfigurer(T target)
    {
        this.target = target;
    }

    public void resolveInverseDependecies( Configurable parent, Configurable inner )
    {
        FieldUtils
                .getFieldsListWithAnnotation( inner.getClass(), ConfigParam.class )
                .stream()
                .filter( field -> field.getAnnotation( ConfigParam.class ).getFromParent() )
                .forEach( field -> {

                    if( Configurable.class.isAssignableFrom( field.getType() ) )
                    {

                        try {
                            resolveInverseDependecies( inner, (Configurable) field.get(inner)); ;
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
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
                            e.printStackTrace();
                        }

                    }

                });
    }

    @Override
    public void config(Map<String, Object> configParams) {

        FieldUtils
                .getFieldsListWithAnnotation( this.target.getClass(), ConfigParam.class )
                .stream()
                .sorted( ( current, comparing)  -> {

                    Integer comparingPriority = current.getAnnotation(ConfigParam.class).priority();
                    Integer currentPriority = comparing.getAnnotation(ConfigParam.class).priority();

                    return currentPriority.compareTo(comparingPriority);
                })
                .forEach( configField -> {

                    ConfigParam annotation = configField.getAnnotation(ConfigParam.class);

                    String configFieldName = annotation.name();

                    if( configParams.containsKey(configFieldName) )
                    {

                        try {


                            if( !annotation.getFromParent() )
                            {
                                Object value = configParams.get(configFieldName);

                                configField.set( this.target,
                                        annotation.getFromParent() ?
                                            this.target.getParent().getConfigParamValue(configFieldName) :
                                            value
                                );
                            }


                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (ConfigParamNotFoundException e) {
                            e.printStackTrace();
                        }

                    }

                });

        this.target.getCurrentConfiguration()
                .values()
                .stream()
                .filter( value -> value instanceof Configurable )
                .forEach( value -> this.resolveInverseDependecies( this.target, (Configurable) value ) );

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

    @Override
    public void setParent(Configurable configurable)
    {
         this.target.parent = configurable;
         configurable.addChild(this.target);
    }

    @Override
    public Configurable getParent()
    {
        return this.target.parent;
    }
}
