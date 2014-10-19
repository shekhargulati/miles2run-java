$(function () {
    setTimezoneCookie();
});

function setTimezoneCookie() {

    var timezone_cookie = "timezoneoffset";

    // if the timezone cookie not exists create one.
    if (!$.cookie(timezone_cookie)) {

        // check if the browser supports cookie
        var test_cookie = 'test cookie';
        $.cookie(test_cookie, true);

        // browser supports cookie
        if ($.cookie(test_cookie)) {

            // delete the test cookie
            $.cookie(test_cookie, null);

            // create a new cookie
            $.cookie(timezone_cookie, new Date().getTimezoneOffset(), {path: '/'});

            // re-load the page
            location.reload();
        }
    }
    // if the current timezone and the one stored in cookie are different
    // then store the new timezone in the cookie and refresh the page.
    else {

        var storedOffset = parseInt($.cookie(timezone_cookie));
        var currentOffset = new Date().getTimezoneOffset();

        // user may have changed the timezone
        if (storedOffset !== currentOffset) {
            $.cookie(timezone_cookie, new Date().getTimezoneOffset(), {path: '/'});
            location.reload();
        }
    }
}