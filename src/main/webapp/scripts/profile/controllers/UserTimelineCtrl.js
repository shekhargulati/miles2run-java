'use strict';

angular.module('miles2run-profile')
    .controller('UserTimelineCtrl', function ($scope, activeGoal, $location, $http, TimelineService, userProfile, ConfigService) {

        $scope.userProfile = userProfile;

        var currentPage = 1;

        if (activeGoal != null) {
            TimelineService.userGoalTimeline(userProfile.username, activeGoal.id, currentPage).success(function (data, status, headers, config) {
                $scope.activities = data.timeline;
                $scope.currentPage = currentPage;
                $scope.totalItems = data.totalItems;
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to fetch home timeline. Please try after sometime.");
            });
        } else {
            $scope.activities = [];
        }


        $scope.pageChanged = function () {
            console.log('Page changed to: ' + $scope.currentPage);
            TimelineService.userGoalTimeline(userProfile.username, activeGoal.id, $scope.currentPage).success(function (data, status, headers, config) {
                $scope.activities = data.timeline;
                $scope.totalItems = data.totalItems;
            }).error(function (data, status, headers, config) {
                toastr.error("Unable to fetch home timeline. Please try after sometime.");
            });
        };

        $scope.appContext = function(){
            return ConfigService.appContext();
        }
    });
