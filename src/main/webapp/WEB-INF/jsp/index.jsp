<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0"
            crossorigin="anonymous"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="js/Chart.min.js"></script>
</head>
<body>
<div class="container mx-4 px-0 my-4">
    <div class="row">
        <div class="col-lg-4">
            <label for="region_select">Select region</label>
            <select id="region_select" class="form-select" multiple aria-label="Select region">
                <c:forEach items="${regions}" var="region">
                    <option name="regionSelected" value="${region.getId()}">${region.getName()}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-lg-4">
            <label for="target_audience_select">Select target audience</label>
            <select id="target_audience_select" class="form-select" multiple aria-label="Select target audience">
                <c:forEach items="${audiences}" var="target_audience">
                    <option name="targetAudienceSelected"
                            value="${target_audience.getId()}">${target_audience.getName()}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-lg-4">
            <label for="category_select">Select category</label>
            <select id="category_select" class="form-select" aria-label="Select category">
                <c:forEach items="${categories}" var="category">
                    <option name="categoriesSelected"
                            value="${category.getId()}">${category.getName()}</option>
                </c:forEach>
            </select>
            <button id="button">Send</button>
        </div>
    </div>
    <div class="row">
        <div class="col-lg-4 ml-3">
            <p id="result"></p>
        </div>
        <canvas id="canvas"></canvas>
    </div>
</div>
<script>
    let chart;
    $(document).ready(function() {
        let ctx = $('#canvas');
        chart = new Chart(ctx, {
            type: 'line',
            data: {
                datasets: [{
                    label: 'Распределение',
                    backgroundColor: 'rgba(3, 174, 41, 0.4)',
                    borderColor: 'rgb(3, 174, 41)',
                    borderWidth: 1
                }]
            }
        });
    });

    $('#button').click(function setData() {
        let selectedValues = $('#region_select').val();
        let selectedValuesTargetAudience = $('#target_audience_select').val();
        let selectedCategory = $('#category_select').val();
        $.ajax({
            type: 'POST',
            data: {
                selectedValues: selectedValues,
                selectedValuesTargetAudience: selectedValuesTargetAudience,
                selectedCategory: selectedCategory
            },
            url: 'index',
            success: function (answer) {
                chart.data.labels = answer.pointNames;
                chart.data.datasets[0].data = answer.pointValues;
                chart.update();
            }
        });
    });
</script>
</body>
</html>