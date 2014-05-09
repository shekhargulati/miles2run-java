'use strict';

angular.module('milestogo')
    .controller('EditActivityCtrl', function ($scope, $routeParams, ActivityService, $location, activeProfile) {
        $scope.currentUser = activeProfile;
        var activityId = $routeParams.activityId;
        ActivityService.get($scope.currentUser.username, activityId).success(function (data) {
            $scope.activityDetails = data;
            $scope.duration = toHrMinSec(data.duration);
        }).error(function (data) {
            toastr.error("Unable to fetch activity with id: " + activityId);
            $location.path('/');

        });

        $scope.update = function () {

            var activity = {
                id: $scope.activityDetails.id,
                status: $scope.activityDetails.status,
                goalUnit: $scope.activityDetails.goalUnit,
                distanceCovered: $scope.activityDetails.distanceCovered,
                share: $scope.activityDetails.share,
                activityDate: $scope.activityDetails.activityDate
            };
            activity.duration = toSeconds($scope.duration);
            ActivityService.updateActivity($scope.currentUser.username, activityId, activity).success(function (data, status, headers, config) {
                toastr.success("Updated activity");
                $location.path('/');
            }).error(function (data, status, headers, config) {
                console.log("Error handler for update activity. Status code " + status);
                toastr.error("Unable to update activity. Please try later.");
                $location.path('/');
            });
        };

        $scope.today = function () {
            $scope.dt = new Date();
        };
        $scope.today();

        $scope.showWeeks = true;
        $scope.toggleWeeks = function () {
            $scope.showWeeks = !$scope.showWeeks;
        };

        $scope.clear = function () {
            $scope.dt = null;
        };

        // Disable weekend selection
        $scope.disabled = function (date, mode) {
            return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
        };

        $scope.toggleMin = function () {
            $scope.minDate = ( $scope.minDate ) ? null : new Date();
        };
        $scope.toggleMin();

        $scope.open = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();

            $scope.opened = true;
        };

        $scope.dateOptions = {
            'year-format': "'yy'",
            'starting-day': 1
        };

        $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'shortDate'];
        $scope.format = $scope.formats[0];
    });


function toSeconds(duration) {
    if (duration) {
        var hours = duration.hours ? duration.hours : 0;
        var minutes = duration.minutes ? duration.minutes : 0;
        var seconds = duration.seconds ? duration.seconds : 0;
        return hours * 60 * 60 + minutes * 60 + seconds;
    }
    return 0;

}

function toHrMinSec(duration) {
    var hours = Math.floor(duration / (60 * 60));
    var minutes = Math.floor(duration / 60) - (hours * 60);
    var seconds = duration - (minutes * 60) - (hours * 60 * 60);
    return  {
        hours: hours,
        minutes: minutes,
        seconds: seconds
    }
}