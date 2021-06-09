let chart;
let chartLine;
let chartPlato;
let chartPeak;
$(document).ready(function() {
    $('#no_data').hide();
    $('#overlay').hide();
    $('#loading').hide();
    let ctx = $('#canvas');
    chart = new Chart(ctx, {
        type: 'line',
        data: {
            datasets: [{
                label: 'Распределение по времени',
                backgroundColor: 'rgba(3, 174, 41, 0.4)',
                borderColor: 'rgb(3, 174, 41)',
                borderWidth: 1
            }]
        }
    });
    let ctx2 = $('#canvas2');
    chartLine = new Chart(ctx2, {
        type: 'line',
        data: {
            datasets: [{
                label: 'Линейное распределение',
                backgroundColor: 'rgba(3, 174, 41, 0.4)',
                borderColor: 'rgb(3, 174, 41)',
                borderWidth: 1
            }],
            labels: ['Минимальное', 'Максимальное']
        }
    });
    let ctx3 = $('#canvas3');
    chartPlato = new Chart(ctx3, {
        type: 'line',
        data: {
            datasets: [{
                label: 'График нагрузки',
                backgroundColor: 'rgba(3, 174, 41, 0.4)',
                borderColor: 'rgb(3, 174, 41)',
                borderWidth: 1,
                cubicInterpolationMode: 'monotone',
                tension: 0,
            }],
            labels: ['Минимальное', 'Максимальное', '']
        }
    });
    let ctx4 = $('#canvas4');
    chartPeak = new Chart(ctx4, {
        type: 'line',
        data: {
            datasets: [{
                label: 'График пиковой нагрузки',
                backgroundColor: 'rgba(3, 174, 41, 0.4)',
                borderColor: 'rgb(3, 174, 41)',
                borderWidth: 1,
                cubicInterpolationMode: 'monotone',
                tension: 0,
            }],
            labels: ['Минимальное', 'Среднее', '', '', '', '', '', '', 'Максимальное', '', '', '', '', '', '']
        }
    });
});

$(document).on('click', '#button', function setData() {
    startLoading();
    let selectedValues = $('#region_select').val();
    let selectedTargetAudience = $('#target_audience_select').val();
    let selectedCategory = $('#category_select').val();
    $.ajax({
        type: 'POST',
        data: {
            selectedValues: selectedValues,
            selectedTargetAudience: selectedTargetAudience,
            selectedCategory: selectedCategory
        },
        url: 'index',
        success: function (answer) {
            if (!answer.empty) {
                chart.data.labels = answer.pointNames;
                chart.data.datasets[0].data = answer.pointValues;
                chart.update();
                var min = Math.min.apply(null, answer.pointValues);
                var max = Math.max.apply(null, answer.pointValues);
                var avg = answer.pointValues.reduce((a, b) => a + b) / answer.pointValues.length;
                chartLine.data.datasets[0].data = [min, max];
                chartLine.update();
                chartPlato.data.datasets[0].data = [min, max, max];
                chartPlato.update();
                chartPeak.data.datasets[0].data = [min, avg, avg, avg, avg, avg, avg, avg, max, avg, avg, avg, avg, avg, avg];
                chartPeak.update();
            }
            stopLoading(!answer.empty);
        }
    });
});

function startLoading() {
    $('#button').prop('disabled',true);
    $('#no_data').hide();
    $('#overlay').show();
    $('#loading').show();
}

function stopLoading(hasData) {
    if (hasData) {
        $('#overlay').hide();
        $('.chart').each(function () {
            $(this).show();
        });
    } else {
        chart.clear();
        chartLine.clear();
        chartPlato.clear();
        chartPeak.clear();
        $('#loading').hide();
        $('#no_data').show();
    }
    $('#button').prop('disabled',false);
}