var appContext = window.location.port === "8080" ? "/miles2run/" : "/";

$(document).ready(function () {
    $('a[href="#follow"]').click(function (event) {
        console.log("Pressed follow button");
        event.preventDefault();
        var request = $.ajax({
            url: appContext + 'api/v1/profiles/' + loggedInUsername + '/friendships/create',
            type: 'POST',
            data: JSON.stringify({userToFollow: profileUsername}),
            contentType: "application/json; charset=utf-8",
            dataType: "json"
        });

        request.done(function (response, textStatus, jqXHR) {
            window.location.href = window.location.href;
        });

        request.fail(function (jqXHR, textStatus, errorThrown) {
            console.error("The following error occured: " + textStatus, errorThrown);
        });

    });

    $('a[href="#unfollow"]').click(function (event) {
        console.log("Pressed unfollow button");
        event.preventDefault();
        var request = $.ajax({
            url: appContext + 'api/v1/profiles/' + loggedInUsername + '/friendships/destroy',
            type: 'POST',
            data: JSON.stringify({userToUnfollow: profileUsername}),
            contentType: "application/json; charset=utf-8",
            dataType: "json"
        });

        request.done(function (response, textStatus, jqXHR) {
            window.location.href = window.location.href;
        });

        request.fail(function (jqXHR, textStatus, errorThrown) {
            console.error("The following error occured: " + textStatus, errorThrown);
        });
    });
});