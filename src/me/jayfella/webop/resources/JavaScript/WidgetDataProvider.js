var widgetPage = '';
var tpsData = [];
var memData = [];
var entData = [];
var cnkData = [];

var widgetDataReload = function()
{
    $.get('viewdata.php?widgetdata', function(response)
    {
        widgetPage = response;
        var lines = widgetPage.split('\n');

        var tpsPreData = lines[0].split(',');
        tpsData = [];

        for (var i = 0; i < tpsPreData.length; i++)
        {
            tpsData.push([i, tpsPreData[i]]);
        }

        var memPreData = lines[1].split(',');
        memData = [];

        for (var i = 0; i < memPreData.length; i++)
        {
            memData.push([i, memPreData[i]]);
        }

        var entPreData = lines[2].split(',');
        entData = [];

        for (var i = 0; i < entPreData.length; i++)
        {
            entData.push([i, entPreData[i]]);
        }

        var cnkPreData = lines[3].split(',');
        cnkData = [];

        for (var i = 0; i < cnkPreData.length; i++)
        {
            cnkData.push([i, cnkPreData[i]]);
        }
    }, 'text');

    setTimeout(widgetDataReload, 1000);
};

setTimeout(widgetDataReload, 1000);


