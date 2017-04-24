$(function () {
    $('#buttonJson').click(function () {
        var array = [];
        var json = $('tr.row-data').map(function (i, v) {
            var td = $(this).find('td');
            var itemId = td.first().text();
            var cateCode = $(this).find('input').val();
            array.push({
                itemId: itemId,
                cateCode: cateCode
            });
        });
        var data = {
            category: $('#category').val(),
            period: $('#period').val(),
            list: array
        };
        $.ajax({
            type: "post",
            url: "/checkCategory/update",
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify(data),
            success: function (result) {
                alert('修改成功');
            },
            failure: function () {
                alert('修改失败');
            }
        })
    });
});