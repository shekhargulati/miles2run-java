'use strict';

function CreateCommunityGoalCtrl($scope, $location, activeProfile, $http, ConfigService, $window) {

    $scope.getCommunityRuns = function (val) {
        return $http.get(ConfigService.appContext() + 'api/v1/community_runs', {
            params: {
                name: val,
                sensor: false
            }
        }).then(function (res) {
            var communityRuns = res.data;
            return communityRuns;
        });
    }

    $scope.fetchCommunityRun = function () {
        $window.location.href = ConfigService.appContext() + "community_runs/" + $scope.communityRun.slug;
    }

}

angular.module('miles2run-home')
    .controller('CreateCommunityGoalCtrl', CreateCommunityGoalCtrl);
