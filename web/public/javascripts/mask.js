$(function(){
    $('form').submit(function(){
        var div=$('<div class="overlay"><span>loading...</span></div>');
        var height=$('div.main').height();
        div.height(height+200)
        $('body').append(div);
    });
    $('ul.pager').click(function(){
        var div=$('<div class="overlay"><span>loading...</span></div>');
        var height=$('div.main').height();
        div.height(height+200)
        $('body').append(div);
    });
})
