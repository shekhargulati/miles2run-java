'use strict';

var app = angular.module('miles2run-home', [
        'ngCookies',
        'ngResource',
        'ngSanitize',
        'ngRoute',
        'ui.bootstrap'
    ])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'views/home/goals.html',
                controller: 'GoalsCtrl'
            })
            .when('/timeline', {
                templateUrl: 'views/home/timeline.html',
                controller: 'HomeTimelineCtrl'
            })
            .when('/goals/archive', {
                templateUrl: 'views/home/archived.html',
                controller: 'ArchivedGoalsCtrl'
            })
            .when('/goals/create', {
                templateUrl: 'views/home/create.html',
                controller: 'CreateGoalCtrl'
            })
            .when('/friends', {
                templateUrl: 'views/home/friends.html',
                controller: 'FriendsCtrl'
            })
            .when('/goals/edit/:goalId', {
                templateUrl: 'views/home/edit.html',
                controller: 'EditGoalCtrl'
            })
            .when('/goals/:goalId/activity/:activityId', {
                templateUrl: 'views/home/view_activity.html',
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