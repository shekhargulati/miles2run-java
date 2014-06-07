'use strict';

angular.module('milestogo')
    .controller('FollowersCtrl', function ($scope, userProfile, FriendsService, ConfigService) {

        FriendsService.followers(userProfile.username).then(function (response) {
            $scope.followers = response.data;
        });

        $scope.context = ConfigService.appContext();
    });