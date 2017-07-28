$(function(){
    $('form').submit(function(){
        var desc=$('#description').val()
        $('#content').empty();
        $.ajax({
            url:"/manual",
            data:{description:desc},
            success:function(response){
                var div=$("<div/>")
                response.forEach(function(v){
                    $('#content').append(div.clone().text(v))
                });
            }
        });
        return false;
    });
})