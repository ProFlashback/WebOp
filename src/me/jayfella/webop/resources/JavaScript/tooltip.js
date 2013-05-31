$(function()
{
    $( document ).tooltip(
    {
        track: true,
        position:
        {
            my: "left+10 top-20",
            at: "left+10 top-20"

        },

        content: function()
        {
            var element = $(this);
            return element.attr("title");
        }

    });
});