'use strict';

angular.module('milestogo')
    .controller('UserTimelineCtrl', function ($scope, userProfile, $location, $http, TimelineService) {
        var currentPage = 1;
        TimelineService.userTimeline(userProfile.username, currentPage).success(function (data, status, headers, config) {
            $scope.activities = data.timeline;
            $scope.currentPage = currentPage;
            $scope.totalItems = data.totalItems;
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch home timeline. Please try after sometime.");
        });

        $scope.pageChanged = function () {
            console.log('Page changed to: ' + $scope.currentPage);
            TimelineService.userTimeline(userProfile.username, $scope.currentPage).success(function (data, status, headers, config) {
                $scope.activities = data.timeline;
                $scope.totalItems = data.totalItems;
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to fetch home timeline. Please try after sometime.");
            });
        };

    });
