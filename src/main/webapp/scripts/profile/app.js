'use strict';

var app = angular.module('miles2run-profile', [
        'ngCookies',
        'ngResource',
        'ngSanitize',
        'ngRoute',
        'ui.bootstrap'
    ])
    .config(function ($routeProvider) {

        $routeProvider
            .when('/', {
                templateUrl: 'views/profile/timeline.html',
                controller: 'UserTimelineCtrl'
            })
            .when('/followers', {
                templateUrl: 'views/profile/followers.html',
                controller: 'FollowersCtrl'
            })
            .when('/following', {
                templateUrl: 'views/profile/following.html',
                controller: 'FollowingCtrl'
            })
            .otherwise({
                redirectTo: '/'
            });
    });

app.filter('moment', function () {
    return function (text) {
        return moment(text, "MMDDYYYY HH mm ss").fromNow();
    }
});

app.config(['$provide', function ($provide) {
    var activeUserProfile = angular.copy(window.activeUserProfile);
    $provide.constant('activeProfile', activeUserProfile);
    var userProfile = angular.copy(window.userProfile);
    $provide.constant('userProfile', userProfile);
    var activeGoal = angular.copy(window.activeGoal);
    $provide.constant('activeGoal', activeGoal);
}]);

app.run(function ($rootScope, $location) {
    $rootScope.$on('$routeChangeStart', function (event, next, current) {
        var context = "/"
        if ($location.port() === 8080) {
            context = "/miles2run/";
        }
        next.templateUrl = context + next.templateUrl;
    });
});


function HeaderCtrl($scope, $location) {
    $scope.isActive = function (viewLocation) {
        return viewLocation === $location.path();
    };
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