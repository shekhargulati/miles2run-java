'use strict';

angular.module('miles2run-home')
    .controller('ViewActivityCtrl', function ($scope, $routeParams, ConfigService, $location, $http) {
        var goalId = $routeParams.goalId;
        var activityId = $routeParams.activityId;

        $http.get(ConfigService.getBaseUrl() + "goals/" + goalId + "/activities/" + activityId).success(function (data) {
            $scope.activity = data;
        }).error(function (data) {
            toastr.error("Unable to fetch activity with id: " + activityId);
            $location.path('/');

        });

    });