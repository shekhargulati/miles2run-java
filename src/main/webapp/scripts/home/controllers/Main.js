'use strict';

angular.module('milestogo')
    .controller('MainCtrl', function ($scope, ActivityService, activeProfile, $modal, ConfigService, $location) {
        $scope.currentUser = activeProfile;

        ActivityService.timeline($scope.currentUser.username).success(function (data, status, headers, config) {
            $scope.activities = data;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch timeline. Please try after sometime.");
        });

        $scope.messageToShare = function (activity) {
            return activity.fullname + ' ran ' + activity.distanceCovered + ' ' + activity.goalUnit + ' &via=miles2runorg';
        };

        $scope.redirectUri = function () {
            if ($location.host() === "localhost") {
                return "http://localhost:8080/miles2run"
            }
            return "http://" + $location.host();
        }

        $scope.facebookAppId = function(){
            if ($location.host() === "localhost") {
                return 433218286822536;
            }
            return 433218286822536;
        }

        $scope.delete = function (idx) {
            var modalIntance = $modal.open({
                templateUrl: "confirm.html",
                controller: DeleteActivityCtrl,
                resolve: {
                    activityToDelete: function () {
                        var activityToDelete = $scope.activities[idx];
                        return activityToDelete;
                    },
                    idx: function () {
                        return idx;
                    },
                    activities: function () {
                        return $scope.activities;
                    }
                }
            })

        };

    });

var DeleteActivityCtrl = function ($scope, ActivityService, activeProfile, $modalInstance, activityToDelete, idx, activities) {

    $scope.currentUser = activeProfile;

    $scope.ok = function () {
        ActivityService.deleteActivity($scope.currentUser.username, activityToDelete.id).success(function (data, status) {
            toastr.success("Deleted activity");
            activities.splice(idx, 1);
            $modalInstance.close({});
        }).error(function (data, status, headers, config) {
            console.log("Status code %s", status);
            if (status == 401) {
                toastr.error("You are not authorized to perform this operation.")
            } else {
                toastr.error("Unable to delete activity. Please try later.");
            }
            $modalInstance.close({});
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

    $scope.appContext = function () {
        return ConfigService.appContext();
    };

};