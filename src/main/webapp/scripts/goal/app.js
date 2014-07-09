'use strict';

var app = angular.module('milestogo', [
        'ngCookies',
        'ngResource',
        'ngSanitize',
        'ngRoute',
        'ui.bootstrap',
        'ngAnimate',
        'cgBusy'
    ])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: function () {
                    var activeGoal = angular.copy(window.activeGoal);
                    if (activeGoal.goalType.$name === 'DISTANCE_GOAL') {
                        return  '../views/goal/goal_distance_dashboard.html';
                    } else {
                        return '../views/goal/goal_duration_dashboard.html';
                    }
                },
                controller: 'DashboardCtrl'
            })
            .when('/timeline', {
                templateUrl: '../views/goal/timeline.html',
                controller: 'TimelineCtrl'
            })
            .when('/activity/post', {
                templateUrl: '../views/goal/postactivity.html',
                controller: 'PostActivityCtrl'
            }).when('/activity/calendar', {
                templateUrl: '../views/goal/calendar.html',
                controller: 'ActivityCalendarCtrl'
            })
            .when('/activity/share/:activityId', {
                templateUrl: '../views/goal/share.html',
                controller: 'ShareActivityCtrl'
            })
            .when('/progress', {
                templateUrl: '../views/goal/progress.html',
                controller: 'ProgressCtrl'
            })
            .when('/activity/edit/:activityId', {
                templateUrl: '../views/goal/EditActivity.html',
                controller: 'EditActivityCtrl'
            }).when('/activity/:activityId', {
                templateUrl: '../views/goal/ViewActivity.html',
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
    var activeGoal = angular.copy(window.activeGoal);
    $provide.constant('activeGoal', activeGoal);
}]);


function HeaderCtrl($scope, $location) {
    $scope.isActive = function (viewLocation) {
        return viewLocation === $location.path();
    };
}

function ProgressCtrl($scope, ProgressService, activeProfile, $rootScope, activeGoal) {

    $scope.currentUser = activeProfile;

    ProgressService.progress(activeGoal.id).success(function (data, status, headers, config) {
        $scope.error = null;
        $scope.status = status;
        $scope.percentage = Math.ceil(data.percentage);
        $scope.style = "width:" + Math.ceil(data.percentage) + "%";
    });

    $rootScope.$on('update.progress', function (event, value) {
        ProgressService.progress(activeGoal.id).success(function (data, status, headers, config) {
            $scope.error = null;
            $scope.status = status;
            $scope.percentage = Math.ceil(data.percentage);
            $scope.style = "width:" + Math.ceil(data.percentage) + "%";
        });
    });
}