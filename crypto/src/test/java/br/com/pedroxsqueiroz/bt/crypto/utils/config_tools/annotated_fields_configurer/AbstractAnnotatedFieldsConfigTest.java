package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.annotated_fields_configurer;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

//import org.junit.Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractAnnotatedFieldsConfigTest {

    public abstract Configurable getConfigurable();

    public abstract Map<String, Object> getRawParams();

    public abstract Map<String, Object> getResolvedParams();

    public void validateResolvedParams(Map<String, Object> expected, Map<String, Object> resolvedParams)
    {
        expected.forEach( ( configName, resolvedParam ) -> {

            boolean paramSuccefullyReturned = resolvedParams.containsKey(configName);

            assertTrue(paramSuccefullyReturned, String.format("The param %s was not extrated", configName) );

            assertEquals( resolvedParams.get(configName), resolvedParam );

        });
    }

    @Autowired
    private ConfigurableParamsUtils paramsUtils;

    protected ConfigurableParamsUtils getParamUtils()
    {
        return this.paramsUtils;
    }

    @Test
    public void shouldGetAllRequiredConfigFields()
    {
        Configurable configurable = this.getConfigurable();

        Map<String, Class<?>> configParamsNameAndType = configurable.getConfigParamsNameAndType();
        assertTrue( !configParamsNameAndType.isEmpty(), "No exists fields to config" );

        List<Field> annotatedClassFields = FieldUtils.getFieldsListWithAnnotation(configurable.getClass(), ConfigParam.class);
        assertTrue( !annotatedClassFields.isEmpty(), "No exists annotated fields for config" );

        boolean sameCountOfRquiredFieldsAndAnnotatedFields = annotatedClassFields.size() == configParamsNameAndType.size();
        assertTrue(sameCountOfRquiredFieldsAndAnnotatedFields, "The count of annotated fields and fields to config not corresponds");

        configParamsNameAndType.entrySet().forEach( configFieldEntry -> {

            String configFieldEntryName = configFieldEntry.getKey();

            boolean fieldExists = annotatedClassFields.stream().filter( annotatedField ->
                    {
                        boolean nameFound = annotatedField
                                .getAnnotation(ConfigParam.class)
                                .name()
                                .equals(configFieldEntryName);

                        boolean typeCorresponds = annotatedField
                                                    .getType()
                                                    .equals( configFieldEntry.getValue() );

                        return nameFound && typeCorresponds;
                    }
            ).findAny().isPresent();

            assertTrue( fieldExists, String.format("The config field %s was not found on annotated fields by name or type", configFieldEntry.getKey() ) );

        });
    }

    @Test
    public void shouldParseParams()
    {
        Map<String, Object> rawParams = this.getRawParams();

        Configurable configurable = this.getConfigurable();

        ConfigurableParamsUtils paramUtils = this.getParamUtils();
        Map<String, Object> resolvedParams = paramUtils.extractConfigParamRawValuesMap(rawParams, configurable);

        this.validateResolvedParams( this.getResolvedParams(), resolvedParams );

    }

    @Test
    public void shouldUpdateConfigParams()
    {

        Configurable configurable = this.getConfigurable();

        Map<String, Object> params = this.getResolvedParams();
        configurable.config(params);

        Map<String, Class<?>> configParamsNameAndType = configurable.getConfigParamsNameAndType();
        assertTrue( !configParamsNameAndType.isEmpty(), "No exists fields to config" );

        List<Field> annotatedClassFields = FieldUtils.getFieldsListWithAnnotation(configurable.getClass(), ConfigParam.class);
        assertTrue( !annotatedClassFields.isEmpty(), "No exists annotated fields for config" );

        boolean sameCountOfRquiredFieldsAndAnnotatedFields = annotatedClassFields.size() == configParamsNameAndType.size();
        assertTrue(sameCountOfRquiredFieldsAndAnnotatedFields, "The count of annotated fields and fields to config not corresponds");

        annotatedClassFields.forEach( configField -> {

            try {

                ConfigParam configFieldAnnotation = (ConfigParam) configField.getAnnotation(ConfigParam.class);
                String configFieldParamName = configFieldAnnotation.name();
                Object valueToInsert = params.get(configFieldParamName);

                Object insertedValue = configField.get(configurable);

                assertEquals(valueToInsert, insertedValue, "The value inserted on field %s was not the configured value");

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        });


    }

}
