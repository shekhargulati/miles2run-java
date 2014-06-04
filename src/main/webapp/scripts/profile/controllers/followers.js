'use strict';

angular.module('milestogo')
    .controller('FollowersCtrl', function ($scope, userProfile, $http, $location) {

        var context = "/"
        if ($location.port() === 8080) {
            context = "/miles2run/";
        }

        $http.get(context + 'api/v1/profiles/' + userProfile.username + '/followers').then(function (response) {
            $scope.followers = response.data;
        });

        $scope.context = context;
    });