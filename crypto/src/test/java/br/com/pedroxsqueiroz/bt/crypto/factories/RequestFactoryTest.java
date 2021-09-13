package br.com.pedroxsqueiroz.bt.crypto.factories;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Test;

public class RequestFactoryTest {

	protected RequestFactory getRequestFactory()
	{
		return new RequestFactory();		
	}
	
	protected Map<String, String> getDummyDefaultParameters()
	{
		
		Map<String, String> params = new HashMap<String, String>();
		
		params.put("dummy", "1");
		params.put("dummy1", "2");
		params.put("dummy2", "3");
		
		return params;
	}
	
	@Test
	public void shouldAlwaysCreateRequestWithDefaultParams() throws CloneNotSupportedException 
	{
		RequestFactory requestFactory = this.getRequestFactory();
		
		Map<String, String> dummyDefaultParameters = this.getDummyDefaultParameters();
		
		dummyDefaultParameters.forEach( (key, value) -> {
			try {
				requestFactory.withRequestParams(key, value);
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} );
		
		HttpUriRequest request = requestFactory.setup("GET", "http://somewhere/do/something").build();
		
		List<NameValuePair> obtainedParams = new URIBuilder(request.getURI()).getQueryParams();
		
		dummyDefaultParameters.keySet().forEach( currentKeyDummyParam -> {
			
			Optional<NameValuePair> foundedParam = obtainedParams.stream()
															.filter( currentObtainedParam -> currentObtainedParam.getName().equals(currentKeyDummyParam) )
															.findAny();
			
			assertTrue( foundedParam.isPresent() );
			
			assertEquals( dummyDefaultParameters.get(currentKeyDummyParam), foundedParam.get().getValue(), String.format( "Param %s with divergent value from input", currentKeyDummyParam ) );
		});
		
	}
	
}
