'use strict';

angular.module('milestogo')
    .controller('MainCtrl', function ($scope, ActivityService, activeProfile, $modal) {
        $scope.currentUser = activeProfile;

        ActivityService.timeline($scope.currentUser.username).success(function (data, status, headers, config) {
            $scope.activities = data;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch timeline. Please try after sometime.");
        });

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
        }).error(function () {
            toastr.error("Unable to delete activity. Please try later.");
            $modalInstance.close({});
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};