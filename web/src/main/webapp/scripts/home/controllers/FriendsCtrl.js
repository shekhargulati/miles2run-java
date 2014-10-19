'use strict';

angular.module('miles2run-home')
    .controller('FriendsCtrl', function ($scope, $http, $window, activeProfile, ConfigService, $rootScope) {

        $scope.currentUser = activeProfile;

        $scope.getProfiles = function (val) {
            return $http.get(ConfigService.appContext() + 'api/v1/users', {
                params: {
                    name: val,
                    sensor: false
                }
            }).then(function (res) {
                var profiles = res.data;
                return profiles;
            });
        };

        $scope.fetchProfile = function () {
            $window.location.href = ConfigService.appContext() + "users/" + $scope.profile.username;
        };

        $http.get(ConfigService.appContext() + "api/v1/users/suggestions").then(function (response) {
            $scope.friends = response.data;
        });

        $http.get(ConfigService.appContext() + 'api/v1/users/me/following').then(function (response) {
            $scope.following = response.data;
        });


        $scope.followUser = function (friend, idx) {
            console.log(friend);
            $http.post(ConfigService.appContext() + "api/v1/friendships/create", {"userToFollow": friend.username}).success(function (data, status, headers, config) {
                $scope.friends.splice(idx, 1);
                $rootScope.$broadcast('update.following', 'true');
                toastr.success("Successfully followed user");
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to follow user. Please try later");
            });
        }

        $scope.unfollowUser = function (friend, idx) {
            $http.post(ConfigService.appContext() + "api/v1/friendships/destroy", {"userToUnfollow": friend.username}).success(function (data, status, headers, config) {
                console.log("User unfollowed... " + status);
                $scope.following.splice(idx, 1);
                toastr.success("Successfully unfollowed user");
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to unfollow user. Please try later.");
            });
        }


        $rootScope.$on('update.following', function (event, value) {
            $http.get(ConfigService.appContext() + 'api/v1/profiles/me/following').then(function (response) {
                $scope.following = response.data;
            });
        });

    });
