var consoleDataReload = function()
{
    $('#consoleData').load('viewdata.php?consoledata');
    $('#consoleData').scrollTop($('#consoleData').prop('scrollHeight'));
    setTimeout(consoleDataReload, 1000);
};

setTimeout(consoleDataReload, 1000);