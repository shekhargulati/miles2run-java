'use strict';

angular.module('milestogo')
    .controller('ViewActivityCtrl', function ($scope, $routeParams, ActivityService, $location) {
        var activityId = $routeParams.activityId;

        ActivityService.get(activityId).success(function (data) {
            $scope.activity = data;
        }).error(function (data) {
            toastr.error("Unable to fetch activity with id: " + activityId);
            $location.path('/');

        });

});