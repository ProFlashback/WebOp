$(document).ready(function()
{
    $("#clearSearchResults").click(function()
    {
        $('#searchOutput').clear();
    });
});



$('#ajaxloader').hide();

$('#logSearchForm').submit(function(evt)
{
    var $form = $(this);
    var $inputs = $form.find('input, select, button, textarea');
    var serializedData = $form.serialize();

    $inputs.prop('disabled', true);
    $('#ajaxloader').show();

    var request = $.ajax(
    {
        url: 'searchlog.php',
        type: 'post',
        data: serializedData
    });

    request.done(function(response, textStatus, jqXHR) { $('#searchOutput').html(response); } );
    request.fail(function(jqXHR, textStatus, errorThrown) {  });

    request.always(function()
    {
        $inputs.prop('disabled', false);
        $('#ajaxloader').hide();
        // $('#searchOutput').val('');
    });

    evt.preventDefault();

});