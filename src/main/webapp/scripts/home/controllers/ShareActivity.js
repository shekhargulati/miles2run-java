'use strict';

angular.module('milestogo')
    .controller('ShareActivityCtrl', function ($scope, $routeParams, ActivityService, $location, activeProfile) {
        $scope.currentUser = activeProfile;

        var activityId = $routeParams.activityId;

        ActivityService.get($scope.currentUser.username, activityId).success(function (data) {
            $scope.activityDetails = data;
        }).error(function (data) {
            toastr.error("Unable to fetch activity with id: " + activityId);
            $location.path('/');

        });

        $scope.share = function () {
            var activity = {
                id: $scope.activityDetails.id,
                status: $scope.activityDetails.status,
                share: $scope.activityDetails.share
            };

            ActivityService.shareActivity($scope.currentUser.username, activityId, activity).success(function (data, status, headers, config) {
                toastr.success("Shared activity");
                $location.path('/');
            }).error(function (data, status, headers, config) {
                console.log("Error handler for share activity. Status code " + status);
                toastr.error("Unable to share activity. Please try later.");
                $location.path('/');
            });
        };

        $scope.socialProviderSelected = function () {
            if(!$scope.activityDetails){
                return "disabled";
            }
            return anyProviderSelected($scope.activityDetails.share) ? "goodtogo" : "disabled";
        }

        var anyProviderSelected = function(share){
            if(share.facebook || share.twitter || share.googlePlus){
                return true;
            }
            return false;
        }


    });
