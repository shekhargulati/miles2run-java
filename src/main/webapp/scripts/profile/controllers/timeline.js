'use strict';

angular.module('milestogo')
    .controller('TimelineCtrl', function ($scope, userProfile, $location, $http) {
        var context = "/"
        if ($location.port() === 8080) {
            context = "/miles2run/";
        }
        $http.get(context + "api/v1/timeline/" + userProfile.username + "/profile").success(function (data, status, headers, config) {
            $scope.activities = data;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch timeline. Please try after sometime.");
        });

    });
