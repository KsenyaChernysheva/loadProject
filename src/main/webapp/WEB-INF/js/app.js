const googleTrends = require('google-trends-api');
var date = new Date();
googleTrends.interestOverTime({
    keyword: 'банк',
    startTime: new Date('2021-03-11'),
    endTime: new Date('2021-04-11'),
    geo: 'RU-MO'
}, function (err, results) {
    if (err) console.log('oh no error!\n' + err);
    else console.log("results -> " + results);
});