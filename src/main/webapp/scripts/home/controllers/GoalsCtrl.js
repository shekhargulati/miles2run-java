'use strict';

angular.module('miles2run-home')
    .controller('GoalsCtrl', function ($scope, $http, $window, activeProfile, ConfigService, $modal) {
        $http.get(ConfigService.getBaseUrl() + "goals").success(function (data) {
            $scope.goals = data;
        }).error(function (data, status) {
            toastr.error("Unable to fetch goals. Please try after sometime.");
            console.log("Error " + data);
            console.log("Status " + status)
        });

        $scope.appContext = ConfigService.appContext();

        $scope.delete = function (idx) {
            var modalIntance = $modal.open({
                templateUrl: "confirm.html",
                controller: DeleteGoalCtrl,
                resolve: {
                    goalToDelete: function () {
                        var goalToDelete = $scope.goals[idx];
                        return goalToDelete;
                    },
                    idx: function () {
                        return idx;
                    },
                    goals: function () {
                        return $scope.goals;
                    }
                }
            })

        };
    });

var DeleteGoalCtrl = function ($scope, $http, $modalInstance, goalToDelete, idx, goals, ConfigService) {

    $scope.ok = function () {
        $http.delete(ConfigService.getBaseUrl() + 'goals/' + goalToDelete.id).success(function (data, status) {
            toastr.success("Deleted goal");
            goals.splice(idx, 1);
            $modalInstance.close({});
        }).error(function (data, status, headers, config) {
            console.log("Status code %s", status);
            if (status == 401) {
                toastr.error("You are not authorized to perform this operation.")
            } else {
                toastr.error("Unable to delete goal. Please try later.");
            }
            $modalInstance.close({});
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};