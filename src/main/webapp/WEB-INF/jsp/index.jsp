<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="UTF-8">
    <title>Нагрузка</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0"
            crossorigin="anonymous"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="js/Chart.min.js"></script>
    <script src="js/index.js" type="application/javascript"></script>
    <link type="text/css" href="css/style.css" rel="stylesheet" />
</head>
<body>
<div class="container-fluid mx-4 px-0 my-4" style="max-width:90%">
    <div class="row">
        <div class="col-lg-6">
            <label for="region_select">Выберите регион</label>
            <select id="region_select" class="form-select" size="6" multiple aria-label="Select region">
                <c:forEach items="${regions}" var="region">
                    <option name="regionSelected" value="${region.getId()}">${region.getName()}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-lg-4">
            <div class="mb-3">
                <label for="target_audience_select">Выберите целевую аудиторию сервиса</label>
                <select id="target_audience_select" class="form-select" aria-label="Select target audience">
                    <c:forEach items="${audiences}" var="target_audience">
                        <option name="targetAudienceSelected"
                                value="${target_audience.getId()}">${target_audience.getName()}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="mb-3">
                <label for="category_select">Выберите категорию сервиса</label>
                <select id="category_select" class="form-select" aria-label="Select category">
                    <c:forEach items="${categories}" var="category">
                        <option name="categoriesSelected"
                                value="${category.getId()}">${category.getName()}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="mb-2">
                <button class="btn btn-outline-success" id="button">Получить распределение</button>
            </div>
        </div>
    </div>
    <div class="row mt-3">
        <div class="col-lg-auto border border-1 box chart" style="display: none;">
            <canvas id="canvas"></canvas>
        </div>
        <div class="col-lg-auto border border-1 box chart" style="display: none;">
            <canvas id="canvas2"></canvas>
        </div>
        <div class="col-lg-auto border border-1 box chart" style="display: none;">
            <canvas id="canvas3"></canvas>
        </div>
        <div class="col-lg-auto border border-1 box chart" style="display: none;">
            <canvas id="canvas4"></canvas>
        </div>
        <div id="overlay" class="overlay">
            <div class="row justify-content-center">
                <div class="col-md-3">
                    <div id="no_data" class="mt-4 py-4 overlay_box">
                        <span class="no-data">Недостаточно данных</span>
                    </div>
                </div>
            </div>
            <div class="row justify-content-center">
                <div class="col-md-1">
                    <div id="loading" class="upper">
                        <div class="spinner"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>