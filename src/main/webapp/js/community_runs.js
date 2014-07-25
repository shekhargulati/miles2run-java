var app = angular.module("miles2run-crs", ['ui.bootstrap']);

var searchCtrl = function ($scope, $http, $window, $location) {

    $scope.fetchCommunityRun = function () {
        $window.location.href = appContext() + "community_runs/" + $scope.communityRun.slug;
    }

    $scope.getCommunityRuns = function (val) {
        return $http.get(appContext() + 'api/v1/community_runs', {
            params: {
                name: val,
                sensor: false
            }
        }).then(function (res) {
            var communityRuns = res.data;
            return communityRuns;
        });
    }

    function appContext() {
        if ($location.port() === 8080) {
            return "/miles2run/";
        } else {
            return "/";
        }
    }

};
app.controller('CommunityRunsSearchCtrl', searchCtrl)