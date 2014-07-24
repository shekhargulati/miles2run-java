'use strict';

var app = angular.module('miles2run-home', [
        'ngCookies',
        'ngResource',
        'ngSanitize',
        'ngRoute',
        'ui.bootstrap',
        'ngAnimate',
        'cgBusy',
        'ngTagsInput'
    ])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/goals', {
                templateUrl: 'views/home/goals.html',
                controller: 'GoalsCtrl'
            })
            .when('/goals/active', {
                templateUrl: 'views/home/goals.html',
                controller: 'GoalsCtrl'
            })
            .when('/', {
                templateUrl: 'views/home/timeline.html',
                controller: 'HomeTimelineCtrl'
            })
            .when('/community_run', {
                templateUrl: 'views/home/community_run.html',
                controller: 'CommunityRunCtrl'
            })
            .when('/goals/archive', {
                templateUrl: 'views/home/archived.html',
                controller: 'ArchivedGoalsCtrl'
            })
            .when('/goals/create', {
                templateUrl: 'views/home/create.html',
                controller: 'CreateGoalCtrl'
            })
            .when('/goals/create_distance_goal', {
                templateUrl: 'views/home/create_distance_goal.html',
                controller: 'CreateDistanceGoalCtrl'
            })
            .when('/goals/create_duration_goal', {
                templateUrl: 'views/home/create_duration_goal.html',
                controller: 'CreateDurationGoalCtrl'
            })
            .when('/goals/create_race_goal', {
                templateUrl: 'views/home/create_duration_goal.html',
                controller: 'CreateGoalCtrl'
            })
            .when('/friends', {
                templateUrl: 'views/home/friends.html',
                controller: 'FriendsCtrl'
            })
            .when('/goals/edit_distance_goal/:goalId', {
                templateUrl: 'views/home/edit_distance_goal.html',
                controller: 'EditGoalCtrl'
            })
            .when('/goals/edit_duration_goal/:goalId', {
                templateUrl: 'views/home/edit_duration_goal.html',
                controller: 'EditGoalCtrl'
            })
            .when('/goals/:goalId/activity/:activityId', {
                templateUrl: 'views/home/view_activity.html',
                controller: 'ViewActivityCtrl'
            })
            .when('/notifications', {
                templateUrl: 'views/home/notifications.html',
                controller: 'NotificationsCtrl'
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

app.filter('momentDaysBetween', function () {
    return function (text, startDate) {
        if (!text) {
            return "-";
        }
        startDate = startDate ? new Date(startDate) : new Date();
        var currentMoment = moment(moment(startDate).format("MMDDYYYY"),"MMDDYYYY");
        var targetMoment = moment(text, "MMDDYYYY");
        var daysDiff = targetMoment.diff(currentMoment, 'days');
        if (daysDiff > 0) {
            daysDiff += 1; // added one because moment does not consider today's date. The number of days between 23rd July and 24th July should be 2 not 1.
            return daysDiff + ' day(s)';
        } else if (daysDiff === 0) {
            return 'Ends Today'
        } else {
            return moment(text, "MMDDYYYY").fromNow();
        }

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

function HeaderCtrl($scope, $location, ConfigService) {

    $scope.isActive = function (viewLocation) {
        if (viewLocation instanceof Array) {
            for (var i = 0; i < viewLocation.length; i++) {
                if (viewLocation[i] === $location.path()) {
                    return true;
                }
            }
        }
        return viewLocation === $location.path();
    };
    $scope.appContext = function () {
        return ConfigService.appContext();
    }
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