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
            .when('/goals/create', {
                templateUrl: 'views/home/create.html',
                controller: 'CreateGoalCtrl'
            })
            .when('/friends', {
                templateUrl: 'views/home/friends.html',
                controller: 'FriendsCtrl'
            })
            .when('/goal/edit/:goalId', {
                templateUrl: 'views/home/edit.html',
                controller: 'EditGoalCtrl'
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
    var profile = angular.copy(window.activeUserProfile);
    $provide.constant('activeProfile', profile);
}]);


function HeaderCtrl($scope, $location) {

    $scope.isActive = function (viewLocation) {
        return viewLocation === $location.path();
    };
}