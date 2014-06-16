'use strict';

angular.module('miles2run-home')
    .controller('ArchivedGoalsCtrl', function ($scope, $http, $window, activeProfile, ConfigService, $modal) {
        $http.get(ConfigService.getBaseUrl() + "goals/", {params: {"archived": true}}).success(function (data) {
            $scope.goals = data;
        }).error(function (data, status) {
            toastr.error("Unable to fetch goals. Please try after sometime.");
            console.log("Error " + data);
            console.log("Status " + status)
        });

        $scope.appContext = ConfigService.appContext();

        $scope.unarchive = function (idx) {
            var modalIntance = $modal.open({
                templateUrl: "confirm.html",
                controller: UnArchiveCtrl,
                resolve: {
                    goalToUnArchive: function () {
                        var goalToUnArchive = $scope.goals[idx];
                        return goalToUnArchive;
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

var UnArchiveCtrl = function ($scope, $http, $modalInstance, goalToUnArchive, idx, goals, ConfigService) {

    $scope.ok = function () {
        $http.put(ConfigService.getBaseUrl() + 'goals/' + goalToUnArchive.id + "/archive", {}, {params: {"archived": false}}).success(function (data, status) {
            toastr.success("Unarchived goal");
            goals.splice(idx, 1);
            $modalInstance.close({});
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