var app = angular.module("milestogo", []);
app.filter('moment', function () {
    return function (text) {
        return moment(text, "MMDDYYYY HH mm ss").fromNow();
    }
});

app.config(['$provide', function ($provide) {
    var activeUserProfile = angular.copy(window.activeUserProfile);
    $provide.constant('activeProfile', activeUserProfile);
}]);


app.filter('unit', function () {
    return function (text, unit) {
        var n = text ? Number(text) : 0;
        if (unit === 'kms') {
            var kms = n / 1000;
            return kms;
        } else {
            var miles = n / 1609;
            return miles;
        }

    }
});

app.filter('toHour', function () {
    return function (text) {
        var n = text ? Number(text) : 0;
        var hours = n / 3600;
        return hours;

    }
});


app.controller('CounterCtrl', CounterCtrl);

function CounterCtrl($scope, $http, $timeout) {

    var poll = function () {
        $timeout(function () {
            $http.get("api/v1/counters").success(function (data, status, headers, config) {
                $scope.counter = data;
                poll();
            }).error(function (data, status, headers, config) {
                console.log(data);
                console.log(status);
                poll();
            });
        }, 10000);

    }

    poll();

    $http.get("api/v1/counters").success(function (data, status, headers, config) {
        $scope.counter = data;
    });

}
function NotificationCtrl($scope, $http, activeProfile, $location) {

    $scope.fetchNotifications = function () {
        $http.get($scope.appContext() + 'api/v1/profiles/' + activeProfile.username + "/notifications").success(function (data, status, headers, config) {
            $scope.notifications = data;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch notifications. Please try later");
        });
    }


    $scope.appContext = function () {
        var context = "/"
        if ($location.port() === 8080) {
            context = "/miles2run/";
        }
        return context;
    }
}