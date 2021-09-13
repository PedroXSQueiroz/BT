package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools;

import br.com.pedroxsqueiroz.bt.crypto.exceptions.ConfigParamNotFoundException;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Configurable {

    private static final Logger LOGGER = Logger.getLogger(Configurable.class.getName());
	
	protected Configurable parent;

    public abstract void config(Map<String, Object> configParams);

    public abstract Map<String, Class<?>> getConfigParamsNameAndType();

    public abstract boolean isConfigured();

    public abstract Object getConfigParamValue(String propertyFromSource) throws IllegalAccessException, ConfigParamNotFoundException;

    public Map<String, Object> getCurrentConfiguration()
    {
        return this.getConfigParamsNameAndType()
                .keySet()
                .stream()
                .collect(
                    HashMap::new,
                    (map, name) -> {

                        try 
                        {
                            map.put(name, this.getConfigParamValue(name) );
                        } catch (IllegalAccessException | ConfigParamNotFoundException e) {
                            LOGGER.log(Level.WARNING, e.getMessage());
                        } 
                        
                    },Map::putAll
                );

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
