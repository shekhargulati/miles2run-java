'use strict';

var app = angular.module('milestogo', [
        'ngCookies',
        'ngResource',
        'ngSanitize',
        'ngRoute',
        'ui.bootstrap'
    ])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'views/home/dashboard.html',
                controller: 'DashboardCtrl'
            })
            .when('/timeline', {
                templateUrl: 'views/home/timeline.html',
                controller: 'TimelineCtrl'
            })
            .when('/activity/post', {
                templateUrl: 'views/home/postactivity.html',
                controller: 'PostActivityCtrl'
            }).when('/activity/calendar', {
                templateUrl: 'views/home/calendar.html',
                controller: 'ActivityCalendarCtrl'
            })
            .when('/activity/share/:activityId', {
                templateUrl: 'views/home/share.html',
                controller: 'ShareActivityCtrl'
            })
            .when('/progress', {
                templateUrl: 'views/home/progress.html',
                controller: 'ProgressCtrl'
            })
            .when('/notifications', {
                templateUrl: 'views/home/notifications.html',
                controller: 'NotificationsCtrl'
            })
            .when('/friends', {
                templateUrl: 'views/home/friends.html',
                controller: 'FriendsCtrl'
            })
            .when('/activity/edit/:activityId', {
                templateUrl: 'views/home/EditActivity.html',
                controller: 'EditActivityCtrl'
            }).when('/activity/:activityId', {
                templateUrl: 'views/home/ViewActivity.html',
                controller: 'ViewActivityCtrl'
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

app.filter('duration', function () {
    return function (text) {
        if (text) {
            var durationInSeconds = Number(text);
            var hours = Math.floor(durationInSeconds / (60 * 60));
            var minutes = Math.floor(durationInSeconds / 60) - (hours * 60);
            var seconds = durationInSeconds - (minutes * 60) - (hours * 60 * 60);
            var hourText = hours < 10 ? "0" + hours : hours.toString();
            var minutestText = minutes < 10 ? "0" + minutes : minutes.toString();
            var secondsText = seconds < 10 ? "0" + seconds : seconds.toString();
            return hourText + ":" + minutestText + ":" + secondsText;
        }
        return "00:00:00";
    }
});


app.filter('pace', function () {
    return function (distanceCoveredInText, durationInText) {
        if (distanceCoveredInText && durationInText) {
            var distanceCovered = Number(distanceCoveredInText);
            var durationInMinutes = Number(durationInText) / 60;
            return durationInMinutes / distanceCovered;
        }
        return "0";
    }
});

app.config(['$provide', function ($provide) {
    var profile = angular.copy(window.activeUserProfile);
    $provide.constant('activeProfile', profile);
}]);


function HeaderCtrl($scope, $location) {

    $scope.isActive = function (viewLocation) {
        return viewLocation === $location.path();
    };
}

function ProgressCtrl($scope, ProgressService, activeProfile, $rootScope) {

    $scope.currentUser = activeProfile;

    ProgressService.progress($scope.currentUser.username).success(function (data, status, headers, config) {
        $scope.error = null;
        $scope.status = status;
        $scope.data = data;
        $scope.style = "width:" + data.percentage + "%";
    });

    $rootScope.$on('update.progress', function (event, value) {
        ProgressService.progress($scope.currentUser.username).success(function (data, status, headers, config) {
            $scope.error = null;
            $scope.status = status;
            $scope.data = data;
            $scope.style = "width:" + data.percentage + "%";
        });
    });
}

function NotificationCtrl($scope, $http, activeProfile, ConfigService) {

    $scope.fetchNotifications = function () {
        $http.get(ConfigService.appContext() + 'api/v1/profiles/' + activeProfile.username + "/notifications").success(function (data, status, headers, config) {
            $scope.notifications = data;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch notifications. Please try later");
        });
    }


    $scope.appContext = function () {
        return ConfigService.appContext();
    }
}