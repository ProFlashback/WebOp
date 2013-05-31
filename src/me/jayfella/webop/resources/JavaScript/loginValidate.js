$('#loginForm').submit(function(evt)
{
    var errors = 0;

    $('#loginForm :input').map(function()
    {
        if(!$.trim(this.value).length)
        {
            $(this).parents('div').addClass('warning');
            errors++;
        }
        else if ($.trim(this.value).length)
        {
            $(this).parents('div').removeClass('warning');
        }

    });

    if(errors > 0)
    {
        return false;
    }

    var $form = $(this);
    var $inputs = $form.find('input, select, button, textarea');
    var serializedData = $form.serialize();

    $inputs.prop('disabled', true);

    var request = $.ajax({ url: 'login.php', type: 'post', data: serializedData });

    request.done(function(response, textStatus, jqXHR)
        {
            if (response === 'OK')
            {
                window.location.href = '/';
            }
            else
            {
                $(document.body).html(response);
            }
        });

    request.fail(function(jqXHR, textStatus, errorThrown) { });
    request.always(function() { $inputs.prop('disabled', false); });

    evt.preventDefault();
});