package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools;

import br.com.pedroxsqueiroz.bt.crypto.exceptions.ConfigParamNotFoundException;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AnnotadedFieldsConfigurer<T extends Configurable> extends Configurable{

    private static final Logger LOGGER = Logger.getLogger(AnnotadedFieldsConfigurer.class.getName());
	
	private final T target;

    public AnnotadedFieldsConfigurer(T target)
    {
        this.target = target;
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


                        } catch (IllegalAccessException | ConfigParamNotFoundException e) {
                        	LOGGER.log(Level.WARNING, e.getMessage());
                        } 

                    }

                });

        this.target.getCurrentConfiguration()
                .values()
                .stream()
                .filter( value -> value instanceof Configurable )
                .forEach( value -> ConfigurableParamsUtils.resolveInverseDependecies( this.target, (Configurable) value ) );

    }

    @Override
    public Map<String, Class<?>> getConfigParamsNameAndType() {
        return FieldUtils
                .getFieldsListWithAnnotation( this.target.getClass(), ConfigParam.class )
                .stream()
                .collect(
                    Collectors.toMap(
                        field -> field.getAnnotation(ConfigParam.class).name(),
                        Field::getType
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
                    	LOGGER.log(Level.WARNING, e.getMessage());
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
