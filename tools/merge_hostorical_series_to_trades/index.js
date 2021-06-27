const fs                        = require('fs');
const minimist                  = require('minimist');
const csvParser                 = require('csv-parser');
const { createObjectCsvWriter } = require('csv-writer');

/**
 * PARÂMETROS
 */
const {
    seriesFile: SERIES_FILE,
    tradeFile:  TRADE_FILE,
    resultFile: RESULT_FILE
} = minimist(process.argv);

console.log( `Params:\n\tseriesFile:\t${SERIES_FILE}\n\ttradeFile:\t${TRADE_FILE}\n\tresultFile:\t${RESULT_FILE}` );


function readTrades(tradeFile, tradeLoadCallback) {
    
    let totalTrades = 0;
    trades = {};
    
    fs.createReadStream(tradeFile)
        .pipe(csvParser())
        .on('data', rowData => {
            
            if(!trades[rowData.Data])
            {
                trades[rowData.Data] = rowData;
                totalTrades++;
            }
            // else
            // {
            //     delete trades[rowData.Data];
            // }

        })
        .on('close', () => {
            console.log(`Trades loaded, total: ${totalTrades}`);

            tradeLoadCallback( trades );

        });
}


function readHistoricSeries(seriesFile, onReadHistoricEntry) {
    
    let totalEntries = 0;

    fs.createReadStream(seriesFile, {
        autoClose: true,
    }).pipe(csvParser())
        .on('data', data => {
            onReadHistoricEntry(data);
            totalEntries++;
        }).on('end', () => console.log(`Historical Series combined: ${totalEntries}`));

}


/**
 * LEITURA DE TRADES
 */
readTrades( TRADE_FILE, (trades) => {

    /**
     * CRIA ESCRITOR DO RESULTADO
     */
    const writer    = createObjectCsvWriter({
        path:   RESULT_FILE,
        header: ['Data', 'Fechamento', 'Tipo'],
        append: true,
        alwaysQuote: true
    });

    /**
     * LÊ SÉRIE HISTÓRICA
     */
    readHistoricSeries(SERIES_FILE, (data) => {

        let currentResultEntry = {
            Data: data.Data,
            Fechamento: data['Último']
        };

        if ((currentTradeEntry = trades[data.Data])) {
            
            if(!currentResultEntry['Tipo'])
            {
                currentResultEntry['Tipo'] = currentTradeEntry.Tipo;
            }
            
        }
        else {
            currentResultEntry['Tipo'] = '---';
        }

        const currentRow = [currentResultEntry];
        
        /**
         * ESCREVE ESTRADA DO RESULTADO
         */
        writer.writeRecords(currentRow)
                    // .then( () => console.log('entry persisted') );

    });

});