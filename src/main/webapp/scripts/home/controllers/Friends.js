'use strict';

angular.module('milestogo')
    .controller('FriendsCtrl', function ($scope, $http, $window, activeProfile) {

        $scope.currentUser = activeProfile;

        $scope.getProfiles = function (val) {
            return $http.get('api/v2/profiles', {
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
            $window.location.href = "profiles/" + $scope.profile.username;
        };

        $http.get('api/v2/profiles/' + $scope.currentUser.username + "/suggestions").then(function (response) {
            $scope.friends = response.data;
        });


        $scope.followUser = function (friend, idx) {
            console.log(friend);
            $http.post('api/v2/profiles/' + $scope.currentUser.username + "/friendships/create", {"userToFollow": friend.username}).success(function (data, status, headers, config) {
                console.log("User followed... " + status);
                $scope.friends.splice(idx, 1);
                toastr.success("Successfully followed user");
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to follow user. Please try later");
            });
        }

    });
