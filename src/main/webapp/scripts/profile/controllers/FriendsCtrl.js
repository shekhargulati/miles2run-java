'use strict';

angular.module('milestogo')
    .controller('FriendsCtrl', function ($scope, $http, $window, activeProfile, userProfile, ConfigService) {

        $scope.loggedInUser = activeProfile;
        $scope.userProfile = userProfile;


        $scope.unfollowUser = function () {
            $http.post(ConfigService.appContext() + 'api/v1/profiles/' + $scope.loggedInUser.username + "/friendships/destroy", {"userToUnfollow": $scope.userProfile.username}).success(function (data, status, headers, config) {
                console.log("User unfollowed... " + status);
                $window.location.href = ConfigService.appContext() + 'profiles/' + $scope.userProfile.username;
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to unfollow user. Please try later.");
            });
        }

        $scope.followUser = function () {
            $http.post(ConfigService.appContext() + 'api/v1/profiles/' + $scope.loggedInUser.username + "/friendships/create", {"userToFollow": $scope.userProfile.username}).success(function (data, status, headers, config) {
                console.log("User followed... " + status);
                $window.location.href = ConfigService.appContext() + 'profiles/' + $scope.userProfile.username;
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to follow user. Please try later.");
            });
        }

    });
