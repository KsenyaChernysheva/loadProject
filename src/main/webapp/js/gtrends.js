const googleTrends = require('google-trends-api');
var date = new Date();
googleTrends.interestOverTime({
    keyword: process.argv[2],
    startTime: new Date(process.argv[3]),
    endTime: new Date(process.argv[4]),
    geo: process.argv[5],
    granularTimeResolution: true
}, function (err, results) {
    if (err) console.log('oh no error!\n' + err);
    else console.log(JSON.stringify(JSON.parse(results).default.timelineData));
});