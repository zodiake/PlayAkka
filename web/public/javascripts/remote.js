$(function(){
    $('div.row input[type="checkBox"]').click(function(event){
        var source=$(event.target);
        var text=source.parent().find('input[type="text"]');
        var disabled=text.prop('disabled')
        if(disabled){
            text.prop('disabled',false)
        }else{
            text.prop('disabled',true)
        }
    });

    $.ajax({
        url: '/deployXml/getAllDbNames',
        success: function (data) {
            $(".auto-complete").autocomplete({
                minLength: 2,
                source: data
            });
        }
    });
})