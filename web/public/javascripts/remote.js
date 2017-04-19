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
})