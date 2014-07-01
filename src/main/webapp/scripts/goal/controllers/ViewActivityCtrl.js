'use strict';

angular.module('milestogo')
    .controller('ViewActivityCtrl', function ($scope, $routeParams, ActivityService, $location, $modal) {
        var activityId = $routeParams.activityId;

        ActivityService.get(activityId).success(function (data) {
            $scope.activity = data;
        }).error(function (data) {
            toastr.error("Unable to fetch activity with id: " + activityId);
            $location.path('/');

        });

        $scope.delete = function () {
            var modalIntance = $modal.open({
                templateUrl: "confirm.html",
                controller: DeleteActivityCtrl,
                resolve: {
                    activityToDelete: function () {
                        return $scope.activity;
                    }
                }
            })

        };

    });

var DeleteActivityCtrl = function ($scope, ActivityService, $modalInstance, activityToDelete, $rootScope, activeGoal, $location) {

    $scope.ok = function () {
        ActivityService.deleteActivity(activityToDelete.id, activeGoal.id).success(function (data, status) {
            toastr.success("Deleted activity");
            $rootScope.$broadcast('update.progress', 'true');
            $modalInstance.close({});
            $location.path("/");
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
};