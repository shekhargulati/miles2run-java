'use strict';

var app = angular.module('milestogo', [
        'ngCookies',
        'ngResource',
        'ngSanitize',
        'ngRoute'
    ])
    .config(function ($routeProvider) {

        $routeProvider
            .when('/', {
                templateUrl: 'views/profile/timeline.html',
                controller: 'TimelineCtrl'
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

app.config(['$provide', function ($provide) {
    var activeUserProfile = angular.copy(window.activeUserProfile);
    $provide.constant('activeProfile', activeUserProfile);
    var userProfile = angular.copy(window.userProfile);
    $provide.constant('userProfile', userProfile);
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