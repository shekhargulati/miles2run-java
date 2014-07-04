'use strict';

angular.module('miles2run-home')
    .controller('GoalsCtrl', function ($scope, $http, $window, activeProfile, ConfigService, $modal) {
        $scope.goalsPromise =  $http.get(ConfigService.getBaseUrl() + "goals").success(function (data) {
            $scope.goals = data;
        }).error(function (data, status) {
            toastr.error("Unable to fetch goals. Please try after sometime.");
            console.log("Error " + data);
            console.log("Status " + status)
        });

        $scope.appContext = ConfigService.appContext();

        $scope.archive = function (idx) {
            var modalIntance = $modal.open({
                templateUrl: "confirm.html",
                controller: ArchiveGoalCtrl,
                resolve: {
                    goalToArchive: function () {
                        var goalToArchive = $scope.goals[idx];
                        return goalToArchive;
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

var ArchiveGoalCtrl = function ($scope, $http, $modalInstance, goalToArchive, idx, goals, ConfigService) {

    $scope.ok = function () {
        $http.put(ConfigService.getBaseUrl() + 'goals/' + goalToArchive.id + "/archive", {}, {params: {"archived": true}}).success(function (data, status) {
            toastr.success("Archived goal");
            goals.splice(idx, 1);
            $modalInstance.close({});
        }).error(function (data, status, headers, config) {
            console.log("Status code %s", status);
            if (status == 401) {
                toastr.error("You are not authorized to perform this operation.")
            } else {
                toastr.error("Unable to archive goal. Please try later.");
            }
            $modalInstance.close({});
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};