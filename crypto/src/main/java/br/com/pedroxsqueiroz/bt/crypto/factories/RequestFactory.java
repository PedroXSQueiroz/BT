package br.com.pedroxsqueiroz.bt.crypto.factories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;

@Component
public class RequestFactory implements Cloneable {

    private static Logger LOGGER = Logger.getLogger( RequestFactory.class.getName() );

    public interface RequestAssigner
    {
        void assign( RequestBuilder builder );
    }

    public RequestFactory()
    {
        this.headers = new HashMap<String, String>();
        this.defaultHeaders = new HashMap<String, String>();
        this.params = new HashMap<String, String>();
        this.defaultParams = new HashMap<String, String>();
    }

    public RequestBuilder currentBuilder;

    private RequestAssigner assigner;

    public void setAssigner(RequestAssigner assigner)
    {
        this.assigner = assigner;
    }

    private String root;

    public void setRoot(String root)
    {
        this.root = root;
    }

    private Map<String, String> defaultParams;

    private Map<String, String> params;

    public void withDefaultRequestParams(String name, String value)
    {
        this.defaultParams.put(name, value);
    }

    public RequestFactory withRequestParams(String name, String value) throws CloneNotSupportedException
    {
        this.params.put(name, value);

        return (RequestFactory) this.clone();
    }

    private Map<String, String> defaultHeaders;

    private Map<String, String> headers;

    public void withDefaultRequestHeaders(String name, String value)
    {
        this.defaultHeaders.put(name, value);
    }

    public RequestFactory withRequestHeader(String name, String value) throws CloneNotSupportedException
    {
        this.headers.put(name, value);
        return (RequestFactory) this.clone();
    }

    public RequestFactory setup( String method, String path ) throws URISyntaxException, CloneNotSupportedException {

        RequestFactory selfClone = (RequestFactory) this.clone();

        RequestBuilder requestBuilder = getRequestBuilder(method, path);

        selfClone.currentBuilder = requestBuilder;

        return selfClone;
    }

    public RequestFactory setup(String method, String path, Object body ) throws JsonProcessingException, CloneNotSupportedException {

        RequestBuilder requestBuilder = getRequestBuilder(method, path);

        ObjectMapper serializer = new ObjectMapper();

        requestBuilder.setEntity( new StringEntity( serializer.writeValueAsString(body), ContentType.APPLICATION_JSON ));

        RequestFactory selfClone = (RequestFactory) this.clone();
        selfClone.currentBuilder = requestBuilder;

        return selfClone;
    }

    public RequestFactory assign() throws CloneNotSupportedException {
        this.assigner.assign(this.currentBuilder);
        return (RequestFactory) this.clone();
    }

    public HttpUriRequest build(){
        return  this.currentBuilder.build();
    }

    private RequestBuilder getRequestBuilder(String method, String path) {
        String uri = String.format("%s/%s",this.root, path);

        LOGGER.info(String.format( "Building Request to %s %s", method, uri ));

        RequestBuilder requestBuilder = RequestBuilder
                .create(method)
                .setUri(uri);

        this.defaultHeaders.forEach((key, value) -> requestBuilder.addHeader(new BasicHeader(key, value)));

        this.headers.forEach((key, value) -> requestBuilder.addHeader(new BasicHeader(key, value)));
        this.headers.clear();

        this.defaultParams.forEach((key, value) -> requestBuilder.addParameter(key, value));

        this.params.forEach((key, value) -> requestBuilder.addParameter(key, value));
        this.params.clear();

        return requestBuilder;
    }

}
