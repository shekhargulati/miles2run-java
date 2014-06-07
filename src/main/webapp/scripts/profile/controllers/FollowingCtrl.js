'use strict';

angular.module('milestogo')
    .controller('FollowingCtrl', function ($scope, userProfile, FriendsService, ConfigService) {

        FriendsService.following(userProfile.username).then(function (response) {
            $scope.following = response.data;
        });

        $scope.context = ConfigService.appContext();

    });
