'use strict';

angular.module('miles2run-home')
    .controller('GoalsCtrl', function ($scope, $http, $window, activeProfile, ConfigService, $modal) {
        $scope.goalExists = true;
        $scope.goalsPromise = $http.get(ConfigService.getBaseUrl() + "goals").success(function (data) {
            if (isEmpty(data)) {
                $scope.goalExists = false;
            } else {
                $scope.goalExists = true;
                $scope.distanceGoals = data['DISTANCE_GOAL'];
                $scope.durationGoals = data['DURATION_GOAL'];
                $scope.communityRunGoals = data['COMMUNITY_RUN_GOAL'];
            }
        }).error(function (data, status) {
            toastr.error("Unable to fetch goals. Please try after sometime.");
            console.log("Error " + data);
            console.log("Status " + status)
        });

        var isEmpty = function (obj) {
            return  Boolean(obj && typeof obj == 'object') && !Object.keys(obj).length;
        }

        $scope.appContext = ConfigService.appContext();

        $scope.archive = function (idx, goalType) {
            var modalIntance = $modal.open({
                templateUrl: "confirm.html",
                controller: ArchiveGoalCtrl,
                resolve: {
                    goalToArchive: function () {
                        if (goalType === 'DISTANCE_GOAL') {
                            var goalToArchive = $scope.distanceGoals[idx];
                            return goalToArchive;
                        } else if (goalType === 'DURATION_GOAL') {
                            var goalToArchive = $scope.durationGoals[idx];
                            return goalToArchive;
                        } else if (goalType === 'COMMUNITY_RUN_GOAL') {
                            var goalToArchive = $scope.communityRunGoals[idx];
                            return goalToArchive;
                        } else {
                            return null;
                        }

                    },
                    idx: function () {
                        return idx;
                    },
                    goals: function () {
                        if (goalType === 'DISTANCE_GOAL') {
                            return $scope.distanceGoals;
                        } else if (goalType === 'DURATION_GOAL') {
                            return $scope.durationGoals;
                        } else if (goalType === 'COMMUNITY_RUN_GOAL') {
                            return $scope.communityRunGoals;
                        }else {
                            return null;
                        }
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