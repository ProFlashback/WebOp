var wlDialog = $( "#whitelistPage" ).dialog(
    {
        autoOpen: false,
        minHeight: 400,
        maxHeight: 400,
        width: 400,
        modal: true
    });

$("#whitelistLink").click(function()
{
    // $("#whitelistPage").dialog("open");
    wlDialog.load('viewpage.php?whitelist').dialog('open');
});

