'use strict';

angular.module('miles2run-profile')
    .controller('FriendsCtrl', function ($scope, $http, $window, activeProfile, userProfile, ConfigService) {

        $scope.loggedInUser = activeProfile;
        $scope.userProfile = userProfile;


        $scope.unfollowUser = function () {
            $http.post(ConfigService.appContext() + "api/v1/friendships/destroy", {"userToUnfollow": $scope.userProfile.username}).success(function (data, status, headers, config) {
                console.log("User unfollowed... " + status);
                $window.location.href = ConfigService.appContext() + 'users/' + $scope.userProfile.username;
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to unfollow user. Please try later.");
            });
        }

        $scope.followUser = function () {
            $http.post(ConfigService.appContext() + "api/v1/friendships/create", {"userToFollow": $scope.userProfile.username}).success(function (data, status, headers, config) {
                console.log("User followed... " + status);
                $window.location.href = ConfigService.appContext() + 'users/' + $scope.userProfile.username;
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to follow user. Please try later.");
            });
        }

    });
