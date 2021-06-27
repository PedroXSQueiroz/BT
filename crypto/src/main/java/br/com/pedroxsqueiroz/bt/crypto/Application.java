package br.com.pedroxsqueiroz.bt.crypto;

import br.com.pedroxsqueiroz.bt.crypto.services.MarketFacade;
import br.com.pedroxsqueiroz.bt.crypto.services.deprecated.WalletService;
import br.com.pedroxsqueiroz.bt.crypto.services.impl.TrendFollowingTradeAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Set;
import java.util.logging.Logger;

@SpringBootApplication
@EnableAutoConfiguration
@EnableConfigurationProperties
@EnableScheduling
public class Application {

	private static Logger LOGGER = Logger.getLogger( Application.class.getName() );

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Autowired
	private Set<WalletService> wallets;

	/*
	@PostConstruct
	public void setupApplication() {

		this.wallets.forEach( wallet -> {
			wallet.setup();
		});

	}
	*/

	@Autowired
	private ApplicationArguments args;

	@Autowired
	private TrendFollowingTradeAlgorithm tf;

	@Qualifier("csv")
	@Autowired
	private MarketFacade marketFacade;

	/*
	@PostConstruct
	public void startTradeBTCBinance() throws
			TradeAlgorithmExcutionMissingPrerequisitesException,
			TradeAlgorithmInstanceAlreadyStartedException,
			IOException,
			TradeAlgorihtmNameNotFoundException {

		List<String> stockOptions = this.args.getOptionValues("stock");
		StockType stockType = new StockType();
		stockType.setName(stockOptions.get(0));

		List<String> outputTradeOptions = this.args.getOptionValues("outputTrade");
		String outputTrade = outputTradeOptions.get(0);
		File outputTradeFile = new File(outputTrade);
		FileOutputStream outputStreamTrade = new FileOutputStream(outputTradeFile);
		CSVWriter tradeWriter = new CSVWriter(new OutputStreamWriter(outputStreamTrade));

		List<String> outputSeriesOptions = this.args.getOptionValues("outputSeries");
		String outputSeries = outputSeriesOptions.get(0);
		File outputSeriesFile = new File(outputSeries);
		FileOutputStream outputStreamSeries = new FileOutputStream(outputSeriesFile);
		CSVWriter seriesWriter = new CSVWriter(new OutputStreamWriter(outputStreamSeries));

		tradeWriter.writeNext(new String[] { "Data", "Valor", "Cotas", "Tipo" });
		tradeWriter.flush();
		seriesWriter.writeNext(new String[]{"Data","Último","Abertura","Máxima","Mínima","Vol.","Var%"});
		seriesWriter.flush();

		TradeProcessor tradeProcessor = new TradeProcessor(
				TradeAlgorithm.of("trendFollowing"),
				this.marketFacade,
				stockType,
				new ArrayList<>() {{

					add((tradePosition) -> {

						ZonedDateTime zonedDateTime = tradePosition.getEntryTime().atZone(ZoneId.systemDefault());
						String currentDate = DateTimeFormatter.ISO_DATE_TIME.format(zonedDateTime);
						LOGGER.info( String.format("Trade entry at [%s]", currentDate) );

					});

				}},
				new ArrayList<>() {{

					add((tradePosition) -> {

						try
						{

							ZonedDateTime zonedDateTime = tradePosition.getEntryTime().atZone(ZoneId.systemDefault());
							String currentDate = DateTimeFormatter.ISO_DATE_TIME.format(zonedDateTime);
							LOGGER.info( String.format("Trade exit at [%s]", currentDate) );

							tradeWriter.writeNext(tradePosition.getEntryRow());
							tradeWriter.writeNext(tradePosition.getExitRow());

							tradeWriter.flush();

						} catch (IOException e) {
							e.printStackTrace();
						}

					});

				}},
				new ArrayList<>() {{

					add((serialEntries) -> {

						serialEntries.forEach( serialEntry -> {
							try {

								ZonedDateTime zonedDateTime = serialEntry.getDate().toInstant().atZone(ZoneId.systemDefault());
								String currentDate = DateTimeFormatter.ISO_DATE_TIME.format(zonedDateTime);
								LOGGER.info( String.format("Series fecthed at [%s]", currentDate) );

								String[] newRow = new String[] {
									currentDate,
									Double.toString( serialEntry.getClosing() ),
									Double.toString( serialEntry.getOpening() ),
									Double.toString( serialEntry.getMax() ),
									Double.toString( serialEntry.getMin() ),
									Double.toString( serialEntry.getVolume() ),
									"---"
								};

								seriesWriter.writeNext(newRow);

								seriesWriter.flush();

							} catch (IOException e) {
								e.printStackTrace();
							}

						});

					});

				}});

		tradeProcessor.start(stockType, new HashMap<String, Object>(){{
			put("stockType", stockType);
		}});

	}
		*/

}
