package br.com.pedroxsqueiroz.bt.crypto.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigurableParamsUtils;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BotServiceTest {
	
	private static final String START_INTERVAL = "2021-01-01T01:00:00";
	private static final String FINAL_INTERVAL = "2020-01-01T02:00:00";
	
	private static final Instant START_INTERVAL_DATETIME = LocalDateTime
																.parse( START_INTERVAL, 
																			DateTimeFormatter.ISO_DATE_TIME )
																.atZone(ZoneId.systemDefault())
																.toInstant();
	
	private static final Instant FINAL_INTERVAL_DATETIME = LocalDateTime
																.parse( FINAL_INTERVAL, 
																		DateTimeFormatter.ISO_DATE_TIME )
															.atZone(ZoneId.systemDefault())
															.toInstant();
	
	@Mock
	private MarketFacade marketFacade;
	
	@Mock
	private TradeAlgorithm algorihtm;
	
	@Mock
	private AmmountExchanger ammountExchanger;
	
	@Mock
	private EntryAmmountGetter entryAmmountGetter;
	
	@Mock
	private ExitAmmountGetter exitAmmountGetter;
	
	@Mock
	private ConfigurableParamsUtils paramsUtils;
	
	@InjectMocks
	private BotService botService;
	
	@BeforeAll
	public void init() 
	{
		
		Mockito
			.doReturn(new HashMap<String, Object>() {{
				put("botName", "dummy");
				put("algorithm", algorihtm);
				put("market", marketFacade);
				put("ammountExchanger", ammountExchanger);
				put("entryAmmountGetter", entryAmmountGetter);
				put("exitAmmountGetter", exitAmmountGetter);
				put("startInterval", START_INTERVAL_DATETIME);
				put("endInterval", FINAL_INTERVAL_DATETIME);
				put("intervalEntriesUnit", ChronoUnit.MINUTES);
			}})
			.when(this.paramsUtils)
			.extractConfigParamRawValuesMap(Mockito.anyMap(), Mockito.any());
	}
	
	private Map<String, Object> getDummyParameters()
	{
		return new HashMap<String, Object>(){{
			put("botName", "dummy");
			put("algorithm", new HashMap<String, Object>() {{
				put("name", "dummy");
			}});
			put("market", new HashMap<String, Object>() {{
				put("name", "dummy");
			}});
			put("ammountExchanger", new HashMap<String, Object>() {{
				put("name", "dummy");
			}});
			put("entryAmmountGetter", new HashMap<String, Object>() {{
				put("name", "dummy");
			}});
			put("exitAmmountGetter", new HashMap<String, Object>() {{
				put("name", "dummy");
			}});
			put("exitAmmountGetter", new HashMap<String, Object>() {{
				put("name", "dummy");
			}});
			put("startInterval", START_INTERVAL);
			put("endInterval", FINAL_INTERVAL);
			put("intervalEntriesUnit", "m");
		}};
	}
	
	@Test
	public void shouldCreateByParams()
	{
		Map<String, Object> dummyParameters = this.getDummyParameters();
		Bot bot = this.botService.create(dummyParameters);
		
		assertEquals("dummy", bot.name);
		assertEquals(this.algorihtm, bot.algorithm);
		assertEquals(this.marketFacade, bot.marketFacade);
		assertEquals(this.ammountExchanger, bot.ammountExchanger);
		assertEquals(this.entryAmmountGetter, bot.entryAmmountGetter);
		assertEquals(this.exitAmmountGetter, bot.exitAmmountGetter);
		assertEquals(START_INTERVAL_DATETIME, bot.startInterval);
		assertEquals(FINAL_INTERVAL_DATETIME, bot.endInterval);
		assertEquals(ChronoUnit.MINUTES, bot.intervalEntriesUnit);
	}
	
}
