'use strict';

angular.module('miles2run-home')
    .controller('ArchivedGoalsCtrl', function ($scope, $http, $window, activeProfile, ConfigService, $modal) {

        $scope.goalExists = true;

        $scope.archivedGoalsPromise = $http.get(ConfigService.getBaseUrl() + "goals/", {params: {"archived": true}}).success(function (data) {
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

        $scope.unarchive = function (idx, goalType) {
            var modalIntance = $modal.open({
                templateUrl: "confirm.html",
                controller: UnArchiveCtrl,
                resolve: {
                    goalToUnArchive: function () {
                        if (goalType === 'DISTANCE_GOAL') {
                            var goalToUnArchive = $scope.distanceGoals[idx];
                            return goalToUnArchive;
                        } else if (goalType === 'DURATION_GOAL') {
                            var goalToUnArchive = $scope.durationGoals[idx];
                            return goalToUnArchive;
                        } else if (goalType === 'COMMUNITY_RUN_GOAL') {
                            var goalToUnArchive = $scope.communityRunGoals[idx];
                            return goalToUnArchive;
                        }else {
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
                        }else if (goalType === 'COMMUNITY_RUN_GOAL') {
                            return $scope.communityRunGoals;
                        } else {
                            return null;
                        }
                    }
                }
            })

        };
    });

var UnArchiveCtrl = function ($scope, $http, $modalInstance, goalToUnArchive, idx, goals, ConfigService, $location, $route) {

    $scope.ok = function () {
        $http.put(ConfigService.getBaseUrl() + 'goals/' + goalToUnArchive.id + "/archive", {}, {params: {"archived": false}}).success(function (data, status) {
            toastr.success("Unarchived goal");
            goals.splice(idx, 1);
            $modalInstance.close({});
            $location.path('/goals/archive');
            $route.reload();
        }).error(function (data, status, headers, config) {
            console.log("Status code %s", status);
            if (status == 401) {
                toastr.error("You are not authorized to perform this operation.")
            } else {
                toastr.error("Unable to Unarchived goal. Please try later.");
            }
            $modalInstance.close({});
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};