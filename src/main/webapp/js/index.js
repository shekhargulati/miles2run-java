var app = angular.module("milestogo", []);

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

app.filter('countryText', function () {
    return function (text) {
        var n = text ? Number(text) : 0;
        return n > 1 ? n + " Countries" : n + " Country";
    }
});

app.filter('developerText', function () {
    return function (text) {
        var n = text ? Number(text) : 0;
        return n > 1 ? n + " Developers" : n + " Developer";
    }
});

app.controller('CounterCtrl', CounterCtrl);

function CounterCtrl($scope, $http, $timeout) {

    var poll = function () {
        $timeout(function () {
            $http.get("api/v2/counters").success(function (data, status, headers, config) {
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

    $http.get("api/v2/counters").success(function (data, status, headers, config) {
        $scope.counter = data;
    });

}