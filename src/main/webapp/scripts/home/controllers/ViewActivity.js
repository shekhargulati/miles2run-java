'use strict';

angular.module('milestogo')
    .controller('ViewActivityCtrl', function ($scope, $routeParams, ActivityService, $location, activeProfile) {
        $scope.currentUser = activeProfile;

        var activityId = $routeParams.activityId;

        ActivityService.get($scope.currentUser.username, activityId).success(function (data) {
            $scope.activity = data;
        }).error(function (data) {
            toastr.error("Unable to fetch activity with id: " + activityId);
            $location.path('/');

        });

});