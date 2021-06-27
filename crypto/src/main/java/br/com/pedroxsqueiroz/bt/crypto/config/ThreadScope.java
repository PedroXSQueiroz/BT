package br.com.pedroxsqueiroz.bt.crypto.config;

import org.apache.commons.collections.map.MultiKeyMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public class ThreadScope implements Scope {

    private MultiKeyMap threadsToObjectsMap = new MultiKeyMap();

    private MultiKeyMap threadsToRomoveCallbacks = new MultiKeyMap();

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {

        long currentThreadId = Thread.currentThread().getId();
        if( this.threadsToObjectsMap.containsKey(currentThreadId, name) )
        {
            this.threadsToObjectsMap.put(currentThreadId, name, objectFactory.getObject());
        }

        return this.threadsToObjectsMap.get(currentThreadId, name);
    }

    @Override
    public Object remove(String name) {
        long currentThreadId = Thread.currentThread().getId();

        return this.threadsToObjectsMap.remove(currentThreadId, name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable runnable) {

        long currentThreadId = Thread.currentThread().getId();

        this.threadsToRomoveCallbacks.put(currentThreadId, name, runnable);

    }

    @Override
    public Object resolveContextualObject(String name) {
        return this.threadsToObjectsMap.get(Thread.currentThread().getId(), name);
    }

    @Override
    public String getConversationId() {
        long id = Thread.currentThread().getId();
        return Long.toString(id);
    }
}
