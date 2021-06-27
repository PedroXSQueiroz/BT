const fs                        = require('fs');
const minimist                  = require('minimist');
const csvParser                 = require('csv-parser');
const { createObjectCsvWriter } = require('csv-writer');
const StreamArray               = require("stream-json/streamers/StreamArray");


const {
    backtestResultFile: BACKTEST_RESULT_FILE,
    resultFile:         RESULT_FILE
} = minimist(process.argv);


const writer    = createObjectCsvWriter({
    path:   RESULT_FILE,
    header: ['Data', 'Fechamento', 'Tipo'],
    append: true,
    alwaysQuote: true
});

fs.createReadStream( BACKTEST_RESULT_FILE)
    .pipe(StreamArray.withParser())
    .on('data', (currentTrade) => {
        
        console.log(currentTrade);

        writer.writeRecords([{
            Data: currentTrade.value.date,
            Fechamento: currentTrade.value.closing,
            Tipo: currentTrade.value.tradeMovementType || '---'
        }]);

    });