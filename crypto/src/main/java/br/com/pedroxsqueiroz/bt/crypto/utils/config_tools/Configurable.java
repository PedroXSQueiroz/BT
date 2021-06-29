package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools;

import br.com.pedroxsqueiroz.bt.crypto.exceptions.ConfigParamNotFoundException;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Configurable {

    private Configurable parent;

    public abstract void config(Map<String, Object> configParams);

    public abstract Map<String, Class<?>> getConfigParamsNameAndType();

    public abstract boolean isConfigured();

    public abstract Object getConfigParamValue(String propertyFromSource) throws IllegalAccessException, ConfigParamNotFoundException;

    public Map<String, Object> getCurrentConfiguration()
    {
        //FIXME: CRIAR UM MÉTODO QUE OBTENHA SOMENTE OS NOMES DOS CAMPOS
        return this.getConfigParamsNameAndType()
                .keySet()
                .stream()
                .collect(
                    HashMap::new,
                    (map, name) -> {

                        try {
                            map.put(name, this.getConfigParamValue(name) );
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (ConfigParamNotFoundException e) {
                            e.printStackTrace();
                        }

                    },Map::putAll
                );
                /*.collect(
                        Collectors.toMap(
                                paramName -> paramName,
                                paramName -> {

                                    try {
                                        return this.getConfigParamValue(paramName);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (ConfigParamNotFoundException e) {
                                        e.printStackTrace();
                                    }

                                    return null;
                                }
                        ));*/


    }

    public void setParent(Configurable configurable)
    {
        this.parent = configurable;
        this.parent.addChild(this);
    }

    private List<Configurable> children = new ArrayList<>();

    public void addChild(Configurable child)
    {
        this.children.add(child);
    }

    public List<Configurable> getChildren()
    {
        return this.children;
    }

    public Configurable getParent()
    {
        return this.parent;
    }

}
