$(function(){
    $('#segType').change(function(){
        var self=$(this);
        var value=self.find(":selected").text()
        $('label[for="depends"]').remove();
        $('#segTypeCode').val(value);
        if(value.indexOf("SUBBRAND")>-1){
            $('#depends').remove();
            $('#dependsLabel').remove();
            $('label[for="depends"]').remove();
            var depends=$("<label id='dependsLabel'>depends:</label><input type='text' name='depends' class='form-control' id='depends'/>")
            self.after(depends)
        }else{
            $('#depends').remove();
            $('#dependsLabel').remove();
        }
    });

    $('#category').blur(function(){
        var category=$(this).val();
        $.get( '/pre/segments', {category:category} ).done(function(response){
            if(response.length>0){
                $('#segType').children().remove();
                var options=response.forEach(function(i,v){
                   $('#segType').append($('<option data-value='+i.name+' value='+i.code+'>'+i.name+'</option>'));
                })
            }
        })
    });
});