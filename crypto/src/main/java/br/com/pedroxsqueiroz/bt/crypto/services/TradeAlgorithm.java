package br.com.pedroxsqueiroz.bt.crypto.services;

import br.com.pedroxsqueiroz.bt.crypto.dtos.SerialEntry;
import br.com.pedroxsqueiroz.bt.crypto.dtos.StockType;
import br.com.pedroxsqueiroz.bt.crypto.dtos.TradePosition;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.*;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.AnnotadedFieldsConfigurer;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ConfigParam;
import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.Configurable;
import br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands.Finishable;
import br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands.Startable;
import br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands.Stopable;
import lombok.experimental.Delegate;

import java.util.List;

public abstract class TradeAlgorithm extends Configurable implements Startable, Stopable, Finishable {

    /*-----------------------------------------------------------------------------------------
    MARKET ADAPTERS
    ----------------------------------------------------------------------------------------- */

    public interface EntryTradePositionMethod
    {
        TradePosition entry( TradePosition trade );
    }

    private EntryTradePositionMethod entryMethod;

    public interface ExitTradePositionMethod
    {
        TradePosition exit( TradePosition trade );
    }

    private ExitTradePositionMethod exitMethod;

    public void setEntryMethod( EntryTradePositionMethod entry )
    {
        this.entryMethod = entry;
    }

    public TradePosition entryPosition( TradePosition trade )
    {
        return this.entryMethod.entry(trade);
    }

    public void setExitMethod( ExitTradePositionMethod exit )
    {
        this.exitMethod = exit;
    }

    public TradePosition exitPosition( TradePosition trade)
    {
        return this.exitMethod.exit(trade);
    }

    public interface FetchSeriesEntryMethod
    {
        List<SerialEntry> fetchNext(StockType type);
    }

    private FetchSeriesEntryMethod fetchSeriesEntry;

    public void setFetchNextSeriesEntryMethod(FetchSeriesEntryMethod fetch)
    {
        this.fetchSeriesEntry = fetch;
    }

    protected List<SerialEntry> fetchNextSeriesEntry(StockType type)
    {
        return this.fetchSeriesEntry.fetchNext(type);
    }

    /*-----------------------------------------------------------------------------------------
    ALGORITHM LOGIC
    -----------------------------------------------------------------------------------------*/
    protected abstract void logic();

    /*-----------------------------------------------------------------------------------------
    COMMANDS
    -----------------------------------------------------------------------------------------*/
    protected abstract void prepare();

    private boolean alive;

    public boolean isAlive()
    {
        return this.alive;
    }

    public void start() throws ImpossibleToStartException {

        if(this.alive)
        {
            throw new ImpossibleToStartException(
                    new TradeAlgorithmInstanceAlreadyStartedException()
            );
        }

        if(!this.isConfigured())
        {
            throw new ImpossibleToStartException(
                    new TradeAlgorithmExcutionMissingPrerequisitesException()
            );
        }

        this.alive = true;
        this.logic();
    }

    protected abstract void stopLogic();

    public void stop() throws ImpossibleToStopException {

        if(this.alive)
        {
            this.stopLogic();
            this.alive = false;
        }
        else
        {
            throw new ImpossibleToStopException(
                new TradeAlgorithmAlreadyStoppedException()
            );
        }

    }

    public TradeAlgorithm build() throws TradeAlgorithmBuildMissingPrerequisitesException {

        if(this.isConfigured())
        {
            this.prepare();
            return this;
        }

        throw new TradeAlgorithmBuildMissingPrerequisitesException();
    }

    @Delegate( types = Configurable.class)
    public AnnotadedFieldsConfigurer configurer = new AnnotadedFieldsConfigurer(this);

    abstract public void closeCurrentPosition();

}
