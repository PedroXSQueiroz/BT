package br.com.pedroxsqueiroz.bt.crypto.services.deprecated.impl;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.Series;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.factories.RequestFactory;
import br.com.pedroxsqueiroz.bt.crypto.services.deprecated.SeriesService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoinBaseSeriesService implements SeriesService {

    @Qualifier("coinBaseRequestFactory")
    @Autowired
    private RequestFactory requestFactory;

    @Override
    public List<SerialEntry> fecth(StockType type, Instant from, Instant to) {

        try {

            return HttpClients.createDefault().execute(
                    this.requestFactory.setup("GET",
                            String.format(  "quotes/%s/history", type.getName() ) )
                            .withRequestParams("time_start", DateTimeFormatter.ISO_DATE_TIME.format(from) )
                            .withRequestParams( "time_end", DateTimeFormatter.ISO_DATE_TIME.format(from)  )
                            .build(),
                    (response) -> {

                        ObjectMapper serializer = new ObjectMapper();

                        Series series = new Series();
                        series.setStockType(type);

                        List<Double> itens =  new ArrayList<Double>();

                        JsonNode responseJsonRoot = serializer.readTree(response.getEntity().getContent());

                        for(JsonNode currentItem : responseJsonRoot)
                        {
                            itens.add(currentItem.get("bid_price").asDouble());
                        }

                        series.setItens(itens);

                        return null;
                    }
            )

            ;

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null ;
    }

    @Override
    public void write(List<SerialEntry> series) {

    }
}
