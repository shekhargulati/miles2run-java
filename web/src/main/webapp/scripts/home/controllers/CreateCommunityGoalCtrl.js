'use strict';

function CreateCommunityGoalCtrl($scope, $location, activeProfile, $http, ConfigService, $window, $modal) {

    $http.get(ConfigService.appContext() + 'api/v1/community_runs', {
        params: {
            include_stats: true,
            include_participation_detail: true
        }
    }).then(function (res) {
        $scope.communityRuns = res.data;
    });


    $scope.joinCommunityRun = function (idx) {
        var modalIntance = $modal.open({
            templateUrl: "confirm_join.html",
            controller: CommunityRunJoinCtrl,
            resolve: {
                communityRunToJoin: function () {
                    return $scope.communityRuns[idx];

                },
                idx: function () {
                    return idx;
                },
                communityRuns: function () {
                    return $scope.communityRuns;
                }
            }
        })

    };


    $scope.leaveCommunityRun = function (idx) {
        var modalIntance = $modal.open({
            templateUrl: "confirm_leave.html",
            controller: CommunityRunLeaveCtrl,
            resolve: {
                communityRunToLeave: function () {
                    return $scope.communityRuns[idx];

                },
                idx: function () {
                    return idx;
                },
                communityRuns: function () {
                    return $scope.communityRuns;
                }
            }
        })

    };
}
var CommunityRunJoinCtrl = function ($scope, $http, $modalInstance, communityRunToJoin, idx, communityRuns, ConfigService, $window) {

    $scope.ok = function () {
        $http.post(ConfigService.getBaseUrl() + 'community_runs/' + communityRunToJoin.slug + "/join", {}).success(function (data, status) {
            toastr.success("Joined Community Run");
            $modalInstance.close({});
            $window.location.href = ConfigService.appContext() + 'goals/' + data.id;

        }).error(function (data, status, headers, config) {
            console.log("Status code %s", status);
            toastr.error("Unable to join CommunityRun. Please try later.");
            $modalInstance.close({});
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};


var CommunityRunLeaveCtrl = function ($scope, $http, $modalInstance, communityRunToLeave, idx, communityRuns, ConfigService, $window) {

    $scope.ok = function () {
        $http.post(ConfigService.getBaseUrl() + 'community_runs/' + communityRunToLeave.slug + "/leave", {}).success(function (data, status) {
            toastr.success("Left Community Run");
            $modalInstance.close({});
            $window.location.href = ConfigService.appContext();

        }).error(function (data, status, headers, config) {
            console.log("Status code %s", status);
            toastr.error("Unable to leave CommunityRun. Please try later.");
            $modalInstance.close({});
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};


angular.module('miles2run-home')
    .controller('CreateCommunityGoalCtrl', CreateCommunityGoalCtrl);
