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
            <button id="button">Send</button>
        </div>

        <div class="col-lg-4 ml-3">
            <p id="result"></p>
        </div>
    </div>
</div>
<script>
    $('#button').click(function setData() {
        let selectedValues = $('#region_select').val();
        $.ajax({
            type: 'POST',
            data: {selectedValues: selectedValues},
            url: 'index',
            success: function (answer) {
                $('#result').text(answer);
            }
        });
    });
</script>
</body>
</html>