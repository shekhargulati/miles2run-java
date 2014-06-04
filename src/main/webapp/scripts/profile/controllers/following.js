'use strict';

angular.module('milestogo')
    .controller('FollowingCtrl', function ($scope, userProfile, $http, $location) {

        var context = "/"
        if ($location.port() === 8080) {
            context = "/miles2run/";
        }

        $http.get(context + 'api/v1/profiles/' + userProfile.username + '/following').then(function (response) {
            $scope.following = response.data;
        });

        $scope.context = context;

    });
