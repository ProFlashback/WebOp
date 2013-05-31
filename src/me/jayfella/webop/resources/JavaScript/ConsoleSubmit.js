$('#consoleForm').submit(function(evt)
{
    var $form = $(this);
    var $inputs = $form.find('input, select, button, textarea');
    var serializedData = $form.serialize();

    $inputs.prop('disabled', true);

    var request = $.ajax(
    {
        url: 'console.php',
        type: 'post',
        data: serializedData
    });

    request.done(function(response, textStatus, jqXHR) { } );
    request.fail(function(jqXHR, textStatus, errorThrown) { });

    request.always(function()
    {
        $inputs.prop('disabled', false);
        $('#consoleText').val('');
    });

    evt.preventDefault();

});